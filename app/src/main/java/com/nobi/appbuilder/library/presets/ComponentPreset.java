package com.nobi.appbuilder.library.presets;

import java.util.LinkedHashMap;
import java.util.Map;

/** A named set of property values for one component type. */
public class ComponentPreset {

    private String id;
    private String projectId;
    private String name;
    private String componentType;
    private Map<String, String> properties = new LinkedHashMap<>();
    private boolean builtIn;
    private long createdAt;

    public ComponentPreset() {
    }

    public ComponentPreset(String id, String projectId, String name,
                           String componentType,
                           Map<String, String> properties,
                           boolean builtIn) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.componentType = componentType;
        this.properties = properties != null
                ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
        this.builtIn = builtIn;
        this.createdAt = System.currentTimeMillis();
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

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Map<String, String> getProperties() {
        return new LinkedHashMap<>(properties);
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties != null
                ? new LinkedHashMap<>(properties) : new LinkedHashMap<>();
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
