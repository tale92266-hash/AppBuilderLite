package com.nobi.appbuilder.sync;

import java.util.ArrayList;
import java.util.List;

/** Outcome of one synchronization run. */
public class SynchronizationResult {

    private final boolean success;
    private final int appliedChanges;
    private final int conflicts;
    private final List<SynchronizationError> errors;
    private final long durationMs;

    public SynchronizationResult(boolean success, int appliedChanges,
                                 int conflicts,
                                 List<SynchronizationError> errors,
                                 long durationMs) {
        this.success = success;
        this.appliedChanges = appliedChanges;
        this.conflicts = conflicts;
        this.errors = errors == null ? new ArrayList<>() : errors;
        this.durationMs = durationMs;
    }

    public static SynchronizationResult success(int appliedChanges,
                                                long durationMs) {
        return new SynchronizationResult(true, appliedChanges, 0,
                new ArrayList<>(), durationMs);
    }

    public static SynchronizationResult failure(
            List<SynchronizationError> errors, long durationMs) {
        return new SynchronizationResult(false, 0, 0, errors, durationMs);
    }

    public boolean isSuccess() {
        return success;
    }

    public int getAppliedChanges() {
        return appliedChanges;
    }

    public int getConflicts() {
        return conflicts;
    }

    public List<SynchronizationError> getErrors() {
        return errors;
    }

    public long getDurationMs() {
        return durationMs;
    }
}
