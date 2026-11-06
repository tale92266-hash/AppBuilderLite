package com.nobi.appbuilder.assets;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks asset ID ↔ resource name ↔ JSON IR component references.
 * ImageViews store the stable asset id in their "assetId" (and
 * "src") property — never a temporary filesystem path. Renaming an
 * asset updates every known reference; deleting a referenced asset
 * is blocked.
 */
public class AssetReferenceManager {

    private final DatabaseHelper dbHelper;

    public AssetReferenceManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    /** Screen + component ids that reference this asset. */
    public List<String> findUsages(String projectId, String assetId) {
        List<String> usages = new ArrayList<>();
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            return usages;
        }
        for (ScreenModel screen : document.getScreens()) {
            ComponentModel root = screen.getRootComponent();
            if (root == null) {
                continue;
            }
            collectUsages(root, screen, assetId, usages);
        }
        return usages;
    }

    private void collectUsages(ComponentModel model, ScreenModel screen,
                               String assetId, List<String> usages) {
        String src = model.getProperty("src");
        String storedId = model.getProperty("assetId");
        if (assetId.equals(src) || assetId.equals(storedId)) {
            usages.add(screen.getName() + " / " + model.getId());
        }
        for (ComponentModel child : model.getChildren()) {
            collectUsages(child, screen, assetId, usages);
        }
    }

    public boolean isReferenced(String projectId, String assetId) {
        return !findUsages(projectId, assetId).isEmpty();
    }

    /**
     * Renames an asset's resource name and updates every IR
     * reference that used the old name. Returns the number of
     * components updated.
     */
    public int renameAsset(String projectId, String assetId,
                           String newResourceName) {
        AssetRepository repository = new AssetRepository(dbHelper);
        AssetModel asset = repository.getById(assetId);
        if (asset == null) {
            return 0;
        }
        String oldName = asset.getResourceName();
        asset.setResourceName(newResourceName);
        repository.update(asset);
        DocumentRepository repo = new DocumentRepository(dbHelper);
        IRDocument document = repo.loadDocument(projectId);
        if (document == null) {
            return 0;
        }
        int updated = 0;
        for (ScreenModel screen : document.getScreens()) {
            ComponentModel root = screen.getRootComponent();
            if (root != null) {
                updated += renameInTree(root, oldName, newResourceName);
            }
        }
        if (updated > 0) {
            repo.saveDocument(document);
        }
        return updated;
    }

    private int renameInTree(ComponentModel model, String oldName,
                             String newName) {
        int updated = 0;
        String src = model.getProperty("src");
        if (oldName != null && oldName.equals(src)) {
            model.setProperty("src", newName);
            updated++;
        }
        for (ComponentModel child : model.getChildren()) {
            updated += renameInTree(child, oldName, newName);
        }
        return updated;
    }

    public void recordReference(String projectId, String assetId,
                                String screenId, String componentId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("asset_id", assetId);
        values.put("project_id", projectId);
        values.put("screen_id", screenId);
        values.put("component_id", componentId);
        values.put("created_at", System.currentTimeMillis());
        db.insertWithOnConflict(DatabaseHelper.TABLE_ASSET_REFERENCES,
                null, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public List<String> getRecordedReferences(String assetId) {
        List<String> refs = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_ASSET_REFERENCES,
                new String[]{"screen_id", "component_id"},
                "asset_id = ?", new String[]{assetId}, null, null, null)) {
            while (cursor.moveToNext()) {
                refs.add(cursor.getString(0) + "/" + cursor.getString(1));
            }
        }
        return refs;
    }
}
