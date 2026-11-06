package com.nobi.appbuilder.library.presets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** SQLite store for component presets. */
public class PresetRepository {

    private final DatabaseHelper dbHelper;
    private final Gson gson = new Gson();

    public PresetRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(ComponentPreset preset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", preset.getId());
        values.put("project_id", preset.getProjectId());
        values.put("name", preset.getName());
        values.put("component_type", preset.getComponentType());
        values.put("properties_json", gson.toJson(preset.getProperties()));
        values.put("is_built_in", preset.isBuiltIn() ? 1 : 0);
        values.put("created_at", preset.getCreatedAt());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_COMPONENT_PRESETS,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public List<ComponentPreset> getByProject(String projectId) {
        List<ComponentPreset> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_PRESETS,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public List<ComponentPreset> getByType(String projectId,
                                           String componentType) {
        List<ComponentPreset> out = new ArrayList<>();
        for (ComponentPreset preset : getByProject(projectId)) {
            if (componentType.equals(preset.getComponentType())) {
                out.add(preset);
            }
        }
        return out;
    }

    public boolean delete(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_COMPONENT_PRESETS,
                "id = ?", new String[]{id}) > 0;
    }

    private ComponentPreset fromCursor(Cursor cursor) {
        ComponentPreset preset = new ComponentPreset();
        preset.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        preset.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        preset.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        preset.setComponentType(cursor.getString(
                cursor.getColumnIndexOrThrow("component_type")));
        String json = cursor.getString(
                cursor.getColumnIndexOrThrow("properties_json"));
        Map<String, String> props = new LinkedHashMap<>();
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<Map<String, String>>() {
                }.getType();
                Map<String, String> parsed = gson.fromJson(json, type);
                if (parsed != null) {
                    props.putAll(parsed);
                }
            } catch (Exception ignored) {
            }
        }
        preset.setProperties(props);
        preset.setBuiltIn(cursor.getInt(
                cursor.getColumnIndexOrThrow("is_built_in")) == 1);
        preset.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")));
        return preset;
    }
}
