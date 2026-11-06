package com.nobi.appbuilder.sync.history;

import com.nobi.appbuilder.sync.ChangeOrigin;

import java.util.UUID;

/** One restorable point: the full IR JSON before a sync run. */
public class SynchronizationSnapshot {

    private String id;
    private String projectId;
    private String screenId;
    private ChangeOrigin origin;
    private long timestamp;
    private String irJson;
    private String description;

    public SynchronizationSnapshot() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SynchronizationSnapshot(String projectId, String screenId,
                                   ChangeOrigin origin, String irJson,
                                   String description) {
        this();
        this.projectId = projectId;
        this.screenId = screenId;
        this.origin = origin;
        this.irJson = irJson;
        this.description = description;
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

    public ChangeOrigin getOrigin() {
        return origin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIrJson() {
        return irJson;
    }

    public String getDescription() {
        return description;
    }
}
