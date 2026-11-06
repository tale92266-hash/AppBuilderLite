package com.nobi.appbuilder.ir;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * Schema versioning for IR documents. Today only version 1 exists, but
 * every document passes through here so a future version can migrate
 * instead of breaking. Two shapes are understood:
 * - v1 envelope: { "schemaVersion": 1, "project": {...}, "screens": [...] }
 * - legacy Phase 1 project JSON: { "appName": ..., "packageName": ...,
 *   "screens": [...] } — wrapped into a v1 envelope, nothing dropped.
 * Unknown fields are never removed; they survive inside extraFields.
 */
public final class IRVersionManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private IRVersionManager() {
    }

    public static int currentVersion() {
        return IRConstants.CURRENT_SCHEMA_VERSION;
    }

    /** Schema version declared by a raw JSON object (1 when absent). */
    public static int declaredVersion(JsonObject root) {
        if (root == null || !root.has(IRConstants.KEY_SCHEMA_VERSION)) {
            return 1;
        }
        try {
            return root.get(IRConstants.KEY_SCHEMA_VERSION).getAsInt();
        } catch (Exception e) {
            return 1;
        }
    }

    /** True when the JSON is a legacy Phase 1 project document. */
    public static boolean isLegacyProjectJson(JsonObject root) {
        return root != null
                && !root.has(IRConstants.KEY_PROJECT)
                && root.has("appName")
                && root.has("packageName");
    }

    /**
     * Migrates raw JSON text to the current schema and returns the
     * migrated JSON text. Throws IllegalArgumentException when the text
     * is not JSON, not an object, or declares a newer schema than this
     * build understands.
     */
    public static String migrate(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("Document is empty.");
        }
        JsonObject root;
        try {
            JsonElement parsed = JsonParser.parseString(json);
            if (!parsed.isJsonObject()) {
                throw new IllegalArgumentException("Document must be a JSON object.");
            }
            root = parsed.getAsJsonObject();
        } catch (JsonSyntaxException e) {
            throw new IllegalArgumentException("Document is not valid JSON: " + e.getMessage());
        }

        if (isLegacyProjectJson(root)) {
            root = wrapLegacyProject(root);
        }

        int version = declaredVersion(root);
        if (version > IRConstants.CURRENT_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Document schema version " + version
                            + " is newer than supported version "
                            + IRConstants.CURRENT_SCHEMA_VERSION + ".");
        }
        if (version < IRConstants.MIN_SUPPORTED_SCHEMA_VERSION) {
            throw new IllegalArgumentException(
                    "Document schema version " + version + " is no longer supported.");
        }
        root.addProperty(IRConstants.KEY_SCHEMA_VERSION,
                IRConstants.CURRENT_SCHEMA_VERSION);
        return GSON.toJson(root);
    }

    /** Brings an in-memory document up to the current schema. */
    public static void migrateDocument(IRDocument document) {
        if (document == null) {
            return;
        }
        document.setSchemaVersion(IRConstants.CURRENT_SCHEMA_VERSION);
        document.normalize();
    }

    /**
     * Wraps a legacy Phase 1 project object into a v1 envelope. The
     * project's own "screens" array moves to the envelope level; every
     * other legacy field stays inside "project" untouched.
     */
    private static JsonObject wrapLegacyProject(JsonObject legacy) {
        JsonObject envelope = new JsonObject();
        envelope.addProperty(IRConstants.KEY_SCHEMA_VERSION,
                IRConstants.CURRENT_SCHEMA_VERSION);
        JsonObject project = legacy.deepCopy();
        JsonElement screens = project.remove(IRConstants.KEY_SCREENS);
        envelope.add(IRConstants.KEY_PROJECT, project);
        envelope.add(IRConstants.KEY_SCREENS,
                screens != null && screens.isJsonArray() ? screens : new JsonArray());
        envelope.add(IRConstants.KEY_THEME, new JsonObject());
        envelope.add(IRConstants.KEY_ASSETS, new JsonArray());
        envelope.add(IRConstants.KEY_EDITOR_METADATA, new JsonObject());
        return envelope;
    }
}
