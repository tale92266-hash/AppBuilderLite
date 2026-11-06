package com.nobi.appbuilder.sync;

import android.os.Handler;
import android.os.Looper;

/** Debounced scheduling: each new change restarts the timer. */
public class SynchronizationScheduler {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pending;

    public synchronized void schedule(Runnable task, int delayMs) {
        cancel();
        pending = task;
        handler.postDelayed(task, delayMs);
    }

    public synchronized void cancel() {
        if (pending != null) {
            handler.removeCallbacks(pending);
            pending = null;
        }
    }
}
