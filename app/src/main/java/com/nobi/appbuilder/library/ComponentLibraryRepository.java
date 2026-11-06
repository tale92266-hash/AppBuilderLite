package com.nobi.appbuilder.library;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite store for library rows: component_library (catalog rows
 * and personal entries), component_favorites and component_usage.
 * Built-in catalog rows are seeded from code on first access.
 */
public class ComponentLibraryRepository {

    private final DatabaseHelper dbHelper;

    public ComponentLibraryRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean upsert(ComponentLibraryItem item, String projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", item.getId());
        values.put("project_id", projectId);
        values.put("base_type", item.getBaseType());
        values.put("variant", item.getVariant());
        values.put("display_name", item.getDisplayName());
        values.put("category", item.getCategory().name());
        values.put("description", item.getDescription());
        values.put("is_custom", item.isCustom() ? 1 : 0);
        values.put("is_supported", item.isSupported() ? 1 : 0);
        values.put("unsupported_reason", item.getUnsupportedReason());
        values.put("custom_component_id", item.getCustomComponentId());
        values.put("updated_at", System.currentTimeMillis());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_COMPONENT_LIBRARY,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public List<ComponentLibraryItem> getAll(String projectId) {
        List<ComponentLibraryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_LIBRARY, null,
                "project_id = ?", new String[]{projectId}, null, null,
                "display_name ASC")) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    public List<ComponentLibraryItem> getFavorites(String projectId) {
        List<ComponentLibraryItem> items = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sql = "SELECT l.* FROM " + DatabaseHelper.TABLE_COMPONENT_LIBRARY
                + " l JOIN " + DatabaseHelper.TABLE_COMPONENT_FAVORITES
                + " f ON l.id = f.item_id AND l.project_id = f.project_id"
                + " WHERE l.project_id = ? ORDER BY l.display_name ASC";
        try (Cursor cursor = db.rawQuery(sql, new String[]{projectId})) {
            while (cursor.moveToNext()) {
                items.add(fromCursor(cursor));
            }
        }
        return items;
    }

    public boolean setFavorite(String projectId, String itemId, boolean favorite) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (favorite) {
            ContentValues values = new ContentValues();
            values.put("project_id", projectId);
            values.put("item_id", itemId);
            values.put("created_at", System.currentTimeMillis());
            return db.insertWithOnConflict(DatabaseHelper.TABLE_COMPONENT_FAVORITES,
                    null, values, SQLiteDatabase.CONFLICT_IGNORE) >= 0;
        }
        return db.delete(DatabaseHelper.TABLE_COMPONENT_FAVORITES,
                "project_id = ? AND item_id = ?",
                new String[]{projectId, itemId}) >= 0;
    }

    public boolean isFavorite(String projectId, String itemId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_FAVORITES,
                new String[]{"item_id"}, "project_id = ? AND item_id = ?",
                new String[]{projectId, itemId}, null, null, null)) {
            return cursor.moveToFirst();
        }
    }

    public boolean delete(String projectId, String itemId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_COMPONENT_FAVORITES,
                "project_id = ? AND item_id = ?",
                new String[]{projectId, itemId});
        return db.delete(DatabaseHelper.TABLE_COMPONENT_LIBRARY,
                "project_id = ? AND id = ?",
                new String[]{projectId, itemId}) > 0;
    }

    private ComponentLibraryItem fromCursor(Cursor cursor) {
        ComponentLibraryItem item = new ComponentLibraryItem(
                cursor.getString(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("base_type")),
                cursor.getString(cursor.getColumnIndexOrThrow("variant")),
                cursor.getString(cursor.getColumnIndexOrThrow("display_name")),
                ComponentCategory.fromName(
                        cursor.getString(cursor.getColumnIndexOrThrow("category"))),
                cursor.getString(cursor.getColumnIndexOrThrow("description")),
                null,
                cursor.getInt(cursor.getColumnIndexOrThrow("is_supported")) == 1,
                cursor.getString(cursor.getColumnIndexOrThrow("unsupported_reason")),
                cursor.getString(cursor.getColumnIndexOrThrow("custom_component_id")));
        return item;
    }
}
