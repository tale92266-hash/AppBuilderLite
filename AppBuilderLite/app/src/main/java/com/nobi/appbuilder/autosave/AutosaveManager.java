package com.nobi.appbuilder.autosave;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;

/**
 * Debounced autosave (Phase 5, replaces the editor's inline handler).
 * Meaningful edits call scheduleSave(); the document is written on a
 * background thread after the debounce quiet period, never during a
 * drag movement (only the commit schedules a save). Explicit Save and
 * editor exit flush immediately. The listener drives the save-status
 * indicator in the toolbar.
 */
public class AutosaveManager {

    public enum SaveState {
        IDLE, DIRTY, SAVING, SAVED, FAILED
    }

    /** Runs the actual database write; returns true on success. */
    public interface SaveTask {
        boolean save();
    }

    public interface SaveListener {
        void onSaveStateChanged(SaveState state);
    }

    public static final long DEFAULT_DEBOUNCE_MS = 800;

    private final Executor executor;
    private final SaveTask saveTask;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private SaveListener listener;
    private long debounceMs = DEFAULT_DEBOUNCE_MS;
    private boolean dirty;
    private boolean saving;
    private boolean saveRequestedWhileSaving;

    private final Runnable debouncedSave = this::runSave;

    public AutosaveManager(Executor executor, SaveTask saveTask) {
        this.executor = executor;
        this.saveTask = saveTask;
    }

    public void setListener(SaveListener listener) {
        this.listener = listener;
    }

    public void setDebounceMs(long debounceMs) {
        this.debounceMs = Math.max(300, Math.min(2000, debounceMs));
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        dirty = true;
        setState(SaveState.DIRTY);
    }

    /** Schedules a save after the debounce quiet period. */
    public void scheduleSave() {
        markDirty();
        mainHandler.removeCallbacks(debouncedSave);
        mainHandler.postDelayed(debouncedSave, debounceMs);
    }

    /** Saves right now (explicit Save tap). */
    public void saveNow() {
        mainHandler.removeCallbacks(debouncedSave);
        runSave();
    }

    /** Cancels a pending debounced save and writes immediately if dirty. */
    public void flush() {
        mainHandler.removeCallbacks(debouncedSave);
        if (dirty) {
            runSave();
        }
    }

    /** Cancels any pending save without writing (editor torn down). */
    public void cancelPending() {
        mainHandler.removeCallbacks(debouncedSave);
    }

    private void runSave() {
        if (saving) {
            saveRequestedWhileSaving = true;
            return;
        }
        if (!dirty) {
            return;
        }
        saving = true;
        setState(SaveState.SAVING);
        executor.execute(() -> {
            boolean ok = false;
            try {
                ok = saveTask.save();
            } catch (Exception ignored) {
            }
            boolean success = ok;
            mainHandler.post(() -> {
                saving = false;
                if (success) {
                    dirty = false;
                    setState(SaveState.SAVED);
                    if (saveRequestedWhileSaving) {
                        saveRequestedWhileSaving = false;
                        scheduleSave();
                    }
                } else {
                    setState(SaveState.FAILED);
                }
            });
        });
    }

    private void setState(SaveState state) {
        if (listener != null) {
            listener.onSaveStateChanged(state);
        }
    }
}
