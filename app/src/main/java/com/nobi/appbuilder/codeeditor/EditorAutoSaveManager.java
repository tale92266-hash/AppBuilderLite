package com.nobi.appbuilder.codeeditor;

import android.os.Handler;
import android.os.Looper;

/** Debounced autosave: saves 800 ms after the last edit, never
 * mid-keystroke, and never overwrites a file when the save fails. */
public class EditorAutoSaveManager {

    public static final long DEFAULT_DEBOUNCE_MS = 800;

    public enum SaveState {
        IDLE, DIRTY, SAVING, SAVED, FAILED
    }

    public interface SaveListener {
        void onSaveState(SaveState state);
    }

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final long debounceMs;
    private SaveListener listener;
    private Runnable pending;
    private SaveState state = SaveState.IDLE;

    public EditorAutoSaveManager() {
        this(DEFAULT_DEBOUNCE_MS);
    }

    public EditorAutoSaveManager(long debounceMs) {
        this.debounceMs = debounceMs;
    }

    public void setListener(SaveListener listener) {
        this.listener = listener;
    }

    public SaveState getState() {
        return state;
    }

    /** Call on every edit; the save fires after the debounce. */
    public void markDirty(Runnable saveAction) {
        setState(SaveState.DIRTY);
        if (pending != null) {
            handler.removeCallbacks(pending);
        }
        pending = () -> {
            setState(SaveState.SAVING);
            try {
                saveAction.run();
                setState(SaveState.SAVED);
            } catch (Exception e) {
                setState(SaveState.FAILED);
            }
        };
        handler.postDelayed(pending, debounceMs);
    }

    /** Saves immediately, cancelling any pending debounced save. */
    public void saveNow(Runnable saveAction) {
        if (pending != null) {
            handler.removeCallbacks(pending);
            pending = null;
        }
        setState(SaveState.SAVING);
        try {
            saveAction.run();
            setState(SaveState.SAVED);
        } catch (Exception e) {
            setState(SaveState.FAILED);
        }
    }

    public void cancelPending() {
        if (pending != null) {
            handler.removeCallbacks(pending);
            pending = null;
        }
    }

    private void setState(SaveState state) {
        this.state = state;
        if (listener != null) {
            listener.onSaveState(state);
        }
    }
}
