package com.nobi.appbuilder.library.custom;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite store for custom components and their versions. Every save
 * writes a new row to custom_component_versions so an older
 * definition can be restored.
 */
public class CustomComponentRepository {

    private final DatabaseHelper dbHelper;

    public CustomComponentRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(CustomComponentDefinition definition) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = toValues(definition);
            long row = db.insertWithOnConflict(
                    DatabaseHelper.TABLE_CUSTOM_COMPONENTS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE);
            if (row < 0) {
                return false;
            }
            ContentValues version = new ContentValues();
            version.put("component_id", definition.getId());
            version.put("project_id", definition.getProjectId());
            version.put("version", definition.getVersion());
            version.put("definition_json",
                    CustomComponentSerializer.toJson(definition));
            version.put("created_at", System.currentTimeMillis());
            db.insert(DatabaseHelper.TABLE_CUSTOM_COMPONENT_VERSIONS,
                    null, version);
            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public CustomComponentDefinition getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOM_COMPONENTS,
                null, "id = ?", new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public List<CustomComponentDefinition> getByProject(String projectId) {
        List<CustomComponentDefinition> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_CUSTOM_COMPONENTS,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public boolean delete(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_CUSTOM_COMPONENT_VERSIONS,
                "component_id = ?", new String[]{id});
        return db.delete(DatabaseHelper.TABLE_CUSTOM_COMPONENTS,
                "id = ?", new String[]{id}) > 0;
    }

    public List<CustomComponentDefinition> getVersions(String componentId) {
        List<CustomComponentDefinition> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_CUSTOM_COMPONENT_VERSIONS, null,
                "component_id = ?", new String[]{componentId}, null, null,
                "version DESC")) {
            while (cursor.moveToNext()) {
                CustomComponentDefinition definition =
                        CustomComponentSerializer.fromJson(cursor.getString(
                                cursor.getColumnIndexOrThrow("definition_json")));
                if (definition != null) {
                    out.add(definition);
                }
            }
        }
        return out;
    }

    private ContentValues toValues(CustomComponentDefinition definition) {
        ContentValues values = new ContentValues();
        values.put("id", definition.getId());
        values.put("project_id", definition.getProjectId());
        values.put("name", definition.getName());
        values.put("description", definition.getDescription());
        values.put("root_type", definition.getRootType());
        values.put("components_json", definition.getComponentsJson());
        values.put("public_properties_json",
                new com.google.gson.Gson().toJson(
                        definition.getPublicProperties()));
        values.put("version", definition.getVersion());
        values.put("created_at", definition.getCreatedAt());
        values.put("updated_at", definition.getUpdatedAt());
        return values;
    }

    private CustomComponentDefinition fromCursor(Cursor cursor) {
        CustomComponentDefinition definition = new CustomComponentDefinition();
        definition.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        definition.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        definition.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        definition.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow("description")));
        definition.setRootType(cursor.getString(
                cursor.getColumnIndexOrThrow("root_type")));
        definition.setComponentsJson(cursor.getString(
                cursor.getColumnIndexOrThrow("components_json")));
        String propsJson = cursor.getString(cursor.getColumnIndexOrThrow(
                "public_properties_json"));
        if (propsJson != null && !propsJson.isEmpty()) {
            try {
                java.lang.reflect.Type listType =
                        new com.google.gson.reflect.TypeToken<
                                List<CustomComponentDefinition.PublicProperty>>() {
                        }.getType();
                definition.setPublicProperties(
                        new com.google.gson.Gson().fromJson(propsJson, listType));
            } catch (Exception ignored) {
                definition.setPublicProperties(new ArrayList<CustomComponentDefinition.PublicProperty>());
            }
        }
        definition.setVersion(cursor.getInt(
                cursor.getColumnIndexOrThrow("version")));
        definition.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")));
        definition.setUpdatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")));
        return definition;
    }
}
