package com.nobi.appbuilder.assets.vector;

import java.util.ArrayList;
import java.util.List;

/** A vector drawable asset: viewport + size + paths. */
public class VectorAssetModel {

    private String id;
    private String projectId;
    private String name;
    private float viewportWidth = 24;
    private float viewportHeight = 24;
    private float widthDp = 24;
    private float heightDp = 24;
    private List<VectorPathModel> paths = new ArrayList<>();
    private long createdAt;

    public VectorAssetModel() {
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

    public float getViewportWidth() {
        return viewportWidth;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    }

    public float getWidthDp() {
        return widthDp;
    }

    public void setWidthDp(float widthDp) {
        this.widthDp = widthDp;
    }

    public float getHeightDp() {
        return heightDp;
    }

    public void setHeightDp(float heightDp) {
        this.heightDp = heightDp;
    }

    public List<VectorPathModel> getPaths() {
        return paths;
    }

    public void setPaths(List<VectorPathModel> paths) {
        this.paths = paths != null ? paths : new ArrayList<VectorPathModel>();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
