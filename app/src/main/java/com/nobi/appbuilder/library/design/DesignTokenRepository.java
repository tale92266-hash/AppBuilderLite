package com.nobi.appbuilder.library.design;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite store for design tokens. */
public class DesignTokenRepository {

    private final DatabaseHelper dbHelper;

    public DesignTokenRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean save(DesignToken token) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", token.getId());
        values.put("project_id", token.getProjectId());
        values.put("kind", token.getKind());
        values.put("name", token.getName());
        values.put("value", token.getValue());
        values.put("updated_at", token.getUpdatedAt());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_DESIGN_TOKENS,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public List<DesignToken> getByProjectAndKind(String projectId, String kind) {
        List<DesignToken> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_DESIGN_TOKENS, null,
                "project_id = ? AND kind = ?", new String[]{projectId, kind},
                null, null, "name ASC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public DesignToken getByName(String projectId, String kind, String name) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_DESIGN_TOKENS, null,
                "project_id = ? AND kind = ? AND name = ?",
                new String[]{projectId, kind, name}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public boolean delete(String tokenId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_DESIGN_TOKENS,
                "id = ?", new String[]{tokenId}) > 0;
    }

    private DesignToken fromCursor(Cursor cursor) {
        DesignToken token = new DesignToken();
        token.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        token.setProjectId(cursor.getString(
                cursor.getColumnIndexOrThrow("project_id")));
        token.setKind(cursor.getString(cursor.getColumnIndexOrThrow("kind")));
        token.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        token.setValue(cursor.getString(cursor.getColumnIndexOrThrow("value")));
        token.setUpdatedAt(cursor.getLong(
                cursor.getColumnIndexOrThrow("updated_at")));
        return token;
    }
}
