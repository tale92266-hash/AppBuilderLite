package com.nobi.appbuilder.assets.fonts;

import android.content.Context;
import android.net.Uri;

import com.nobi.appbuilder.assets.AssetImportManager;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetType;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.io.File;

/**
 * Font import through the asset importer, plus the font row in
 * font_assets. The original file is preserved.
 */
public class FontImportManager {

    private final AssetImportManager delegate;
    private final DatabaseHelper dbHelper;

    public FontImportManager(Context context, DatabaseHelper dbHelper) {
        this.delegate = new AssetImportManager(context, dbHelper);
        this.dbHelper = dbHelper;
    }

    public FontAssetModel importFont(String projectId, Uri uri,
                                     String displayName) {
        AssetImportManager.ImportResult result =
                delegate.importFromUri(projectId, uri, displayName);
        if (result.error != null || result.asset == null
                || result.asset.getType() != AssetType.FONT) {
            return null;
        }
        AssetModel asset = result.asset;
        File file = new File(asset.getPath());
        if (!FontValidator.validate(file).isEmpty()) {
            return null;
        }
        FontAssetModel font = new FontAssetModel();
        font.setId(asset.getId());
        font.setProjectId(projectId);
        font.setName(asset.getResourceName());
        font.setFamily(asset.getResourceName());
        font.setFileName(asset.getName());
        font.setPath(asset.getPath());
        font.setCreatedAt(asset.getCreatedAt());
        android.content.ContentValues values = new android.content.ContentValues();
        values.put("id", font.getId());
        values.put("project_id", projectId);
        values.put("name", font.getName());
        values.put("family", font.getFamily());
        values.put("file_name", font.getFileName());
        values.put("path", font.getPath());
        values.put("created_at", font.getCreatedAt());
        long row = dbHelper.getWritableDatabase().insertWithOnConflict(
                DatabaseHelper.TABLE_FONT_ASSETS, null, values,
                android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0 ? font : null;
    }
}
