package com.nobi.appbuilder.sync;

import com.nobi.appbuilder.sync.conflict.ConflictRecord;
import com.nobi.appbuilder.sync.conflict.ConflictResolution;

/**
 * Programmatic facade over SynchronizationManager for activities:
 * trigger a sync, read state, resolve conflicts. The status UI lives
 * in ui.sync.SyncStatusController; this class owns no views.
 */
public class SynchronizationController {

    public interface ControllerListener {
        void onReviewChanges();

        void onOpenHistory();

        void onOpenSettings();
    }

    private final SynchronizationManager manager;
    private ControllerListener listener;

    public SynchronizationController(SynchronizationManager manager) {
        this.manager = manager;
    }

    public void setListener(ControllerListener listener) {
        this.listener = listener;
    }

    public SynchronizationManager getManager() {
        return manager;
    }

    public SynchronizationState getState() {
        return manager.getState();
    }

    public void synchronizeNow() {
        manager.synchronizeNow();
    }

    public void onVisualChange(String screenId) {
        manager.onVisualChange(screenId);
    }

    public void onCodeChange(String screenId, String filePath) {
        manager.onCodeChange(screenId, filePath);
    }

    public boolean resolveConflict(ConflictRecord record,
                                   ConflictResolution resolution) {
        return manager.resolveConflict(record, resolution);
    }

    public void requestReviewChanges() {
        if (listener != null) {
            listener.onReviewChanges();
        }
    }

    public void requestOpenHistory() {
        if (listener != null) {
            listener.onOpenHistory();
        }
    }

    public void requestOpenSettings() {
        if (listener != null) {
            listener.onOpenSettings();
        }
    }
}
