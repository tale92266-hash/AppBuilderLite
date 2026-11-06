package com.nobi.appbuilder.library.custom;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/** Versioned JSON (de)serialization for custom component definitions. */
public final class CustomComponentSerializer {

    public static final int SCHEMA_VERSION = 1;

    private static final Gson GSON = new Gson();

    private CustomComponentSerializer() {
    }

    public static String toJson(CustomComponentDefinition definition) {
        return GSON.toJson(definition);
    }

    public static CustomComponentDefinition fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(json, CustomComponentDefinition.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }
}
