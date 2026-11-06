package com.nobi.appbuilder.generator.java;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.mapping.ComponentMappingRegistry;
import com.nobi.appbuilder.generator.templates.ActivityTemplate;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Generates one Activity Java file per screen from the deterministic
 * ActivityTemplate: computed imports, view fields, findViewById
 * bindings and event listeners. The result is structurally validated
 * by JavaSourceValidator before it is accepted.
 */
public class ActivityGenerator {

    private final GenerationContext context;
    private final ViewBindingGenerator bindings = new ViewBindingGenerator();
    private final EventCodeGenerator events;

    public ActivityGenerator(GenerationContext context) {
        this.context = context;
        this.events = new EventCodeGenerator(context);
    }

    /** Relative path of a screen's Activity file. */
    public String activityPathFor(ScreenModel screen) {
        String packagePath = context.getPackageName().replace('.', '/');
        return "app/src/main/java/" + packagePath + "/"
                + context.activityClassFor(screen.getId()) + ".java";
    }

    public String generateActivity(ScreenModel screen,
                                   Map<String, String> resolvedIds,
                                   Set<String> knownIds,
                                   Set<String> knownLayouts) {
        String className = context.activityClassFor(screen.getId());
        String layoutName = context.layoutNameFor(screen.getId());
        ComponentModel root = screen.getRootComponent();
        Map<String, String> bindingMap = root == null ? new LinkedHashMap<>()
                : bindings.collectBindings(root);

        String fields = bindings.fieldsFor(bindingMap);
        String bindingLines = bindings.bindingsFor(bindingMap, resolvedIds);
        StringBuilder listeners = new StringBuilder();
        if (root != null) {
            collectListeners(root, bindingMap, resolvedIds, listeners);
        }

        Set<String> imports = new LinkedHashSet<>();
        imports.add("import android.os.Bundle;");
        imports.add("import androidx.appcompat.app.AppCompatActivity;");
        imports.add("import android.view.View;");
        imports.add("import android.widget.Button;");
        imports.add("import android.widget.TextView;");
        boolean needsIntent = false;
        boolean needsToast = false;
        boolean needsUri = false;
        boolean needsTextWatcher = false;
        for (Map.Entry<String, String> entry : bindingMap.entrySet()) {
            String importLine = ComponentMappingRegistry.importFor(entry.getValue());
            if (importLine != null) {
                imports.add(importLine);
            }
        }
        if (root != null) {
            needsIntent = treeUses(root, "navigate", "openUrl");
            needsToast = treeUses(root, "toast", "showToast", "validateInput");
            needsUri = treeUses(root, "openUrl");
            needsTextWatcher = treeHasTrigger(root, "text_changed");
        }
        if (needsIntent) {
            imports.add("import android.content.Intent;");
        }
        if (needsToast) {
            imports.add("import android.widget.Toast;");
        }
        if (needsUri) {
            imports.add("import android.net.Uri;");
        }
        if (needsTextWatcher) {
            imports.add("import android.text.Editable;");
            imports.add("import android.text.TextWatcher;");
        }
        StringBuilder importBlock = new StringBuilder();
        for (String line : imports) {
            importBlock.append(line).append("\n");
        }
        // Trim the trailing newline; the template adds its own blank line.
        if (importBlock.length() > 0) {
            importBlock.setLength(importBlock.length() - 1);
        }

        String source = ActivityTemplate.render(
                context.getPackageName(), className, importBlock.toString(),
                fields, layoutName, bindingLines, listeners.toString());

        String path = activityPathFor(screen);
        for (String problem : JavaSourceValidator.validate(
                context.getPackageName(), className, source, knownIds,
                knownLayouts)) {
            context.addError("java", path, screen.getId(), problem);
        }
        return source;
    }

    private void collectListeners(ComponentModel model,
                                  Map<String, String> bindingMap,
                                  Map<String, String> resolvedIds,
                                  StringBuilder out) {
        if (bindingMap.containsKey(model.getId())) {
            String field = com.nobi.appbuilder.generator.xml.XMLIdResolver
                    .fieldNameFor(model.getId());
            out.append(events.listenersFor(model, field,
                    resolvedIds.get(model.getId())));
        }
        for (ComponentModel child : model.getChildren()) {
            collectListeners(child, bindingMap, resolvedIds, out);
        }
    }

    private boolean treeUses(ComponentModel model, String... actions) {
        for (com.nobi.appbuilder.model.EventModel event : model.getEvents()) {
            for (String action : actions) {
                if (action.equals(event.getAction())) {
                    return true;
                }
            }
        }
        for (ComponentModel child : model.getChildren()) {
            if (treeUses(child, actions)) {
                return true;
            }
        }
        return false;
    }

    private boolean treeHasTrigger(ComponentModel model, String trigger) {
        for (com.nobi.appbuilder.model.EventModel event : model.getEvents()) {
            if (trigger.equals(event.getEventType())) {
                return true;
            }
        }
        for (ComponentModel child : model.getChildren()) {
            if (treeHasTrigger(child, trigger)) {
                return true;
            }
        }
        return false;
    }
}
