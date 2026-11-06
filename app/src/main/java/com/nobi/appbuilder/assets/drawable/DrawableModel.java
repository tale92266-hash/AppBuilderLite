package com.nobi.appbuilder.assets.drawable;

/** A saved drawable resource: its name, kind and generated XML. */
public class DrawableModel {

    public static final String KIND_SHAPE = "shape";
    public static final String KIND_GRADIENT = "gradient";
    public static final String KIND_SELECTOR = "selector";
    public static final String KIND_RIPPLE = "ripple";

    private String id;
    private String projectId;
    private String name;
    private String kind;
    private String xml;
    private long createdAt;

    public DrawableModel() {
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

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
