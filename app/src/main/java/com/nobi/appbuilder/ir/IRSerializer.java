package com.nobi.appbuilder.ir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.model.ThemeModel;

import java.util.Map;

/**
 * Turns models into pretty-printed JSON. Null inputs serialize to "{}"
 * instead of crashing; unknown top-level fields kept in
 * IRDocument.extraFields are merged back into the output so a
 * serialize/deserialize round-trip never loses user data.
 */
public final class IRSerializer {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private IRSerializer() {
    }

    /** Serializes a whole document, preserving its extra fields. */
    public static String serializeDocument(IRDocument document) {
        if (document == null) {
            return "{}";
        }
        JsonObject tree = GSON.toJsonTree(document).getAsJsonObject();
        tree.remove("extraFields");
        for (Map.Entry<String, com.google.gson.JsonElement> entry
                : document.getExtraFields().entrySet()) {
            if (!tree.has(entry.getKey())) {
                tree.add(entry.getKey(), entry.getValue());
            }
        }
        return GSON.toJson(tree);
    }

    /** Serializes a project (with its screens) as a document. */
    public static String serializeProject(ProjectModel project) {
        if (project == null) {
            return "{}";
        }
        return serializeDocument(IRDocument.fromProject(project));
    }

    /** Serializes one screen (its design JSON is embedded as a string). */
    public static String serializeScreen(ScreenModel screen) {
        if (screen == null) {
            return "{}";
        }
        return GSON.toJson(screen);
    }

    /** Serializes one component subtree. */
    public static String serializeComponent(ComponentModel component) {
        if (component == null) {
            return "{}";
        }
        return GSON.toJson(component);
    }

    /** Serializes a theme configuration. */
    public static String serializeTheme(ThemeModel theme) {
        if (theme == null) {
            return "{}";
        }
        return GSON.toJson(theme);
    }
}
