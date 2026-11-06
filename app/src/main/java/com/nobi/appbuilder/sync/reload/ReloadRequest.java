package com.nobi.appbuilder.sync.reload;

import com.nobi.appbuilder.sync.ChangeOrigin;

/** One reload request: what changed, where, and why. */
public class ReloadRequest {

    private final String projectId;
    private final String screenId;
    private final ChangeOrigin origin;
    private final long timestamp;

    public ReloadRequest(String projectId, String screenId,
                         ChangeOrigin origin) {
        this.projectId = projectId;
        this.screenId = screenId;
        this.origin = origin;
        this.timestamp = System.currentTimeMillis();
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
}
