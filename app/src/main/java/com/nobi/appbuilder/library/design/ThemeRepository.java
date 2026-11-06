package com.nobi.appbuilder.library.design;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite store for project themes. */
public class ThemeRepository {

    private final DatabaseHelper dbHelper;

    public ThemeRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(ThemeDefinition theme) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", theme.getId());
        values.put("project_id", theme.getProjectId());
        values.put("name", theme.getName());
        values.put("is_dark", theme.isDarkMode() ? 1 : 0);
        values.put("primary_color", theme.getPrimaryColor());
        values.put("primary_dark_color", theme.getPrimaryDarkColor());
        values.put("accent_color", theme.getAccentColor());
        values.put("background_color", theme.getBackgroundColor());
        values.put("surface_color", theme.getSurfaceColor());
        values.put("text_color", theme.getTextColor());
        values.put("created_at", theme.getCreatedAt());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_PROJECT_THEMES,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public ThemeDefinition getById(String id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECT_THEMES,
                null, "id = ?", new String[]{id}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public List<ThemeDefinition> getByProject(String projectId) {
        List<ThemeDefinition> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECT_THEMES,
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
        return db.delete(DatabaseHelper.TABLE_PROJECT_THEMES,
                "id = ?", new String[]{id}) > 0;
    }

    private ThemeDefinition fromCursor(Cursor cursor) {
        ThemeDefinition theme = new ThemeDefinition();
        theme.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        theme.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        theme.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        theme.setDarkMode(cursor.getInt(cursor.getColumnIndexOrThrow("is_dark")) == 1);
        theme.setPrimaryColor(cursor.getString(
                cursor.getColumnIndexOrThrow("primary_color")));
        theme.setPrimaryDarkColor(cursor.getString(
                cursor.getColumnIndexOrThrow("primary_dark_color")));
        theme.setAccentColor(cursor.getString(
                cursor.getColumnIndexOrThrow("accent_color")));
        theme.setBackgroundColor(cursor.getString(
                cursor.getColumnIndexOrThrow("background_color")));
        theme.setSurfaceColor(cursor.getString(
                cursor.getColumnIndexOrThrow("surface_color")));
        theme.setTextColor(cursor.getString(
                cursor.getColumnIndexOrThrow("text_color")));
        theme.setCreatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("created_at")));
        return theme;
    }
}
