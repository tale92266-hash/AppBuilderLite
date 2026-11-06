package com.nobi.appbuilder.export.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/** SQLite export history (table export_history, DB v3). */
public class ExportHistoryRepository {

    /** One history row. */
    public static class ExportRecord {
        public long id;
        public String projectId;
        public String projectName;
        public String zipFileName;
        public long exportDate;
        public long fileSize;
        public String outputUri;
        public String status;
        public String validationSummary;

        public boolean isFileMissing(java.io.File exportsDir) {
            if (zipFileName == null) {
                return true;
            }
            return !new java.io.File(exportsDir, zipFileName).exists()
                    && (outputUri == null
                    || !outputUri.startsWith("content://"));
        }
    }

    public static final String STATUS_SUCCESS = "success";
    public static final String STATUS_FAILED = "failed";

    private final DatabaseHelper dbHelper;

    public ExportHistoryRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(ExportRecord record) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", record.projectId);
        values.put("project_name", record.projectName);
        values.put("zip_filename", record.zipFileName);
        values.put("export_date", record.exportDate);
        values.put("file_size", record.fileSize);
        values.put("output_uri", record.outputUri);
        values.put("status", record.status);
        values.put("validation_summary", record.validationSummary);
        return db.insert(DatabaseHelper.TABLE_EXPORT_HISTORY, null, values);
    }

    public List<ExportRecord> getByProject(String projectId) {
        List<ExportRecord> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_EXPORT_HISTORY,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "export_date DESC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public List<ExportRecord> getAll() {
        List<ExportRecord> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_EXPORT_HISTORY,
                null, null, null, null, null, "export_date DESC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    /** Deletes the history record only — never the ZIP file itself. */
    public boolean delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_EXPORT_HISTORY,
                "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private ExportRecord fromCursor(Cursor cursor) {
        ExportRecord record = new ExportRecord();
        record.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        record.projectId = cursor.getString(
                cursor.getColumnIndexOrThrow("project_id"));
        record.projectName = cursor.getString(
                cursor.getColumnIndexOrThrow("project_name"));
        record.zipFileName = cursor.getString(
                cursor.getColumnIndexOrThrow("zip_filename"));
        record.exportDate = cursor.getLong(
                cursor.getColumnIndexOrThrow("export_date"));
        record.fileSize = cursor.getLong(
                cursor.getColumnIndexOrThrow("file_size"));
        record.outputUri = cursor.getString(
                cursor.getColumnIndexOrThrow("output_uri"));
        record.status = cursor.getString(
                cursor.getColumnIndexOrThrow("status"));
        record.validationSummary = cursor.getString(
                cursor.getColumnIndexOrThrow("validation_summary"));
        return record;
    }
}
