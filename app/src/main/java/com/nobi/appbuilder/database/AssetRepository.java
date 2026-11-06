package com.nobi.appbuilder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

/**
 * CRUD for project assets (images, fonts, raw files). The file itself
 * lives in internal storage under projects/&lt;projectId&gt;/assets/;
 * this table only stores its metadata.
 */
public class AssetRepository {

    public static class Asset {
        public String id;
        public String projectId;
        public String name;
        public String type;
        public String path;
        public long sizeBytes;
        public long createdAt;
    }

    private final DatabaseHelper dbHelper;

    public AssetRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean insert(Asset asset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", asset.id);
        values.put("project_id", asset.projectId);
        values.put("name", asset.name);
        values.put("type", asset.type);
        values.put("path", asset.path);
        values.put("size_bytes", asset.sizeBytes);
        values.put("created_at", asset.createdAt);
        return db.insert(DatabaseHelper.TABLE_ASSETS, null, values) >= 0;
    }

    public List<Asset> getByProject(String projectId) {
        List<Asset> assets = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSETS, null,
                "project_id = ?", new String[]{projectId}, null, null,
                "created_at ASC")) {
            while (cursor.moveToNext()) {
                Asset asset = new Asset();
                asset.id = cursor.getString(cursor.getColumnIndexOrThrow("id"));
                asset.projectId = cursor.getString(cursor.getColumnIndexOrThrow("project_id"));
                asset.name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                asset.type = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                asset.path = cursor.getString(cursor.getColumnIndexOrThrow("path"));
                asset.sizeBytes = cursor.getLong(cursor.getColumnIndexOrThrow("size_bytes"));
                asset.createdAt = cursor.getLong(cursor.getColumnIndexOrThrow("created_at"));
                assets.add(asset);
            }
        }
        return assets;
    }

    public boolean delete(String assetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ASSETS,
                "id = ?", new String[]{assetId}) > 0;
    }

    public boolean deleteByProject(String projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_ASSETS,
                "project_id = ?", new String[]{projectId});
        return true;
    }

    public long totalSize(String projectId) {
        long total = 0;
        for (Asset asset : getByProject(projectId)) {
            total += asset.sizeBytes;
        }
        return total;
    }
}
