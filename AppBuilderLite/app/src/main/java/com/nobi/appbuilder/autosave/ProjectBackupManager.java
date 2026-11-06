package com.nobi.appbuilder.autosave;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.concurrent.Executor;

/**
 * Keeps the last saved document of each project as a backup before it
 * is overwritten, so a bad save can be rolled back. One backup row
 * per project (replaced on every save); deleted with the project.
 */
public class ProjectBackupManager {

    private final DatabaseHelper dbHelper;
    private final Executor executor;

    public ProjectBackupManager(DatabaseHelper dbHelper, Executor executor) {
        this.dbHelper = dbHelper;
        this.executor = executor;
    }

    /** Stores the current saved JSON as the backup (background). */
    public void backup(String projectId, String savedDocumentJson) {
        if (projectId == null || savedDocumentJson == null) {
            return;
        }
        executor.execute(() -> {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            db.beginTransaction();
            try {
                ContentValues values = new ContentValues();
                values.put("project_id", projectId);
                values.put("document_json", savedDocumentJson);
                values.put("created_at", System.currentTimeMillis());
                db.insertWithOnConflict(DatabaseHelper.TABLE_BACKUPS, null, values,
                        SQLiteDatabase.CONFLICT_REPLACE);
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }
        });
    }

    /** Synchronous read; call only from a background thread. */
    public String readBackup(String projectId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_BACKUPS, null,
                "project_id = ?", new String[]{projectId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return cursor.getString(
                        cursor.getColumnIndexOrThrow("document_json"));
            }
        }
        return null;
    }

    public boolean hasBackup(String projectId) {
        return readBackup(projectId) != null;
    }

    public void deleteBackup(String projectId) {
        if (projectId == null) {
            return;
        }
        executor.execute(() -> dbHelper.getWritableDatabase().delete(
                DatabaseHelper.TABLE_BACKUPS, "project_id = ?",
                new String[]{projectId}));
    }
}
