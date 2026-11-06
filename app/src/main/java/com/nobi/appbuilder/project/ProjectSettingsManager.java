package com.nobi.appbuilder.project;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.model.ProjectModel;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Project settings (Phase 5, spec section 13): app name, package
 * name, version name/code, min/target SDK, app icon, default theme,
 * default orientation and starting screen. Settings live in the
 * project_settings table; app name / package / min SDK also update
 * the projects row. Everything is validated before saving, and
 * generated build files are never touched (export owns those).
 */
public class ProjectSettingsManager {

    public static final String KEY_VERSION_NAME = "version_name";
    public static final String KEY_VERSION_CODE = "version_code";
    public static final String KEY_TARGET_SDK = "target_sdk";
    public static final String KEY_APP_ICON = "app_icon";
    public static final String KEY_DEFAULT_THEME = "default_theme";
    public static final String KEY_DEFAULT_ORIENTATION = "default_orientation";
    public static final String KEY_STARTING_SCREEN = "starting_screen";

    private static final Pattern VERSION_NAME_PATTERN =
            Pattern.compile("^\\d+\\.\\d+(\\.\\d+)?$");

    private final DatabaseHelper dbHelper;
    private final ProjectRepository projectRepository;

    public ProjectSettingsManager(DatabaseHelper dbHelper,
                                  ProjectRepository projectRepository) {
        this.dbHelper = dbHelper;
        this.projectRepository = projectRepository;
    }

    public String getSetting(String projectId, String key, String fallback) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SETTINGS,
                new String[]{"setting_value"},
                "project_id = ? AND setting_key = ?",
                new String[]{projectId, key}, null, null, null)) {
            if (cursor.moveToFirst()) {
                String value = cursor.getString(
                        cursor.getColumnIndexOrThrow("setting_value"));
                return value == null ? fallback : value;
            }
        }
        return fallback;
    }

    public void setSetting(String projectId, String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("setting_key", key);
        values.put("setting_value", value);
        db.insertWithOnConflict(DatabaseHelper.TABLE_SETTINGS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** Validates the whole settings form; error string or null. */
    public String validate(String appName, String packageName,
                           String versionName, String versionCodeText,
                           int minSdk, int targetSdk, String startingScreenId,
                           List<String> screenIds) {
        if (!ProjectModel.isValidAppName(appName)) {
            return "Please enter a valid app name.";
        }
        if (!ProjectModel.isValidPackageName(packageName)) {
            return "Package name must look like com.example.myapp.";
        }
        if (versionName == null
                || !VERSION_NAME_PATTERN.matcher(versionName.trim()).matches()) {
            return "Version name must look like 1.0 or 1.0.0.";
        }
        int versionCode;
        try {
            versionCode = Integer.parseInt(versionCodeText.trim());
        } catch (Exception e) {
            return "Version code must be a whole number.";
        }
        if (versionCode < 1) {
            return "Version code must be 1 or higher.";
        }
        if (minSdk < 26 || minSdk > 35) {
            return "Minimum SDK must be between 26 and 35.";
        }
        if (targetSdk < minSdk || targetSdk > 35) {
            return "Target SDK must be between the minimum SDK and 35.";
        }
        if (startingScreenId != null && !startingScreenId.isEmpty()
                && screenIds != null && !screenIds.contains(startingScreenId)) {
            return "Starting screen no longer exists.";
        }
        return null;
    }

    /** Applies validated settings to the project row + settings table. */
    public boolean apply(String projectId, String appName, String packageName,
                         String versionName, String versionCodeText,
                         int minSdk, int targetSdk, String appIcon,
                         String defaultTheme, String defaultOrientation,
                         String startingScreenId) {
        ProjectModel project = projectRepository.getById(projectId);
        if (project == null) {
            return false;
        }
        project.setAppName(appName.trim());
        project.setPackageName(packageName.trim());
        project.setMinSdk(minSdk);
        if (!projectRepository.update(project)) {
            return false;
        }
        setSetting(projectId, KEY_VERSION_NAME, versionName.trim());
        setSetting(projectId, KEY_VERSION_CODE, versionCodeText.trim());
        setSetting(projectId, KEY_TARGET_SDK, String.valueOf(targetSdk));
        setSetting(projectId, KEY_APP_ICON, appIcon == null ? "" : appIcon);
        setSetting(projectId, KEY_DEFAULT_THEME,
                defaultTheme == null ? "" : defaultTheme);
        setSetting(projectId, KEY_DEFAULT_ORIENTATION,
                defaultOrientation == null ? "portrait" : defaultOrientation);
        setSetting(projectId, KEY_STARTING_SCREEN,
                startingScreenId == null ? "" : startingScreenId);
        return true;
    }
}
