package com.nobi.appbuilder.library.templates;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Imports a versioned screen-template JSON package. Validates the
 * schema version and the screen content; a bad template is reported,
 * never silently applied.
 */
public class TemplateImportManager {

    private final Gson gson = new Gson();

    public TemplateDefinition importJson(String json, String projectId,
                                         List<String> errors) {
        JsonObject root;
        try {
            root = gson.fromJson(json, JsonObject.class);
        } catch (JsonSyntaxException e) {
            errors.add("Invalid JSON: " + e.getMessage());
            return null;
        }
        if (root == null || !root.has("schema_version")
                || !root.has("screen_json")) {
            errors.add("Not a screen template package");
            return null;
        }
        int version = root.get("schema_version").getAsInt();
        if (version != TemplateExportManager.PACKAGE_VERSION) {
            errors.add("Unsupported template version: " + version);
            return null;
        }
        TemplateDefinition template = new TemplateDefinition();
        template.setId(IDGenerator.newId());
        template.setProjectId(projectId);
        template.setName(opt(root, "name"));
        template.setCategory(TemplateCategory.fromName(opt(root, "category")));
        template.setDescription(opt(root, "description"));
        template.setScreenName(opt(root, "screen_name"));
        template.setScreenJson(opt(root, "screen_json"));
        template.setVersion(1);
        template.setBuiltIn(false);
        template.setCreatedAt(System.currentTimeMillis());
        List<String> problems = TemplateValidator.validate(template);
        if (!problems.isEmpty()) {
            errors.addAll(problems);
            return null;
        }
        return template;
    }

    private static String opt(JsonObject object, String key) {
        if (!object.has(key) || object.get(key).isJsonNull()) {
            return null;
        }
        return object.get(key).getAsString();
    }
}
