package com.nobi.appbuilder.assets.vector;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Facade for vector assets: validate, save (JSON in SQLite),
 * list, delete, and generate the VectorDrawable XML for export.
 */
public class VectorAssetManager {

    private final DatabaseHelper dbHelper;
    private final Gson gson = new Gson();

    public VectorAssetManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(VectorAssetModel model) {
        if (!VectorValidator.validate(model).isEmpty()) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", model.getId());
        values.put("project_id", model.getProjectId());
        values.put("name", model.getName());
        values.put("viewport_width", model.getViewportWidth());
        values.put("viewport_height", model.getViewportHeight());
        values.put("width_dp", model.getWidthDp());
        values.put("height_dp", model.getHeightDp());
        values.put("paths_json", gson.toJson(model.getPaths()));
        values.put("created_at", model.getCreatedAt());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_VECTOR_ASSETS,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public List<VectorAssetModel> getByProject(String projectId) {
        List<VectorAssetModel> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_VECTOR_ASSETS,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public VectorAssetModel getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_VECTOR_ASSETS,
                null, "id = ?", new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public boolean delete(String id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_VECTOR_ASSETS,
                "id = ?", new String[]{id}) > 0;
    }

    public String toXml(VectorAssetModel model) {
        return VectorXMLWriter.toXml(model);
    }

    private VectorAssetModel fromCursor(Cursor cursor) {
        VectorAssetModel model = new VectorAssetModel();
        model.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        model.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        model.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        model.setViewportWidth(cursor.getFloat(
                cursor.getColumnIndexOrThrow("viewport_width")));
        model.setViewportHeight(cursor.getFloat(
                cursor.getColumnIndexOrThrow("viewport_height")));
        model.setWidthDp(cursor.getFloat(
                cursor.getColumnIndexOrThrow("width_dp")));
        model.setHeightDp(cursor.getFloat(
                cursor.getColumnIndexOrThrow("height_dp")));
        String json = cursor.getString(
                cursor.getColumnIndexOrThrow("paths_json"));
        if (json != null && !json.isEmpty()) {
            try {
                Type type = new TypeToken<List<VectorPathModel>>() {
                }.getType();
                List<VectorPathModel> paths = gson.fromJson(json, type);
                if (paths != null) {
                    model.setPaths(paths);
                }
            } catch (Exception ignored) {
            }
        }
        model.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")));
        return model;
    }
}
