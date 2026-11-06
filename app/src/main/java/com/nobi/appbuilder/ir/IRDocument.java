package com.nobi.appbuilder.ir;

import com.google.gson.JsonElement;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.model.ThemeModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The whole project as one JSON document — the single source of truth.
 * Envelope: schemaVersion, project, screens, theme, assets, editorMetadata.
 * Unknown top-level keys from newer or foreign documents are kept in
 * extraFields and written back verbatim by IRSerializer, so a round-trip
 * never silently discards user data.
 */
public class IRDocument {

    /** One asset reference inside a document. */
    public static class AssetRef {
        private String id;
        private String name;
        private String type;
        private String path;
        private long sizeBytes;

        public AssetRef() {
        }

        public AssetRef(String id, String name, String type, String path, long sizeBytes) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.path = path;
            this.sizeBytes = sizeBytes;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public long getSizeBytes() {
            return sizeBytes;
        }

        public void setSizeBytes(long sizeBytes) {
            this.sizeBytes = sizeBytes;
        }
    }

    private int schemaVersion;
    private ProjectModel project;
    private List<ScreenModel> screens;
    private ThemeModel theme;
    private List<AssetRef> assets;
    private Map<String, String> editorMetadata;
    private Map<String, JsonElement> extraFields;

    public IRDocument() {
        schemaVersion = IRConstants.CURRENT_SCHEMA_VERSION;
        screens = new ArrayList<>();
        theme = new ThemeModel();
        assets = new ArrayList<>();
        editorMetadata = new LinkedHashMap<>();
        extraFields = new LinkedHashMap<>();
    }

    /** Builds a document from a project loaded from the database. */
    public static IRDocument fromProject(ProjectModel project) {
        IRDocument document = new IRDocument();
        document.project = project;
        if (project != null && project.getScreens() != null) {
            document.screens = new ArrayList<>(project.getScreens());
        }
        return document;
    }

    /** The project inside this document, with its screens attached. */
    public ProjectModel toProject() {
        if (project == null) {
            return null;
        }
        project.setScreens(screens == null ? new ArrayList<>() : new ArrayList<>(screens));
        return project;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public ProjectModel getProject() {
        return project;
    }

    public void setProject(ProjectModel project) {
        this.project = project;
    }

    public List<ScreenModel> getScreens() {
        if (screens == null) {
            screens = new ArrayList<>();
        }
        return screens;
    }

    public void setScreens(List<ScreenModel> screens) {
        this.screens = screens == null ? new ArrayList<>() : screens;
    }

    public ScreenModel getScreen(String screenId) {
        if (screenId == null) {
            return null;
        }
        for (ScreenModel screen : getScreens()) {
            if (screenId.equals(screen.getId())) {
                return screen;
            }
        }
        return null;
    }

    public ThemeModel getTheme() {
        if (theme == null) {
            theme = new ThemeModel();
        }
        return theme;
    }

    public void setTheme(ThemeModel theme) {
        this.theme = theme;
    }

    public List<AssetRef> getAssets() {
        if (assets == null) {
            assets = new ArrayList<>();
        }
        return assets;
    }

    public void setAssets(List<AssetRef> assets) {
        this.assets = assets == null ? new ArrayList<>() : assets;
    }

    public Map<String, String> getEditorMetadata() {
        if (editorMetadata == null) {
            editorMetadata = new LinkedHashMap<>();
        }
        return editorMetadata;
    }

    public void setEditorMetadata(Map<String, String> editorMetadata) {
        this.editorMetadata = editorMetadata == null
                ? new LinkedHashMap<>() : editorMetadata;
    }

    public Map<String, JsonElement> getExtraFields() {
        if (extraFields == null) {
            extraFields = new LinkedHashMap<>();
        }
        return extraFields;
    }

    public void setExtraFields(Map<String, JsonElement> extraFields) {
        this.extraFields = extraFields == null
                ? new LinkedHashMap<>() : extraFields;
    }

    /**
     * Repairs null collections / missing theme after Gson deserialization
     * (Gson writes null over constructor defaults when the JSON says null).
     */
    public void normalize() {
        if (screens == null) {
            screens = new ArrayList<>();
        }
        if (theme == null) {
            theme = new ThemeModel();
        }
        if (assets == null) {
            assets = new ArrayList<>();
        }
        if (editorMetadata == null) {
            editorMetadata = new LinkedHashMap<>();
        }
        if (extraFields == null) {
            extraFields = new LinkedHashMap<>();
        }
        if (schemaVersion <= 0) {
            schemaVersion = IRConstants.CURRENT_SCHEMA_VERSION;
        }
        for (ScreenModel screen : screens) {
            if (screen != null && screen.getDesignJson() != null) {
                screen.getRootComponent();
            }
        }
    }
}
