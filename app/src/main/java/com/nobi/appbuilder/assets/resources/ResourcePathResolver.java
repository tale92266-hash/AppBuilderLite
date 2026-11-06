package com.nobi.appbuilder.assets.resources;

import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.io.File;
import java.util.List;

/** Resolves a resource name to the file that backs it. */
public class ResourcePathResolver {

    private final AssetRepository repository;

    public ResourcePathResolver(DatabaseHelper dbHelper) {
        this.repository = new AssetRepository(dbHelper);
    }

    public File resolve(String projectId, String resourceName) {
        if (resourceName == null) {
            return null;
        }
        for (AssetModel asset : repository.getByProject(projectId)) {
            if (resourceName.equals(asset.getResourceName())) {
                return new File(asset.getPath());
            }
        }
        return null;
    }

    public File drawableFileFor(AssetModel asset) {
        if (asset == null || asset.getPath() == null) {
            return null;
        }
        return new File(asset.getPath());
    }

    public List<AssetModel> allAssets(String projectId) {
        return repository.getByProject(projectId);
    }
}
