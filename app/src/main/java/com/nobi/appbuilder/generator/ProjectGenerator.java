package com.nobi.appbuilder.generator;

import com.nobi.appbuilder.generator.gradle.GradleGenerator;
import com.nobi.appbuilder.generator.java.JavaGenerator;
import com.nobi.appbuilder.generator.manifest.ManifestGenerator;
import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;
import com.nobi.appbuilder.generator.resources.ResourceGenerator;
import com.nobi.appbuilder.generator.xml.XMLGenerator;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRValidator;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central generation pipeline (spec section 11):
 * validate IR -&gt; validate events -&gt; XML layouts -&gt; Java
 * Activities -&gt; resources -&gt; manifest -&gt; Gradle -&gt;
 * cross-reference validation -&gt; report. A result with errors is
 * returned, never reported as success.
 */
public class ProjectGenerator {

    private final GenerationContext context;

    public ProjectGenerator(GenerationContext context) {
        this.context = context;
    }

    public GenerationResult generate() {
        GenerationResult result = new GenerationResult(new GenerationReport());

        // 1-2. Validate the document and its events.
        List<String> irProblems =
                IRValidator.validateDocument(context.getDocument());
        for (String problem : irProblems) {
            context.addError("validate", null, null, problem);
        }
        if (!irProblems.isEmpty()) {
            finishWithProblems(result);
            return result;
        }
        for (ScreenModel screen : context.getDocument().getScreens()) {
            if (screen.getRootComponent() == null) {
                continue;
            }
            validateEvents(screen.getRootComponent(), screen.getId());
        }

        // 3. XML layouts (fills the resource collector + id map).
        XMLGenerator xmlGenerator = new XMLGenerator(context);
        Map<String, String> layouts = new java.util.LinkedHashMap<>();
        for (ScreenModel screen : context.getDocument().getScreens()) {
            String xml = xmlGenerator.generateLayout(screen);
            if (xml != null) {
                layouts.put(screen.getId(), xml);
            }
        }
        if (layouts.size() != context.getDocument().getScreens().size()) {
            context.addError("xml", null, null,
                    "some screens could not be generated as XML");
        }

        // 4. Java Activities.
        JavaGenerator javaGenerator = new JavaGenerator(context);
        Map<String, String> sources = javaGenerator.generateAll(
                context.getResolvedIds(), context.getGeneratedLayouts());
        for (ScreenModel screen : context.getDocument().getScreens()) {
            String source = sources.get(screen.getId());
            if (source == null) {
                continue;
            }
            result.addTextFile(
                    "app/src/main/java/"
                            + context.getPackageName().replace('.', '/') + "/"
                            + context.activityClassFor(screen.getId()) + ".java",
                    source);
        }

        // 5. Resources (after layouts — the collector is now full).
        new ResourceGenerator(context).generateInto(result);

        // 6. Manifest.
        result.addTextFile("app/src/main/AndroidManifest.xml",
                new ManifestGenerator(context).generate());

        // 7. Gradle configuration.
        new GradleGenerator(context).generateInto(result);

        // Layout files go in after Java/resources so the result is
        // complete before cross-reference validation runs.
        for (Map.Entry<String, String> entry : layouts.entrySet()) {
            ScreenModel screen = context.getDocument().getScreen(entry.getKey());
            result.addTextFile(xmlGenerator.layoutPathFor(screen), entry.getValue());
        }

        // 8. Cross-reference validation.
        validateReferences(result);

        result.getReport().setScreensGenerated(layouts.size());
        int components = 0;
        for (ScreenModel screen : context.getDocument().getScreens()) {
            if (screen.getRootComponent() != null) {
                components += countComponents(screen.getRootComponent());
            }
        }
        result.getReport().setComponentsGenerated(components);
        finishWithProblems(result);
        return result;
    }

    private void validateEvents(ComponentModel model, String screenId) {
        for (EventModel event : model.getEvents()) {
            boolean targetExists = event.getTargetScreenId() == null
                    || context.activityClassFor(event.getTargetScreenId()) != null;
            String problem = EventMappingRegistry.validateEvent(event, targetExists);
            if (problem != null) {
                context.addError("events", null, model.getId(),
                        "screen " + screenId + ": " + problem);
            }
        }
        for (ComponentModel child : model.getChildren()) {
            validateEvents(child, screenId);
        }
    }

