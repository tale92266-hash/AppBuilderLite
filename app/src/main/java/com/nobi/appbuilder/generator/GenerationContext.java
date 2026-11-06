package com.nobi.appbuilder.generator;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.generator.xml.XMLIdResolver;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared state for one generation run: the document, the project
 * settings that shape the output (package, SDKs, version), the
 * screen-id -&gt; Activity-class map, the id resolver, the shared
 * resource collector (so colours, strings and dimens are deduplicated
 * across every layout), and the problem list.
 */
public class GenerationContext {

    /** Collected resources, shared by every generator stage. */
    public static class ResourceCollector {
        // value -> resource name, insertion ordered
        public final Map<String, String> colors = new LinkedHashMap<>();
        public final Map<String, String> strings = new LinkedHashMap<>();
        public final Map<String, String> dimens = new LinkedHashMap<>();
        // component id -> shape drawable file name
        public final Map<String, String> shapes = new LinkedHashMap<>();
        // component id -> shape parameters for DrawableGenerator
        public final Map<String, ShapeSpec> shapeSpecs = new LinkedHashMap<>();

        /** Parameters of one generated shape drawable. */
        public static class ShapeSpec {
            public final String color;
            public final float radiusDp;
            public final float borderWidthDp;
            public final String borderColor;

            public ShapeSpec(String color, float radiusDp, float borderWidthDp,
                             String borderColor) {
                this.color = color;
                this.radiusDp = radiusDp;
                this.borderWidthDp = borderWidthDp;
                this.borderColor = borderColor;
            }
        }

        public String colorFor(String hexValue, String preferredName) {
            String key = hexValue.toUpperCase();
            String existing = colors.get(key);
            if (existing != null) {
                return existing;
            }
            String name = sanitizeResourceName(preferredName);
            if (colors.containsValue(name)) {
                name = name + "_" + colors.size();
            }
            colors.put(key, name);
            return name;
        }

        public String stringFor(String value, String preferredName) {
            String existing = strings.get(value);
            if (existing != null) {
                return existing;
            }
            String name = sanitizeResourceName(preferredName);
            if (strings.containsValue(name)) {
                name = name + "_" + strings.size();
            }
            strings.put(value, name);
            return name;
        }

        public String dimenFor(String value, String preferredName) {
            String existing = dimens.get(value);
            if (existing != null) {
                return existing;
            }
            String name = sanitizeResourceName(preferredName);
            if (dimens.containsValue(name)) {
                name = name + "_" + dimens.size();
            }
            dimens.put(value, name);
            return name;
        }

        /** Android resource names: lowercase letters, digits, _. */
        public static String sanitizeResourceName(String raw) {
            if (raw == null || raw.isEmpty()) {
                return "value";
            }
            StringBuilder out = new StringBuilder();
            for (int i = 0; i < raw.length(); i++) {
                char c = raw.charAt(i);
                if ((c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_') {
                    out.append(c);
                } else if (c >= 'A' && c <= 'Z') {
                    out.append(Character.toLowerCase(c));
                } else {
                    out.append('_');
                }
            }
            if (out.length() == 0) {
                return "value";
            }
            if (out.charAt(0) >= '0' && out.charAt(0) <= '9') {
                out.insert(0, '_');
            }
            return out.toString();
        }
    }

    private final IRDocument document;
    private final ProjectModel project;
    private final String packageName;
    private final int minSdk;
    private final int targetSdk;
    private final int versionCode;
    private final String versionName;
    private final AssetRepository assetRepository;

    private final XMLIdResolver idResolver = new XMLIdResolver();
    private final ResourceCollector resources = new ResourceCollector();
    private final List<GenerationError> problems = new ArrayList<>();
    private final Map<String, String> activityClassByScreenId = new LinkedHashMap<>();
    private final Map<String, String> layoutFileByScreenId = new LinkedHashMap<>();
    /** component id -> generated android:id, recorded by the XML stage. */
    private final Map<String, String> resolvedIds = new LinkedHashMap<>();
    /** generated layout file names (without .xml), recorded by XML stage. */
    private final java.util.Set<String> generatedLayouts =
            new java.util.LinkedHashSet<>();

    public GenerationContext(IRDocument document, ProjectModel project,
                             String packageName, int minSdk, int targetSdk,
                             int versionCode, String versionName,
                             AssetRepository assetRepository) {
        this.document = document;
        this.project = project;
        this.packageName = packageName;
        this.minSdk = minSdk;
        this.targetSdk = targetSdk;
        this.versionCode = versionCode;
        this.versionName = versionName;
        this.assetRepository = assetRepository;
        for (ScreenModel screen : document.getScreens()) {
            String className = XMLIdResolver.activityClassFor(screen.getId());
            activityClassByScreenId.put(screen.getId(), className);
            layoutFileByScreenId.put(screen.getId(),
                    "activity_" + XMLIdResolver.sanitizeId(screen.getId()));
        }
    }

    public IRDocument getDocument() {
        return document;
    }

    public ProjectModel getProject() {
        return project;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public AssetRepository getAssetRepository() {
        return assetRepository;
    }

    public XMLIdResolver getIdResolver() {
        return idResolver;
    }

    public ResourceCollector getResources() {
        return resources;
    }

    public String activityClassFor(String screenId) {
        return activityClassByScreenId.get(screenId);
    }

    public String layoutFileFor(String screenId) {
        return layoutFileByScreenId.get(screenId);
    }

    /** Layout resource name without the .xml (R.layout.*). */
    public String layoutNameFor(String screenId) {
        return layoutFileByScreenId.get(screenId);
    }

    public void recordResolvedId(String componentId, String generatedId) {
        resolvedIds.put(componentId, generatedId);
    }

    public Map<String, String> getResolvedIds() {
        return resolvedIds;
    }

    public void recordLayout(String layoutName) {
        generatedLayouts.add(layoutName);
    }

    public java.util.Set<String> getGeneratedLayouts() {
        return generatedLayouts;
    }

    public void addProblem(GenerationError problem) {
        problems.add(problem);
    }

    public void addError(String stage, String file, String componentId,
                         String message) {
        problems.add(GenerationError.error(stage, file, componentId, message));
    }

    public void addWarning(String stage, String file, String componentId,
                           String message) {
        problems.add(GenerationError.warning(stage, file, componentId, message));
    }

    public List<GenerationError> getProblems() {
        return new ArrayList<>(problems);
    }

    /** True when any openUrl event exists (drives INTERNET permission). */
    public boolean needsInternet() {
        for (ScreenModel screen : document.getScreens()) {
            if (screen.getRootComponent() == null) {
                continue;
            }
            if (treeNeedsInternet(screen.getRootComponent())) {
                return true;
            }
        }
        return false;
    }

    private boolean treeNeedsInternet(com.nobi.appbuilder.model.ComponentModel model) {
        for (com.nobi.appbuilder.model.EventModel event : model.getEvents()) {
            if ("openUrl".equals(event.getAction())) {
                return true;
            }
        }
        for (com.nobi.appbuilder.model.ComponentModel child : model.getChildren()) {
            if (treeNeedsInternet(child)) {
                return true;
            }
        }
        return false;
    }
}
