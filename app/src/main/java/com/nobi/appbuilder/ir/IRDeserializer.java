package com.nobi.appbuilder.ir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.model.ThemeModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads JSON into models. Every entry point migrates first (legacy Phase 1
 * documents are wrapped, not rejected), then validates; anything invalid
 * fails with IllegalArgumentException carrying the first problem.
 * Missing optional properties fall back to model defaults, and null
 * collections are repaired by normalizeComponent().
 */
public final class IRDeserializer {

    private static final Gson GSON = new GsonBuilder().create();

    private IRDeserializer() {
    }

    /** Loads a whole document; throws IllegalArgumentException if invalid. */
    public static IRDocument deserializeDocument(String json) {
        String migrated = IRVersionManager.migrate(json);
        JsonObject root = JsonParser.parseString(migrated).getAsJsonObject();
        IRDocument document = GSON.fromJson(root, IRDocument.class);
        if (document == null) {
            throw new IllegalArgumentException("Document could not be read.");
        }
        document.normalize();
        collectExtraFields(root, document);
        if (document.getProject() == null) {
            throw new IllegalArgumentException("Document has no project.");
        }
        List<String> errors = IRValidator.validateDocument(document);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0)
                    + (errors.size() > 1 ? " (+" + (errors.size() - 1) + " more)" : ""));
        }
        return document;
    }

    /** Loads one screen; throws IllegalArgumentException if invalid. */
    public static ScreenModel deserializeScreen(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Screen JSON is empty.");
        }
        ScreenModel screen = GSON.fromJson(json, ScreenModel.class);
        if (screen == null) {
            throw new IllegalArgumentException("Screen could not be read.");
        }
        List<String> errors = IRValidator.validateScreen(screen);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }
        return screen;
    }

    /** Loads one component subtree; throws IllegalArgumentException if invalid. */
    public static ComponentModel deserializeComponent(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Component JSON is empty.");
        }
        ComponentModel component = GSON.fromJson(json, ComponentModel.class);
        if (component == null) {
            throw new IllegalArgumentException("Component could not be read.");
        }
        normalizeComponent(component);
        List<String> errors = IRValidator.validateComponentTree(component);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(errors.get(0));
        }
        return component;
    }

    /** Loads a theme; returns the default theme for empty input. */
    public static ThemeModel deserializeTheme(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ThemeModel();
        }
        ThemeModel theme = GSON.fromJson(json, ThemeModel.class);
        return theme == null ? new ThemeModel() : theme;
    }

    /**
     * Repairs null collections that Gson leaves behind when the JSON
     * explicitly says null (constructor defaults do not survive that).
     */
    public static void normalizeComponent(ComponentModel component) {
        if (component == null) {
            return;
        }
        if (component.getProperties() == null) {
            component.setProperties(new LinkedHashMap<String, String>());
        }
        if (component.getChildren() == null) {
            component.setChildren(new ArrayList<ComponentModel>());
        }
        if (component.getEvents() == null) {
            component.setEvents(new ArrayList<com.nobi.appbuilder.model.EventModel>());
        }
        if (component.getLayout() == null) {
            component.setLayout(new com.nobi.appbuilder.model.LayoutModel());
        }
        if (component.getEditorMetadata() == null) {
            component.setEditorMetadata(new LinkedHashMap<String, String>());
        }
        for (ComponentModel child : component.getChildren()) {
            normalizeComponent(child);
        }
    }

    /** Keeps unknown top-level keys so the serializer can write them back. */
    private static void collectExtraFields(JsonObject root, IRDocument document) {
        Map<String, JsonElement> extra = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
            String key = entry.getKey();
            if (!IRConstants.KEY_SCHEMA_VERSION.equals(key)
                    && !IRConstants.KEY_PROJECT.equals(key)
                    && !IRConstants.KEY_SCREENS.equals(key)
                    && !IRConstants.KEY_THEME.equals(key)
                    && !IRConstants.KEY_ASSETS.equals(key)
                    && !IRConstants.KEY_EDITOR_METADATA.equals(key)) {
                extra.put(key, entry.getValue());
            }
        }
        document.setExtraFields(extra);
    }
}
