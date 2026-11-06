package com.nobi.appbuilder.sync;

import com.nobi.appbuilder.sync.conflict.ConflictRecord;

/** Callbacks for sync progress, delivered on the main thread. */
public interface SynchronizationListener {
    void onSyncStateChanged(SynchronizationState state);

    void onConflictDetected(ConflictRecord record);

    void onSyncCompleted(SynchronizationResult result);
}
