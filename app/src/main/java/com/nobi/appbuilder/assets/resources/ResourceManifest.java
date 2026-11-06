package com.nobi.appbuilder.assets.resources;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.List;

/** A JSON manifest of every resource in a project, for export. */
public class ResourceManifest {

    private final AssetRepository repository;

    public ResourceManifest(DatabaseHelper dbHelper) {
        this.repository = new AssetRepository(dbHelper);
    }

    public String build(String projectId) {
        List<AssetModel> assets = repository.getByProject(projectId);
        JsonObject root = new JsonObject();
        root.addProperty("project_id", projectId);
        root.addProperty("asset_count", assets.size());
        JsonArray array = new JsonArray();
        for (AssetModel asset : assets) {
            JsonObject entry = new JsonObject();
            entry.addProperty("id", asset.getId());
            entry.addProperty("name", asset.getName());
            entry.addProperty("resource_name", asset.getResourceName());
            entry.addProperty("type", asset.getType().name());
            entry.addProperty("path", asset.getPath());
            entry.addProperty("size_bytes", asset.getSizeBytes());
            array.add(entry);
        }
        root.add("assets", array);
        return new Gson().toJson(root);
    }
}
