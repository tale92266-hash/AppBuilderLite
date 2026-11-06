package com.nobi.appbuilder.library.design;

/** One design token: a named, typed value (colour, type, spacing…). */
public class DesignToken {

    public static final String KIND_COLOR = "color";
    public static final String KIND_TYPOGRAPHY = "typography";
    public static final String KIND_SPACING = "spacing";
    public static final String KIND_SHAPE = "shape";
    public static final String KIND_ELEVATION = "elevation";

    private String id;
    private String projectId;
    private String kind;
    private String name;
    private String value;
    private long updatedAt;

    public DesignToken() {
    }

    public DesignToken(String id, String projectId, String kind,
                       String name, String value) {
        this.id = id;
        this.projectId = projectId;
        this.kind = kind;
        this.name = name;
        this.value = value;
        this.updatedAt = System.currentTimeMillis();
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

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
