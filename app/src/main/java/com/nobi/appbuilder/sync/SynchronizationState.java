package com.nobi.appbuilder.sync;

/** Current sync health for one project, shown in the status UI. */
public class SynchronizationState {

    public enum Status {
        SYNCHRONIZED,
        SYNCING,
        VISUAL_PENDING,
        CODE_PENDING,
        CONFLICT,
        INVALID_XML,
        UNSUPPORTED,
        MANUAL_REQUIRED
    }

    private Status status = Status.SYNCHRONIZED;
    private long lastSyncTimeMs;
    private int pendingChanges;
    private int conflictCount;
    private String message = "";

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getLastSyncTimeMs() {
        return lastSyncTimeMs;
    }

    public void setLastSyncTimeMs(long lastSyncTimeMs) {
        this.lastSyncTimeMs = lastSyncTimeMs;
    }

    public int getPendingChanges() {
        return pendingChanges;
    }

    public void setPendingChanges(int pendingChanges) {
        this.pendingChanges = pendingChanges;
    }

    public int getConflictCount() {
        return conflictCount;
    }

    public void setConflictCount(int conflictCount) {
        this.conflictCount = conflictCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    public boolean isHealthy() {
        return status == Status.SYNCHRONIZED;
    }

    public static SynchronizationState synchronizedState(long timeMs) {
        SynchronizationState state = new SynchronizationState();
        state.setStatus(Status.SYNCHRONIZED);
        state.setLastSyncTimeMs(timeMs);
        return state;
    }
}
