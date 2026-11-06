package com.nobi.appbuilder.library.custom;

import java.util.ArrayList;
import java.util.List;

/**
 * A reusable custom component: a saved component subtree plus the
 * public properties users may edit on each instance. Stored as
 * versioned JSON; instance overrides never touch the definition.
 */
public class CustomComponentDefinition {

    /** One editable property exposed on instances. */
    public static class PublicProperty {
        public String name;
        public String targetComponentId;
        public String propertyKey;
        public String type;
        public String defaultValue;

        public PublicProperty() {
        }

        public PublicProperty(String name, String targetComponentId,
                              String propertyKey, String type,
                              String defaultValue) {
            this.name = name;
            this.targetComponentId = targetComponentId;
            this.propertyKey = propertyKey;
            this.type = type;
            this.defaultValue = defaultValue;
        }
    }

    private String id;
    private String projectId;
    private String name;
    private String description;
    private String rootType;
    private String componentsJson;
    private List<PublicProperty> publicProperties = new ArrayList<>();
    private int version;
    private long createdAt;
    private long updatedAt;

    public CustomComponentDefinition() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRootType() {
        return rootType;
    }

    public void setRootType(String rootType) {
        this.rootType = rootType;
    }

    public String getComponentsJson() {
        return componentsJson;
    }

    public void setComponentsJson(String componentsJson) {
        this.componentsJson = componentsJson;
    }

    public List<PublicProperty> getPublicProperties() {
        return publicProperties;
    }

    public void setPublicProperties(List<PublicProperty> publicProperties) {
        this.publicProperties = publicProperties != null
                ? publicProperties : new ArrayList<PublicProperty>();
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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
}
