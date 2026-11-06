package com.nobi.appbuilder.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * One screen of a project. The design is a JSON document holding the root
 * ComponentModel tree; it is stored in the screens table as design_json.
 */
public class ScreenModel {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String id;
    private String projectId;
    private String name;
    private boolean isLauncher;
    private int sortOrder;
    private String designJson;
    private long createdAt;
    private long updatedAt;

    public ScreenModel() {
    }

    public ScreenModel(String id, String projectId, String name,
                       boolean isLauncher, int sortOrder) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.isLauncher = isLauncher;
        this.sortOrder = sortOrder;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isLauncher() {
        return isLauncher;
    }

    public void setLauncher(boolean launcher) {
        isLauncher = launcher;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDesignJson() {
        return designJson;
    }

    public void setDesignJson(String designJson) {
        this.designJson = designJson;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    /** Parses designJson into the root component tree, or null if empty. */
    public ComponentModel getRootComponent() {
        if (designJson == null || designJson.trim().isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(designJson, ComponentModel.class);
        } catch (Exception e) {
            return null;
        }
    }

    /** Serialises the root component tree into designJson. */
    public void setRootComponent(ComponentModel root) {
        designJson = root == null ? null : GSON.toJson(root);
        updatedAt = System.currentTimeMillis();
    }
}
