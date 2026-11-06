package com.nobi.appbuilder.sync;

/** One observed change waiting to be synchronized. */
public class SynchronizationEvent {

    public static final String KIND_VISUAL_CHANGE = "visual_change";
    public static final String KIND_CODE_CHANGE = "code_change";
    public static final String KIND_EXTERNAL_CHANGE = "external_change";

    private final String kind;
    private final String projectId;
    private final String screenId;
    private final String componentId;
    private final ChangeOrigin origin;
    private final long timestamp;

    public SynchronizationEvent(String kind, String projectId,
                                String screenId, String componentId,
                                ChangeOrigin origin) {
        this.kind = kind;
        this.projectId = projectId;
        this.screenId = screenId;
        this.componentId = componentId;
        this.origin = origin;
        this.timestamp = System.currentTimeMillis();
    }

    public String getKind() {
        return kind;
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

    public ChangeOrigin getOrigin() {
        return origin;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
