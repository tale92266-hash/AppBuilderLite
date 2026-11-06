package com.nobi.appbuilder.library.templates;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/** Exports one template as a versioned JSON package. */
public class TemplateExportManager {

    public static final int PACKAGE_VERSION = 1;

    private final Gson gson = new Gson();

    public String exportTemplate(TemplateDefinition template) {
        JsonObject root = new JsonObject();
        root.addProperty("schema_version", PACKAGE_VERSION);
        root.addProperty("kind", "screen_template");
        root.addProperty("name", template.getName());
        root.addProperty("category", template.getCategory() == null
                ? TemplateCategory.EMPTY.name() : template.getCategory().name());
        root.addProperty("description", template.getDescription());
        root.addProperty("screen_name", template.getScreenName());
        root.addProperty("screen_json", template.getScreenJson());
        return gson.toJson(root);
    }
}
