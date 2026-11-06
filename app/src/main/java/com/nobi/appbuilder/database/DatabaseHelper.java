package com.nobi.appbuilder.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite database for AppBuilder Lite. Five tables (spec section 8):
 * projects (metadata), screens (JSON design documents), assets,
 * project_history and project_settings. Foreign keys are enforced so
 * deleting a project removes its screens, assets and settings.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "appbuilder.db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_SCREENS = "screens";
    public static final String TABLE_ASSETS = "assets";
    public static final String TABLE_HISTORY = "project_history";
    public static final String TABLE_SETTINGS = "project_settings";

    private static final String CREATE_PROJECTS =
            "CREATE TABLE " + TABLE_PROJECTS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "app_name TEXT NOT NULL, "
                    + "package_name TEXT NOT NULL, "
                    + "min_sdk INTEGER NOT NULL, "
                    + "created_at INTEGER NOT NULL, "
                    + "updated_at INTEGER NOT NULL, "
                    + "thumbnail_path TEXT, "
                    + "schema_version INTEGER NOT NULL DEFAULT 1)";

    private static final String CREATE_SCREENS =
            "CREATE TABLE " + TABLE_SCREENS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "is_launcher INTEGER NOT NULL DEFAULT 0, "
                    + "sort_order INTEGER NOT NULL DEFAULT 0, "
                    + "design_json TEXT, "
                    + "created_at INTEGER NOT NULL, "
                    + "updated_at INTEGER NOT NULL)";

    private static final String CREATE_ASSETS =
            "CREATE TABLE " + TABLE_ASSETS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "type TEXT, "
                    + "path TEXT, "
                    + "size_bytes INTEGER NOT NULL DEFAULT 0, "
                    + "created_at INTEGER NOT NULL)";

    private static final String CREATE_HISTORY =
            "CREATE TABLE " + TABLE_HISTORY + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "project_id TEXT NOT NULL, "
                    + "action TEXT NOT NULL, "
                    + "detail TEXT, "
                    + "created_at INTEGER NOT NULL)";

    private static final String CREATE_SETTINGS =
            "CREATE TABLE " + TABLE_SETTINGS + " ("
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "setting_key TEXT NOT NULL, "
                    + "setting_value TEXT, "
                    + "PRIMARY KEY (project_id, setting_key))";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_PROJECTS);
        db.execSQL(CREATE_SCREENS);
        db.execSQL(CREATE_ASSETS);
        db.execSQL(CREATE_HISTORY);
        db.execSQL(CREATE_SETTINGS);
        db.execSQL("CREATE INDEX idx_screens_project ON " + TABLE_SCREENS + "(project_id)");
        db.execSQL("CREATE INDEX idx_assets_project ON " + TABLE_ASSETS + "(project_id)");
        db.execSQL("CREATE INDEX idx_history_project ON " + TABLE_HISTORY + "(project_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Version 1 is the first release. Future schema migrations go here,
        // one step at a time (oldVersion -> oldVersion + 1), never a wipe.
    }

    /** Appends one audit row. Call inside the caller's transaction. */
    public static void logHistory(SQLiteDatabase db, String projectId,
                                  String action, String detail) {
        db.execSQL("INSERT INTO " + TABLE_HISTORY
                        + " (project_id, action, detail, created_at) VALUES (?, ?, ?, ?)",
                new Object[]{projectId, action, detail, System.currentTimeMillis()});
    }
}
