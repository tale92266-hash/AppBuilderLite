package com.nobi.appbuilder.build.logs;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite build history (table build_history, DB v4) plus log-file
 * references. Deleting a history record never deletes the APK file.
 */
public class BuildLogRepository {

    /** One build history row. */
    public static class BuildRecord {
        public long id;
        public String buildId;
        public String projectId;
        public String projectName;
        public String buildType;
        public String status;
        public long startTime;
        public long endTime;
        public long durationMs;
        public String apkPath;
        public long apkSize;
        public int errorCount;
        public int warningCount;
        public String logPath;

        public long getDuration() {
            return endTime > startTime ? endTime - startTime : durationMs;
        }
    }

    private final DatabaseHelper dbHelper;

    public BuildLogRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public long insert(BuildResult result, String projectName) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("build_id", result.getBuildId());
        values.put("project_id", result.getProjectId());
        values.put("project_name", projectName);
        values.put("build_type", result.getConfiguration().getBuildType());
        values.put("status", result.getStatus().name());
        values.put("start_time", result.getStartTimeMs());
        values.put("end_time", result.getEndTimeMs());
        values.put("duration_ms", result.getDurationMs());
        values.put("apk_path", result.getApkFile() == null ? null
                : result.getApkFile().getAbsolutePath());
        values.put("apk_size", result.getApkFile() == null ? 0
                : result.getApkFile().length());
        values.put("error_count", result.getErrorCount());
        values.put("warning_count", result.getWarningCount());
        values.put("log_path", result.getLogPath());
        return db.insert(DatabaseHelper.TABLE_BUILD_HISTORY, null, values);
    }

    public List<BuildRecord> getByProject(String projectId) {
        List<BuildRecord> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_BUILD_HISTORY,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "start_time DESC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    public List<BuildRecord> getAll() {
        List<BuildRecord> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_BUILD_HISTORY,
                null, null, null, null, null, "start_time DESC")) {
            while (cursor.moveToNext()) {
                out.add(fromCursor(cursor));
            }
        }
        return out;
    }

    /** Deletes the record only — never the APK or log file. */
    public boolean delete(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_BUILD_HISTORY,
                "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    private BuildRecord fromCursor(Cursor cursor) {
        BuildRecord record = new BuildRecord();
        record.id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        record.buildId = cursor.getString(
                cursor.getColumnIndexOrThrow("build_id"));
        record.projectId = cursor.getString(
                cursor.getColumnIndexOrThrow("project_id"));
        record.projectName = cursor.getString(
                cursor.getColumnIndexOrThrow("project_name"));
        record.buildType = cursor.getString(
                cursor.getColumnIndexOrThrow("build_type"));
        record.status = cursor.getString(
                cursor.getColumnIndexOrThrow("status"));
        record.startTime = cursor.getLong(
                cursor.getColumnIndexOrThrow("start_time"));
        record.endTime = cursor.getLong(
                cursor.getColumnIndexOrThrow("end_time"));
        record.durationMs = cursor.getLong(
                cursor.getColumnIndexOrThrow("duration_ms"));
        record.apkPath = cursor.getString(
                cursor.getColumnIndexOrThrow("apk_path"));
        record.apkSize = cursor.getLong(
                cursor.getColumnIndexOrThrow("apk_size"));
        record.errorCount = cursor.getInt(
                cursor.getColumnIndexOrThrow("error_count"));
        record.warningCount = cursor.getInt(
                cursor.getColumnIndexOrThrow("warning_count"));
        record.logPath = cursor.getString(
                cursor.getColumnIndexOrThrow("log_path"));
        return record;
    }
}
