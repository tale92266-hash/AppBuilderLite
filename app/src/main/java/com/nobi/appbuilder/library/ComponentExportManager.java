package com.nobi.appbuilder.library;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

/**
 * Exports library items as a versioned JSON package. Only metadata
 * and default properties are exported — never Java source.
 */
public class ComponentExportManager {

    public static final int PACKAGE_VERSION = 1;

    private final Gson gson = new Gson();

    public String exportItems(List<ComponentLibraryItem> items) {
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", PACKAGE_VERSION);
        root.addProperty("kind", "component_library");
        JsonArray array = new JsonArray();
        for (ComponentLibraryItem item : items) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", item.getId());
            entry.addProperty("base_type", item.getBaseType());
            entry.addProperty("variant", item.getVariant());
            entry.addProperty("display_name", item.getDisplayName());
            entry.addProperty("category", item.getCategory().name());
            entry.addProperty("description", item.getDescription());
            JsonObject props = new JsonObject();
            for (java.util.Map.Entry<String, String> e
                    : item.getDefaultProperties().entrySet()) {
                props.addProperty(e.getKey(), e.getValue());
            }
            entry.add("default_properties", props);
            array.add(entry);
        }
        root.add("items", array);
        return gson.toJson(root);
    }
}
