package com.nobi.appbuilder.assets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** Usage counts + unused-asset detection for cleanup. */
public class AssetUsageTracker {

    private final DatabaseHelper dbHelper;

    public AssetUsageTracker(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void recordUse(String projectId, String assetId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("asset_id", assetId);
        values.put("use_count", 1);
        values.put("last_used_at", System.currentTimeMillis());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_ASSET_USAGE,
                null, values, SQLiteDatabase.CONFLICT_IGNORE);
        if (row < 0) {
            db.execSQL("UPDATE " + DatabaseHelper.TABLE_ASSET_USAGE
                    + " SET use_count = use_count + 1, last_used_at = ?"
                    + " WHERE project_id = ? AND asset_id = ?",
                    new Object[]{System.currentTimeMillis(), projectId, assetId});
        }
    }

    public int getUseCount(String projectId, String assetId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSET_USAGE,
                new String[]{"use_count"},
                "project_id = ? AND asset_id = ?",
                new String[]{projectId, assetId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
        }
        return 0;
    }

    /** Assets with no IR references and no recorded use. */
    public List<AssetModel> getUnused(String projectId,
                                      List<AssetModel> assets) {
        AssetReferenceManager references = new AssetReferenceManager(dbHelper);
        List<AssetModel> unused = new ArrayList<>();
        for (AssetModel asset : assets) {
            if (!references.isReferenced(projectId, asset.getId())
                    && getUseCount(projectId, asset.getId()) == 0) {
                unused.add(asset);
            }
        }
        return unused;
    }
}
