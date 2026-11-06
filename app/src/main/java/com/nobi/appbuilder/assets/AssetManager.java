package com.nobi.appbuilder.assets;

import android.content.Context;
import android.net.Uri;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.io.File;
import java.util.List;

/**
 * Facade for the asset system: import, browse, search, rename,
 * delete (blocked while referenced), folders, and export into a
 * generated Android Studio project.
 */
public class AssetManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;
    private final AssetRepository repository;
    private final AssetImportManager importManager;
    private final AssetReferenceManager references;
    private final AssetUsageTracker usageTracker;
    private final AssetExportManager exportManager;

    public AssetManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.repository = new AssetRepository(dbHelper);
        this.importManager = new AssetImportManager(context, dbHelper);
        this.references = new AssetReferenceManager(dbHelper);
        this.usageTracker = new AssetUsageTracker(dbHelper);
        this.exportManager = new AssetExportManager(context, dbHelper);
    }

    public AssetRepository getRepository() {
        return repository;
    }

    public AssetReferenceManager getReferences() {
        return references;
    }

    public AssetUsageTracker getUsageTracker() {
        return usageTracker;
    }

    public List<AssetModel> getAssets(String projectId) {
        return repository.getByProject(projectId);
    }

    public AssetImportManager.ImportResult importAsset(String projectId,
                                                       Uri uri,
                                                       String displayName) {
        return importManager.importFromUri(projectId, uri, displayName);
    }

    /**
     * Deletes an asset. Returns null on success, or the reason the
     * deletion was refused (referenced, or file could not be
     * removed). Referenced assets are never deleted silently.
     */
    public String deleteAsset(String projectId, String assetId) {
        if (references.isReferenced(projectId, assetId)) {
            return "Asset is used by a screen and cannot be deleted";
        }
        AssetModel asset = repository.getById(assetId);
        if (asset == null) {
            return "Asset not found";
        }
        if (!repository.delete(assetId)) {
            return "Could not delete the asset";
        }
        File file = new File(asset.getPath());
        if (file.exists() && !file.delete()) {
            return "Asset removed from the library but the file could not be deleted";
        }
        return null;
    }

    /** Renames an asset and updates every IR reference. */
    public int renameAsset(String projectId, String assetId,
                           String newResourceName) {
        return references.renameAsset(projectId, assetId, newResourceName);
    }

    public String createFolder(String projectId, String name,
                               String parentId) {
        return repository.createFolder(projectId, name, parentId);
    }

    public List<String[]> getFolders(String projectId) {
        return repository.getFolders(projectId);
    }

    public List<String> exportToProject(String projectId, File workspaceRoot) {
        return exportManager.exportToProject(projectId, workspaceRoot);
    }

    public List<AssetModel> getUnused(String projectId) {
        return usageTracker.getUnused(projectId, getAssets(projectId));
    }
}
