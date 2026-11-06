package com.nobi.appbuilder.sync.conflict;

import java.util.UUID;

/** One recorded conflict, persisted until resolved. */
public class ConflictRecord {

    private String id;
    private String projectId;
    private String screenId;
    private String componentId;
    private ConflictType type;
    private String visualValue;
    private String codeValue;
    private long timestamp;
    private boolean resolved;
    private String resolution;

    public ConflictRecord() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public ConflictRecord(String projectId, String screenId,
                          String componentId, ConflictType type,
                          String visualValue, String codeValue) {
        this();
        this.projectId = projectId;
        this.screenId = screenId;
        this.componentId = componentId;
        this.type = type;
        this.visualValue = visualValue;
        this.codeValue = codeValue;
    }

    public String getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getComponentId() {
        return componentId;
    }

    public ConflictType getType() {
        return type;
    }

    public String getVisualValue() {
        return visualValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
}
