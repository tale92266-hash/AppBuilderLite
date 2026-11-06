package com.nobi.appbuilder.assets.fonts;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for fonts: import, list, delete. Applying a font to a
 * TextView means setting its fontFamily to the font's resource
 * name — the generated project carries the file in res/font/.
 */
public class FontManager {

    private final DatabaseHelper dbHelper;
    private final FontImportManager importManager;

    public FontManager(Context context, DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.importManager = new FontImportManager(context, dbHelper);
    }

    public FontAssetModel importFont(String projectId, Uri uri,
                                     String displayName) {
        return importManager.importFont(projectId, uri, displayName);
    }

    public List<FontAssetModel> getByProject(String projectId) {
        List<FontAssetModel> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_FONT_ASSETS,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                FontAssetModel font = new FontAssetModel();
                font.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                font.setProjectId(cursor.getString(
                        cursor.getColumnIndexOrThrow("project_id")));
                font.setName(cursor.getString(
                        cursor.getColumnIndexOrThrow("name")));
                font.setFamily(cursor.getString(
                        cursor.getColumnIndexOrThrow("family")));
                font.setFileName(cursor.getString(
                        cursor.getColumnIndexOrThrow("file_name")));
                font.setPath(cursor.getString(
                        cursor.getColumnIndexOrThrow("path")));
                font.setCreatedAt(cursor.getLong(
                        cursor.getColumnIndexOrThrow("created_at")));
                out.add(font);
            }
        }
        return out;
    }

    public boolean delete(String fontId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_FONT_ASSETS,
                "id = ?", new String[]{fontId}) > 0;
    }
}
