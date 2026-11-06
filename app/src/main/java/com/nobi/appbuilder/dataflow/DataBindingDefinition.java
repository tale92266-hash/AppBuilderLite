package com.nobi.appbuilder.dataflow;

/**
 * Binds one data source (a variable or screen argument) to one
 * component property. Transforms are fixed strings only — no
 * expression evaluation.
 */
public class DataBindingDefinition {

    public static final String TRANSFORM_NONE = "none";
    public static final String TRANSFORM_UPPERCASE = "uppercase";
    public static final String TRANSFORM_LOWERCASE = "lowercase";

    private String id;
    private String sourceKey;
    private String targetComponentId;
    private String targetProperty;
    private String transform = TRANSFORM_NONE;

    public DataBindingDefinition() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public String getTargetComponentId() {
        return targetComponentId;
    }

    public void setTargetComponentId(String targetComponentId) {
        this.targetComponentId = targetComponentId;
    }

    public String getTargetProperty() {
        return targetProperty;
    }

    public void setTargetProperty(String targetProperty) {
        this.targetProperty = targetProperty;
    }

    public String getTransform() {
        return transform;
    }

    public void setTransform(String transform) {
        this.transform = transform;
    }

    public String applyTransform(String value) {
        if (value == null) {
            return null;
        }
        if (TRANSFORM_UPPERCASE.equals(transform)) {
            return value.toUpperCase();
        }
        if (TRANSFORM_LOWERCASE.equals(transform)) {
            return value.toLowerCase();
        }
        return value;
    }
}
