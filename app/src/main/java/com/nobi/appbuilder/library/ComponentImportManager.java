package com.nobi.appbuilder.library;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.nobi.appbuilder.components.ComponentRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Imports a versioned component-library JSON package. Validates the
 * schema version, every base type and every property value type;
 * unknown types are reported, never silently added. Imported items
 * get fresh ids so nothing collides with the existing library.
 */
public class ComponentImportManager {

    public static class ImportResult {
        public final List<ComponentLibraryItem> items = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();

        public boolean isSuccess() {
            return !items.isEmpty() && errors.isEmpty();
        }
    }

    private final Gson gson = new Gson();

    public ImportResult importJson(String json) {
        ImportResult result = new ImportResult();
        JsonObject root;
        try {
            root = gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException e) {
            result.errors.add("Invalid JSON: " + e.getMessage());
            return result;
        }
        if (root == null || !root.has("schema_version")
                || !root.has("items")) {
            result.errors.add("Not a component library package");
            return result;
        }
        int version = root.get("schema_version").getAsInt();
        if (version != ComponentExportManager.PACKAGE_VERSION) {
            result.errors.add("Unsupported package version: " + version);
            return result;
        }
        JsonArray array = root.getAsJsonArray("items");
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                result.errors.add("Skipped a malformed item entry");
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            String baseType = opt(entry, "base_type");
            String displayName = opt(entry, "display_name");
            if (baseType == null || baseType.isEmpty()) {
                result.errors.add("Skipped an item with no base_type");
                continue;
            }
            if (!ComponentRegistry.contains(baseType)) {
                result.errors.add("Unknown component type: " + baseType);
                continue;
            }
            Map<String, String> props = new LinkedHashMap<>();
            if (entry.has("default_properties")
                    && entry.get("default_properties").isJsonObject()) {
                JsonObject propsJson =
                        entry.getAsJsonObject("default_properties");
                for (Map.Entry<String, JsonElement> e : propsJson.entrySet()) {
                    if (e.getValue().isJsonPrimitive()) {
                        props.put(e.getKey(), e.getValue().getAsString());
                    }
                }
            }
            result.items.add(new ComponentLibraryItem(
                    "imported-" + UUID.randomUUID().toString().substring(0, 8),
                    baseType, opt(entry, "variant"),
                    displayName == null ? baseType : displayName,
                    ComponentCategory.fromName(opt(entry, "category")),
                    opt(entry, "description") == null ? "" : opt(entry, "description"),
                    props, true, null, null));
        }
        return result;
    }

    private static String opt(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }
}
