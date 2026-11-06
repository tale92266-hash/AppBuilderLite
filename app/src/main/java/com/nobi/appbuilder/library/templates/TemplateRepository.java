package com.nobi.appbuilder.library.templates;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite store for personal screen templates (built-ins live in code). */
public class TemplateRepository {

    private final DatabaseHelper dbHelper;

    public TemplateRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(TemplateDefinition template) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", template.getId());
        values.put("project_id", template.getProjectId());
        values.put("name", template.getName());
        values.put("category", template.getCategory() == null
                ? TemplateCategory.EMPTY.name() : template.getCategory().name());
        values.put("description", template.getDescription());
        values.put("version", template.getVersion());
        values.put("screen_name", template.getScreenName());
        values.put("screen_json", template.getScreenJson());
        values.put("is_built_in", template.isBuiltIn() ? 1 : 0);
        values.put("created_at", template.getCreatedAt());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_SCREEN_TEMPLATES,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public TemplateDefinition getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SCREEN_TEMPLATES,
                null, "id = ?", new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public List<TemplateDefinition> getByProject(String projectId) {
        List<TemplateDefinition> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SCREEN_TEMPLATES,
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
        return db.delete(DatabaseHelper.TABLE_SCREEN_TEMPLATES,
                "id = ?", new String[]{id}) > 0;
    }

    private TemplateDefinition fromCursor(Cursor cursor) {
        TemplateDefinition template = new TemplateDefinition();
        template.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        template.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        template.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        template.setCategory(TemplateCategory.fromName(cursor.getString(
                cursor.getColumnIndexOrThrow("category"))));
        template.setDescription(cursor.getString(
                cursor.getColumnIndexOrThrow("description")));
        template.setVersion(cursor.getInt(
                cursor.getColumnIndexOrThrow("version")));
        template.setScreenName(cursor.getString(
                cursor.getColumnIndexOrThrow("screen_name")));
        template.setScreenJson(cursor.getString(
                cursor.getColumnIndexOrThrow("screen_json")));
        template.setBuiltIn(cursor.getInt(
                cursor.getColumnIndexOrThrow("is_built_in")) == 1);
        template.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")));
        return template;
    }
}
