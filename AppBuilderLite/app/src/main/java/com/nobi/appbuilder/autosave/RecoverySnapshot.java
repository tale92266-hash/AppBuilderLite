package com.nobi.appbuilder.autosave;

/**
 * One recovery snapshot: the full project document JSON as it stood
 * at snapshot time, plus the time of the last successful save it was
 * taken against. A snapshot is only offered for recovery when it is
 * newer than the last saved document.
 */
public class RecoverySnapshot {

    private final String projectId;
    private final String screenId;
    private final String documentJson;
    private final long savedAt;
    private final long snapshotAt;

    public RecoverySnapshot(String projectId, String screenId, String documentJson,
                            long savedAt, long snapshotAt) {
        this.projectId = projectId;
        this.screenId = screenId;
        this.documentJson = documentJson;
        this.savedAt = savedAt;
        this.snapshotAt = snapshotAt;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getDocumentJson() {
        return documentJson;
    }

    public long getSavedAt() {
        return savedAt;
    }

    public long getSnapshotAt() {
        return snapshotAt;
    }

    /** True when the snapshot holds work newer than the last save. */
    public boolean hasUnrestoredWork(long lastSavedAt) {
        return snapshotAt > lastSavedAt;
    }
}
