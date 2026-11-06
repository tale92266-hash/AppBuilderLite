package com.nobi.appbuilder.autosave;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.concurrent.Executor;

/**
 * Crash recovery (Phase 5). Writes atomic recovery snapshots of the
 * whole project document (INSERT OR REPLACE in one transaction, so a
 * crash mid-write never corrupts the last valid document) and offers
 * them back on the next open when they are newer than the last save.
 * Snapshots are deleted after a successful restore or an explicit
 * discard, so stale snapshots never pile up.
 */
public class RecoveryManager {

    public static final long DEFAULT_SNAPSHOT_INTERVAL_MS = 30_000;

    /** Supplies the current document JSON for a periodic snapshot. */
    public interface SnapshotSource {
        String getProjectId();

        String getScreenId();

        String getDocumentJson();

        /** True when the document has edits not yet saved. */
        boolean isDirty();
    }

    public interface SnapshotCallback {
        void onSnapshot(RecoverySnapshot snapshot);
    }

    private final DatabaseHelper dbHelper;
    private final Executor executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private long lastSavedAt;
    private boolean autoSnapshotRunning;
    private long snapshotIntervalMs = DEFAULT_SNAPSHOT_INTERVAL_MS;

    private final Runnable autoSnapshotTask = new Runnable() {
        @Override
        public void run() {
            if (!autoSnapshotRunning || autoSnapshotSource == null) {
                return;
            }
            // Only snapshot when there are unsaved edits. Snapshotting a
            // clean document creates a snapshot newer than the last save,
            // which the next open mistakes for unrestored work and asks
            // about — even though everything is already saved.
            if (autoSnapshotSource.isDirty()) {
                String json = autoSnapshotSource.getDocumentJson();
                if (json != null) {
                    saveSnapshot(autoSnapshotSource.getProjectId(),
                            autoSnapshotSource.getScreenId(), json, lastSavedAt);
                }
            }
            mainHandler.postDelayed(this, snapshotIntervalMs);
        }
    };
    private SnapshotSource autoSnapshotSource;

    public RecoveryManager(DatabaseHelper dbHelper, Executor executor) {
        this.dbHelper = dbHelper;
        this.executor = executor;
    }

    public void setLastSavedAt(long lastSavedAt) {
        this.lastSavedAt = lastSavedAt;
    }

    public void setSnapshotIntervalMs(long intervalMs) {
        this.snapshotIntervalMs = Math.max(10_000, intervalMs);
    }

    /** Writes one snapshot (atomic replace). Safe to call on any thread. */
    public void saveSnapshot(String projectId, String screenId,
                             String documentJson, long savedAt) {
        if (projectId == null || documentJson == null) {
            return;
        }
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put("project_id", projectId);
                values.put("screen_id", screenId);
                values.put("document_json", documentJson);
                values.put("saved_at", savedAt);
                values.put("snapshot_at", System.currentTimeMillis());
                db.insertWithOnConflict(DatabaseHelper.TABLE_RECOVERY, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        });
    }

    /** Reads the snapshot for a project (result on the main thread). */
    public void getSnapshot(String projectId, SnapshotCallback callback) {
        executor.execute(() -> {
            RecoverySnapshot snapshot = readSnapshot(projectId);
            mainHandler.post(() -> callback.onSnapshot(snapshot));
        });
    }

    /** Synchronous read; call only from a background thread. */
    public RecoverySnapshot readSnapshot(String projectId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_RECOVERY, null,
                "project_id = ?", new String[]{projectId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return new RecoverySnapshot(
                        cursor.getString(cursor.getColumnIndexOrThrow("project_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("screen_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("document_json")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("saved_at")),
                        cursor.getLong(cursor.getColumnIndexOrThrow("snapshot_at")));
            }
        }
        return null;
    }

    public void deleteSnapshot(String projectId) {
        if (projectId == null) {
            return;
        }
        executor.execute(() -> dbHelper.getWritableDatabase().delete(
                DatabaseHelper.TABLE_RECOVERY, "project_id = ?",
                new String[]{projectId}));
    }

    /** Starts periodic snapshots while the editor is open. */
    public void startAutoSnapshot(SnapshotSource source) {
        autoSnapshotSource = source;
        autoSnapshotRunning = true;
        mainHandler.removeCallbacks(autoSnapshotTask);
        mainHandler.postDelayed(autoSnapshotTask, snapshotIntervalMs);
    }

    public void stopAutoSnapshot() {
        autoSnapshotRunning = false;
        mainHandler.removeCallbacks(autoSnapshotTask);
        autoSnapshotSource = null;
    }
}
