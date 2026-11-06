package com.nobi.appbuilder.library;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** Records how often each component type is added to a canvas. */
public class ComponentUsageTracker {

    private final DatabaseHelper dbHelper;

    public ComponentUsageTracker(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void recordUse(String projectId, String baseType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("item_id", baseType);
        values.put("use_count", 1);
        values.put("last_used_at", System.currentTimeMillis());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_COMPONENT_USAGE,
                null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (row < 0) {
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_COMPONENT_USAGE
                    + " SET use_count = use_count + 1, last_used_at = ?"
                    + " WHERE project_id = ? AND item_id = ?",
                    new Object[]{System.currentTimeMillis(), projectId, baseType});
        }
    }

    public long getLastUsedAt(String projectId, String baseType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_USAGE,
                new String[]{"last_used_at"},
                "project_id = ? AND item_id = ?",
                new String[]{projectId, baseType}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getLong(0);
            }
        }
        return 0;
    }

    public int getUseCount(String projectId, String baseType) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_USAGE,
                new String[]{"use_count"},
                "project_id = ? AND item_id = ?",
                new String[]{projectId, baseType}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    public List<String> getRecentTypes(String projectId, int limit) {
        List<String> types = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COMPONENT_USAGE,
                new String[]{"item_id"},
                "project_id = ?", new String[]{projectId}, null, null,
                "last_used_at DESC", String.valueOf(Math.max(1, limit)))) {
            while (cursor.moveToNext()) {
                types.add(cursor.getString(0));
            }
        }
        return types;
    }
}
