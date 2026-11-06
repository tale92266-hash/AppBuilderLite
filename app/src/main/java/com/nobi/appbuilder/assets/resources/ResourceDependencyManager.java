package com.nobi.appbuilder.assets.resources;

import com.nobi.appbuilder.assets.AssetReferenceManager;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.List;

/**
 * Which screens/components use a resource. Delegates to the
 * AssetReferenceManager — one source of truth for references.
 */
public class ResourceDependencyManager {

    private final AssetReferenceManager references;

    public ResourceDependencyManager(DatabaseHelper dbHelper) {
        this.references = new AssetReferenceManager(dbHelper);
    }

    public List<String> usagesOf(String projectId, String assetId) {
        return references.findUsages(projectId, assetId);
    }

    public boolean isUsed(String projectId, String assetId) {
        return references.isReferenced(projectId, assetId);
    }
}
