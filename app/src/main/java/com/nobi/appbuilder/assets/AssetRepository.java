package com.nobi.appbuilder.assets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Domain-level asset repository. The asset rows themselves live in
 * the existing `assets` table and are read through the database
 * AssetRepository (composition, not a second store); this class
 * adds the Phase 13 extras (folder, content hash, resource name),
 * the asset_metadata rows, and the asset_folders table.
 */
public class AssetRepository {

    private final DatabaseHelper dbHelper;
    private final com.nobi.appbuilder.database.AssetRepository dbAssets;

    public AssetRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.dbAssets = new com.nobi.appbuilder.database.AssetRepository(dbHelper);
    }

    public boolean insert(AssetModel asset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", asset.getId());
        values.put("project_id", asset.getProjectId());
        values.put("name", asset.getName());
        values.put("type", asset.getType().name());
        values.put("path", asset.getPath());
        values.put("size_bytes", asset.getSizeBytes());
        values.put("created_at", asset.getCreatedAt());
        values.put("folder_id", asset.getFolderId());
        values.put("hash", asset.getHash());
        values.put("resource_name", asset.getResourceName());
        return db.insert(DatabaseHelper.TABLE_ASSETS, null, values) >= 0;
    }

    public List<AssetModel> getByProject(String projectId) {
        List<AssetModel> out = new ArrayList<>();
        for (com.nobi.appbuilder.database.AssetRepository.Asset row
                : dbAssets.getByProject(projectId)) {
            AssetModel model = new AssetModel();
            model.setId(row.id);
            model.setProjectId(row.projectId);
            model.setName(row.name);
            model.setType(AssetType.fromName(row.type));
            model.setPath(row.path);
            model.setSizeBytes(row.sizeBytes);
            model.setCreatedAt(row.createdAt);
            out.add(model);
        }
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSETS,
                new String[]{"id", "folder_id", "hash", "resource_name"},
                "project_id = ?", new String[]{projectId}, null, null, null)) {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                for (AssetModel model : out) {
                    if (model.getId().equals(id)) {
                        model.setFolderId(cursor.getString(1));
                        model.setHash(cursor.getString(2));
                        model.setResourceName(cursor.getString(3));
                        break;
                    }
                }
            }
        }
        return out;
    }

    public AssetModel getById(String assetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSETS, null,
                "id = ?", new String[]{assetId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                AssetModel model = new AssetModel();
                model.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                model.setProjectId(cursor.getString(
                        cursor.getColumnIndexOrThrow("project_id")));
                model.setName(cursor.getString(
                        cursor.getColumnIndexOrThrow("name")));
                model.setType(AssetType.fromName(cursor.getString(
                        cursor.getColumnIndexOrThrow("type"))));
                model.setPath(cursor.getString(
                        cursor.getColumnIndexOrThrow("path")));
                model.setSizeBytes(cursor.getLong(
                        cursor.getColumnIndexOrThrow("size_bytes")));
                model.setCreatedAt(cursor.getLong(
                        cursor.getColumnIndexOrThrow("created_at")));
                model.setFolderId(cursor.getString(
                        cursor.getColumnIndexOrThrow("folder_id")));
                model.setHash(cursor.getString(
                        cursor.getColumnIndexOrThrow("hash")));
                model.setResourceName(cursor.getString(
                        cursor.getColumnIndexOrThrow("resource_name")));
                return model;
            }
        }
        return null;
    }

    public AssetModel getByHash(String projectId, String hash) {
        if (hash == null) {
            return null;
        }
        for (AssetModel model : getByProject(projectId)) {
            if (hash.equals(model.getHash())) {
                return model;
            }
        }
        return null;
    }

    public boolean update(AssetModel asset) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", asset.getName());
        values.put("path", asset.getPath());
        values.put("size_bytes", asset.getSizeBytes());
        values.put("folder_id", asset.getFolderId());
        values.put("resource_name", asset.getResourceName());
        return db.update(DatabaseHelper.TABLE_ASSETS, values,
                "id = ?", new String[]{asset.getId()}) > 0;
    }

    public boolean delete(String assetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_ASSET_METADATA,
                "asset_id = ?", new String[]{assetId});
        db.delete(DatabaseHelper.TABLE_ASSET_REFERENCES,
                "asset_id = ?", new String[]{assetId});
        return db.delete(DatabaseHelper.TABLE_ASSETS,
                "id = ?", new String[]{assetId}) > 0;
    }

    public long totalSize(String projectId) {
        return dbAssets.totalSize(projectId);
    }

    public boolean saveMetadata(AssetMetadata metadata) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("asset_id", metadata.getAssetId());
        values.put("width", metadata.getWidth());
        values.put("height", metadata.getHeight());
        values.put("format", metadata.getFormat());
        values.put("animated", metadata.isAnimated() ? 1 : 0);
        values.put("duration_ms", metadata.getDurationMs());
        long row = db.insertWithOnConflict(
                DatabaseHelper.TABLE_ASSET_METADATA, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public AssetMetadata getMetadata(String assetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSET_METADATA,
                null, "asset_id = ?", new String[]{assetId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                AssetMetadata metadata = new AssetMetadata();
                metadata.setAssetId(assetId);
                metadata.setWidth(cursor.getInt(
                        cursor.getColumnIndexOrThrow("width")));
                metadata.setHeight(cursor.getInt(
                        cursor.getColumnIndexOrThrow("height")));
                metadata.setFormat(cursor.getString(
                        cursor.getColumnIndexOrThrow("format")));
                metadata.setAnimated(cursor.getInt(
                        cursor.getColumnIndexOrThrow("animated")) == 1);
                metadata.setDurationMs(cursor.getLong(
                        cursor.getColumnIndexOrThrow("duration_ms")));
                return metadata;
            }
        }
        return null;
    }

    public String createFolder(String projectId, String name,
                               String parentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String id = com.nobi.appbuilder.utils.IDGenerator.newId();
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("project_id", projectId);
        values.put("name", name);
        values.put("parent_id", parentId);
        values.put("created_at", System.currentTimeMillis());
        if (db.insert(DatabaseHelper.TABLE_ASSET_FOLDERS, null, values) < 0) {
            return null;
        }
        return id;
    }

    public List<String[]> getFolders(String projectId) {
        List<String[]> folders = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSET_FOLDERS,
                new String[]{"id", "name", "parent_id"},
                "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                folders.add(new String[]{cursor.getString(0),
                        cursor.getString(1), cursor.getString(2)});
            }
        }
        return folders;
    }

    public boolean isFolderEmpty(String folderId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSETS,
                new String[]{"id"}, "folder_id = ?",
                new String[]{folderId}, null, null, null, "1")) {
            return !cursor.moveToFirst();
        }
    }

    public boolean deleteFolder(String folderId) {
        if (!isFolderEmpty(folderId)) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_ASSET_FOLDERS,
                "id = ?", new String[]{folderId}) > 0;
    }
}