    private int countComponents(ComponentModel model) {
        int count = 1;
        for (ComponentModel child : model.getChildren()) {
            count += countComponents(child);
        }
        return count;
    }

    private void finishWithProblems(GenerationResult result) {
        for (GenerationError problem : context.getProblems()) {
            if (problem.getSeverity() == GenerationError.SEVERITY_ERROR) {
                result.getReport().addError(problem);
            } else {
                result.getReport().addWarning(problem);
            }
        }
    }

    /** Section 13 checks: every reference must resolve. */
    private void validateReferences(GenerationResult result) {
        Set<String> ids = new HashSet<>();
        Set<String> layoutNames = new HashSet<>(context.getGeneratedLayouts());
        Set<String> strings = new HashSet<>(
                context.getResources().strings.values());
        strings.add("app_name");
        Set<String> colors = new HashSet<>(
                context.getResources().colors.values());
        Set<String> dimens = new HashSet<>(
                context.getResources().dimens.values());
        Set<String> drawables = new HashSet<>();
        Set<String> styles = new HashSet<>();
        styles.add("AppTheme");
        for (String path : result.getAllPaths()) {
            if (path.endsWith(".xml") && path.contains("/layout/")) {
                String text = result.getTextFile(path);
                for (String id : findAll(text, "@\\+id/([A-Za-z0-9_]+)")) {
                    ids.add(id);
                }
            }
            if (path.contains("/drawable/") && path.endsWith(".xml")) {
                drawables.add(fileBaseName(path));
            }
            if (path.endsWith(".png") || path.endsWith(".jpg")
                    || path.endsWith(".jpeg") || path.endsWith(".webp")
                    || path.endsWith(".gif")) {
                drawables.add(fileBaseName(path));
            }
        }
        for (String path : result.getAllPaths()) {
            String text = result.getTextFile(path);
            if (text == null) {
                continue;
            }
            for (String ref : findAll(text, "@id/([A-Za-z0-9_]+)")) {
                if (!ids.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @id/" + ref);
                }
            }
            for (String ref : findAll(text, "@string/([A-Za-z0-9_]+)")) {
                if (!strings.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @string/" + ref);
                }
            }
            for (String ref : findAll(text, "@color/([A-Za-z0-9_]+)")) {
                if (!colors.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @color/" + ref);
                }
            }
            for (String ref : findAll(text, "@dimen/([A-Za-z0-9_]+)")) {
                if (!dimens.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @dimen/" + ref);
                }
            }
            for (String ref : findAll(text, "@drawable/([A-Za-z0-9_]+)")) {
                if (!drawables.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @drawable/" + ref);
                }
            }
            for (String ref : findAll(text, "@style/([A-Za-z0-9_]+)")) {
                if (!styles.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved @style/" + ref);
                }
            }
            for (String ref : findAll(text, "R\\.layout\\.([A-Za-z0-9_]+)")) {
                if (!layoutNames.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved R.layout." + ref);
                }
            }
            for (String ref : findAll(text, "R\\.id\\.([A-Za-z0-9_]+)")) {
                if (!ids.contains(ref)) {
                    context.addError("validate", path, null,
                            "unresolved R.id." + ref);
                }
            }
        }
        // Manifest activities must resolve to generated classes.
        for (ScreenModel screen : context.getDocument().getScreens()) {
            String className = context.activityClassFor(screen.getId());
            String javaPath = "app/src/main/java/"
                    + context.getPackageName().replace('.', '/') + "/"
                    + className + ".java";
            if (!result.hasFile(javaPath)) {
                context.addError("validate",
                        "app/src/main/AndroidManifest.xml", screen.getId(),
                        "manifest activity " + className
                                + " has no generated Java file");
            }
        }
    }

    private Set<String> findAll(String text, String regex) {
        Set<String> found = new HashSet<>();
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            found.add(matcher.group(1));
        }
        return found;
    }

    private String fileBaseName(String path) {
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }
}
