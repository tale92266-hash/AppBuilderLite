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
    public static final int DATABASE_VERSION = 6;

    public static final String TABLE_PROJECTS = "projects";
    public static final String TABLE_SCREENS = "screens";
    public static final String TABLE_ASSETS = "assets";
    public static final String TABLE_HISTORY = "project_history";
    public static final String TABLE_SETTINGS = "project_settings";
    public static final String TABLE_RECOVERY = "recovery_snapshots";
    public static final String TABLE_BACKUPS = "project_backups";
    public static final String TABLE_EXPORT_HISTORY = "export_history";
    public static final String TABLE_BUILD_HISTORY = "build_history";
    public static final String TABLE_COMPONENT_LIBRARY = "component_library";
    public static final String TABLE_CUSTOM_COMPONENTS = "custom_components";
    public static final String TABLE_CUSTOM_COMPONENT_VERSIONS =
            "custom_component_versions";
    public static final String TABLE_COMPONENT_FAVORITES = "component_favorites";
    public static final String TABLE_COMPONENT_USAGE = "component_usage";
    public static final String TABLE_SCREEN_TEMPLATES = "screen_templates";
    public static final String TABLE_DESIGN_SYSTEMS = "design_systems";
    public static final String TABLE_DESIGN_TOKENS = "design_tokens";
    public static final String TABLE_PROJECT_THEMES = "project_themes";
    public static final String TABLE_COMPONENT_PRESETS = "component_presets";
    public static final String TABLE_ASSET_FOLDERS = "asset_folders";
    public static final String TABLE_ASSET_METADATA = "asset_metadata";
    public static final String TABLE_ASSET_REFERENCES = "asset_references";
    public static final String TABLE_ASSET_FAVORITES = "asset_favorites";
    public static final String TABLE_ASSET_USAGE = "asset_usage";
    public static final String TABLE_VECTOR_ASSETS = "vector_assets";
    public static final String TABLE_FONT_ASSETS = "font_assets";
    public static final String TABLE_COLOR_RESOURCES = "color_resources";
    public static final String TABLE_DRAWABLE_RESOURCES = "drawable_resources";

    private static final String CREATE_COMPONENT_LIBRARY =
            "CREATE TABLE " + TABLE_COMPONENT_LIBRARY + " ("
                    + "id TEXT NOT NULL, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "base_type TEXT NOT NULL, "
                    + "variant TEXT, "
                    + "display_name TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "description TEXT, "
                    + "is_custom INTEGER NOT NULL DEFAULT 0, "
                    + "is_supported INTEGER NOT NULL DEFAULT 1, "
                    + "unsupported_reason TEXT, "
                    + "custom_component_id TEXT, "
                    + "updated_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (id, project_id))";
    private static final String CREATE_CUSTOM_COMPONENTS =
            "CREATE TABLE " + TABLE_CUSTOM_COMPONENTS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "description TEXT, "
                    + "root_type TEXT NOT NULL, "
                    + "components_json TEXT NOT NULL, "
                    + "public_properties_json TEXT, "
                    + "version INTEGER NOT NULL DEFAULT 1, "
                    + "created_at INTEGER NOT NULL DEFAULT 0, "
                    + "updated_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_CUSTOM_COMPONENT_VERSIONS =
            "CREATE TABLE " + TABLE_CUSTOM_COMPONENT_VERSIONS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "component_id TEXT NOT NULL REFERENCES " + TABLE_CUSTOM_COMPONENTS + "(id) ON DELETE CASCADE, "
                    + "project_id TEXT NOT NULL, "
                    + "version INTEGER NOT NULL, "
                    + "definition_json TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_COMPONENT_FAVORITES =
            "CREATE TABLE " + TABLE_COMPONENT_FAVORITES + " ("
                    + "project_id TEXT NOT NULL, "
                    + "item_id TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (project_id, item_id))";
    private static final String CREATE_COMPONENT_USAGE =
            "CREATE TABLE " + TABLE_COMPONENT_USAGE + " ("
                    + "project_id TEXT NOT NULL, "
                    + "item_id TEXT NOT NULL, "
                    + "use_count INTEGER NOT NULL DEFAULT 1, "
                    + "last_used_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (project_id, item_id))";
    private static final String CREATE_SCREEN_TEMPLATES =
            "CREATE TABLE " + TABLE_SCREEN_TEMPLATES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "category TEXT NOT NULL, "
                    + "description TEXT, "
                    + "version INTEGER NOT NULL DEFAULT 1, "
                    + "screen_name TEXT NOT NULL, "
                    + "screen_json TEXT NOT NULL, "
                    + "is_built_in INTEGER NOT NULL DEFAULT 0, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_DESIGN_SYSTEMS =
            "CREATE TABLE " + TABLE_DESIGN_SYSTEMS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_DESIGN_TOKENS =
            "CREATE TABLE " + TABLE_DESIGN_TOKENS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "kind TEXT NOT NULL, "
                    + "name TEXT NOT NULL, "
                    + "value TEXT NOT NULL, "
                    + "updated_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_PROJECT_THEMES =
            "CREATE TABLE " + TABLE_PROJECT_THEMES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "is_dark INTEGER NOT NULL DEFAULT 0, "
                    + "primary_color TEXT NOT NULL, "
                    + "primary_dark_color TEXT NOT NULL, "
                    + "accent_color TEXT NOT NULL, "
                    + "background_color TEXT NOT NULL, "
                    + "surface_color TEXT NOT NULL, "
                    + "text_color TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    private static final String CREATE_COMPONENT_PRESETS =
            "CREATE TABLE " + TABLE_COMPONENT_PRESETS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "component_type TEXT NOT NULL, "
                    + "properties_json TEXT, "
                    + "is_built_in INTEGER NOT NULL DEFAULT 0, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";

    /** Phase 13: asset folders (flat list, parent_id for nesting). */
    private static final String CREATE_ASSET_FOLDERS =
            "CREATE TABLE " + TABLE_ASSET_FOLDERS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "parent_id TEXT, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    /** Phase 13: per-asset image metadata, one row per asset. */
    private static final String CREATE_ASSET_METADATA =
            "CREATE TABLE " + TABLE_ASSET_METADATA + " ("
                    + "asset_id TEXT PRIMARY KEY REFERENCES " + TABLE_ASSETS + "(id) ON DELETE CASCADE, "
                    + "width INTEGER NOT NULL DEFAULT 0, "
                    + "height INTEGER NOT NULL DEFAULT 0, "
                    + "format TEXT, "
                    + "animated INTEGER NOT NULL DEFAULT 0, "
                    + "duration_ms INTEGER NOT NULL DEFAULT 0)";
    /** Phase 13: recorded IR references (asset used by a component). */
    private static final String CREATE_ASSET_REFERENCES =
            "CREATE TABLE " + TABLE_ASSET_REFERENCES + " ("
                    + "asset_id TEXT NOT NULL REFERENCES " + TABLE_ASSETS + "(id) ON DELETE CASCADE, "
                    + "project_id TEXT NOT NULL, "
                    + "screen_id TEXT NOT NULL, "
                    + "component_id TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (asset_id, screen_id, component_id))";
    /** Phase 13: favorite assets per project (spec table). */
    private static final String CREATE_ASSET_FAVORITES =
            "CREATE TABLE " + TABLE_ASSET_FAVORITES + " ("
                    + "project_id TEXT NOT NULL, "
                    + "asset_id TEXT NOT NULL REFERENCES " + TABLE_ASSETS + "(id) ON DELETE CASCADE, "
                    + "created_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (project_id, asset_id))";
    /** Phase 13: recorded usage counts per project/asset. */
    private static final String CREATE_ASSET_USAGE =
            "CREATE TABLE " + TABLE_ASSET_USAGE + " ("
                    + "project_id TEXT NOT NULL, "
                    + "asset_id TEXT NOT NULL REFERENCES " + TABLE_ASSETS + "(id) ON DELETE CASCADE, "
                    + "use_count INTEGER NOT NULL DEFAULT 1, "
                    + "last_used_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (project_id, asset_id))";
    /** Phase 13: vector assets (paths stored as JSON). */
    private static final String CREATE_VECTOR_ASSETS =
            "CREATE TABLE " + TABLE_VECTOR_ASSETS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "viewport_width REAL NOT NULL DEFAULT 24, "
                    + "viewport_height REAL NOT NULL DEFAULT 24, "
                    + "width_dp REAL NOT NULL DEFAULT 24, "
                    + "height_dp REAL NOT NULL DEFAULT 24, "
                    + "paths_json TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    /** Phase 13: imported font files. */
    private static final String CREATE_FONT_ASSETS =
            "CREATE TABLE " + TABLE_FONT_ASSETS + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "family TEXT, "
                    + "file_name TEXT NOT NULL, "
                    + "path TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";
    /** Phase 13: colour + gradient resources (name is unique per project). */
    private static final String CREATE_COLOR_RESOURCES =
            "CREATE TABLE " + TABLE_COLOR_RESOURCES + " ("
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "kind TEXT NOT NULL, "
                    + "value TEXT NOT NULL, "
                    + "updated_at INTEGER NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (project_id, name))";
    /** Phase 13: generated drawable XML resources. */
    private static final String CREATE_DRAWABLE_RESOURCES =
            "CREATE TABLE " + TABLE_DRAWABLE_RESOURCES + " ("
                    + "id TEXT PRIMARY KEY, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "name TEXT NOT NULL, "
                    + "kind TEXT NOT NULL, "
                    + "xml TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL DEFAULT 0)";

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

    /** Phase 5: one recovery snapshot per project (latest unsaved state). */
    private static final String CREATE_RECOVERY =
            "CREATE TABLE " + TABLE_RECOVERY + " ("
                    + "project_id TEXT PRIMARY KEY REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "screen_id TEXT, "
                    + "document_json TEXT NOT NULL, "
                    + "saved_at INTEGER NOT NULL, "
                    + "snapshot_at INTEGER NOT NULL)";

    /** Phase 5: one backup per project (last saved document before overwrite). */
    private static final String CREATE_BACKUPS =
            "CREATE TABLE " + TABLE_BACKUPS + " ("
                    + "project_id TEXT PRIMARY KEY REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "document_json TEXT NOT NULL, "
                    + "created_at INTEGER NOT NULL)";

    private static final String CREATE_EXPORT_HISTORY =
            "CREATE TABLE " + TABLE_EXPORT_HISTORY + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "project_name TEXT NOT NULL, "
                    + "zip_filename TEXT NOT NULL, "
                    + "export_date INTEGER NOT NULL, "
                    + "file_size INTEGER NOT NULL DEFAULT 0, "
                    + "output_uri TEXT, "
                    + "status TEXT NOT NULL DEFAULT 'success', "
                    + "validation_summary TEXT)";

    private static final String CREATE_BUILD_HISTORY =
            "CREATE TABLE " + TABLE_BUILD_HISTORY + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "build_id TEXT NOT NULL, "
                    + "project_id TEXT NOT NULL REFERENCES " + TABLE_PROJECTS + "(id) ON DELETE CASCADE, "
                    + "project_name TEXT NOT NULL, "
                    + "build_type TEXT NOT NULL DEFAULT 'debug', "
                    + "status TEXT NOT NULL, "
                    + "start_time INTEGER NOT NULL, "
                    + "end_time INTEGER NOT NULL DEFAULT 0, "
                    + "duration_ms INTEGER NOT NULL DEFAULT 0, "
                    + "apk_path TEXT, "
                    + "apk_size INTEGER NOT NULL DEFAULT 0, "
                    + "error_count INTEGER NOT NULL DEFAULT 0, "
                    + "warning_count INTEGER NOT NULL DEFAULT 0, "
                    + "log_path TEXT)";

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
        db.execSQL(CREATE_RECOVERY);
        db.execSQL(CREATE_BACKUPS);
        db.execSQL(CREATE_EXPORT_HISTORY);
        db.execSQL(CREATE_BUILD_HISTORY);
        db.execSQL(CREATE_COMPONENT_LIBRARY);
        db.execSQL(CREATE_CUSTOM_COMPONENTS);
        db.execSQL(CREATE_CUSTOM_COMPONENT_VERSIONS);
        db.execSQL(CREATE_COMPONENT_FAVORITES);
        db.execSQL(CREATE_COMPONENT_USAGE);
        db.execSQL(CREATE_SCREEN_TEMPLATES);
        db.execSQL(CREATE_DESIGN_SYSTEMS);
        db.execSQL(CREATE_DESIGN_TOKENS);
        db.execSQL(CREATE_PROJECT_THEMES);
        db.execSQL(CREATE_COMPONENT_PRESETS);
        db.execSQL(CREATE_ASSET_FOLDERS);
        db.execSQL(CREATE_ASSET_METADATA);
        db.execSQL(CREATE_ASSET_REFERENCES);
        db.execSQL(CREATE_ASSET_FAVORITES);
        db.execSQL(CREATE_ASSET_USAGE);
        db.execSQL(CREATE_VECTOR_ASSETS);
        db.execSQL(CREATE_FONT_ASSETS);
        db.execSQL(CREATE_COLOR_RESOURCES);
        db.execSQL(CREATE_DRAWABLE_RESOURCES);
        db.execSQL("CREATE INDEX idx_screens_project ON " + TABLE_SCREENS + "(project_id)");
        db.execSQL("CREATE INDEX idx_assets_project ON " + TABLE_ASSETS + "(project_id)");
        db.execSQL("CREATE INDEX idx_history_project ON " + TABLE_HISTORY + "(project_id)");
        db.execSQL("CREATE INDEX idx_export_project ON " + TABLE_EXPORT_HISTORY + "(project_id)");
        db.execSQL("CREATE INDEX idx_build_project ON " + TABLE_BUILD_HISTORY + "(project_id)");
        db.execSQL("CREATE INDEX idx_library_project ON " + TABLE_COMPONENT_LIBRARY + "(project_id)");
        db.execSQL("CREATE INDEX idx_templates_project ON " + TABLE_SCREEN_TEMPLATES + "(project_id)");
        db.execSQL("CREATE INDEX idx_tokens_project ON " + TABLE_DESIGN_TOKENS + "(project_id)");
        db.execSQL("CREATE INDEX idx_themes_project ON " + TABLE_PROJECT_THEMES + "(project_id)");
        db.execSQL("CREATE INDEX idx_asset_folders_project ON " + TABLE_ASSET_FOLDERS + "(project_id)");
        db.execSQL("CREATE INDEX idx_asset_refs_asset ON " + TABLE_ASSET_REFERENCES + "(asset_id)");
        db.execSQL("CREATE INDEX idx_vector_project ON " + TABLE_VECTOR_ASSETS + "(project_id)");
        db.execSQL("CREATE INDEX idx_font_project ON " + TABLE_FONT_ASSETS + "(project_id)");
        db.execSQL("CREATE INDEX idx_drawable_project ON " + TABLE_DRAWABLE_RESOURCES + "(project_id)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Step-by-step migrations, never a wipe.
        if (oldVersion < 2) {
            db.execSQL(CREATE_RECOVERY);
            db.execSQL(CREATE_BACKUPS);
            db.execSQL("ALTER TABLE " + TABLE_SCREENS
                    + " ADD COLUMN screen_type TEXT NOT NULL DEFAULT 'activity'");
            db.execSQL("ALTER TABLE " + TABLE_SCREENS
                    + " ADD COLUMN orientation TEXT NOT NULL DEFAULT 'portrait'");
        }
        if (oldVersion < 3) {
            db.execSQL(CREATE_EXPORT_HISTORY);
            db.execSQL("CREATE INDEX idx_export_project ON "
                    + TABLE_EXPORT_HISTORY + "(project_id)");
        }
        if (oldVersion < 4) {
            db.execSQL(CREATE_BUILD_HISTORY);
            db.execSQL("CREATE INDEX idx_build_project ON "
                    + TABLE_BUILD_HISTORY + "(project_id)");
        }
        if (oldVersion < 5) {
            db.execSQL(CREATE_COMPONENT_LIBRARY);
            db.execSQL(CREATE_CUSTOM_COMPONENTS);
            db.execSQL(CREATE_CUSTOM_COMPONENT_VERSIONS);
            db.execSQL(CREATE_COMPONENT_FAVORITES);
            db.execSQL(CREATE_COMPONENT_USAGE);
            db.execSQL(CREATE_SCREEN_TEMPLATES);
            db.execSQL(CREATE_DESIGN_SYSTEMS);
            db.execSQL(CREATE_DESIGN_TOKENS);
            db.execSQL(CREATE_PROJECT_THEMES);
            db.execSQL(CREATE_COMPONENT_PRESETS);
            db.execSQL("CREATE INDEX idx_library_project ON "
                    + TABLE_COMPONENT_LIBRARY + "(project_id)");
            db.execSQL("CREATE INDEX idx_templates_project ON "
                    + TABLE_SCREEN_TEMPLATES + "(project_id)");
            db.execSQL("CREATE INDEX idx_tokens_project ON "
                    + TABLE_DESIGN_TOKENS + "(project_id)");
            db.execSQL("CREATE INDEX idx_themes_project ON "
                    + TABLE_PROJECT_THEMES + "(project_id)");
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_ASSETS + " ADD COLUMN folder_id TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ASSETS + " ADD COLUMN hash TEXT");
            db.execSQL("ALTER TABLE " + TABLE_ASSETS + " ADD COLUMN resource_name TEXT");
            db.execSQL(CREATE_ASSET_FOLDERS);
            db.execSQL(CREATE_ASSET_METADATA);
            db.execSQL(CREATE_ASSET_REFERENCES);
            db.execSQL(CREATE_ASSET_FAVORITES);
            db.execSQL(CREATE_ASSET_USAGE);
            db.execSQL(CREATE_VECTOR_ASSETS);
            db.execSQL(CREATE_FONT_ASSETS);
            db.execSQL(CREATE_COLOR_RESOURCES);
            db.execSQL(CREATE_DRAWABLE_RESOURCES);
            db.execSQL("CREATE INDEX idx_asset_folders_project ON "
                    + TABLE_ASSET_FOLDERS + "(project_id)");
            db.execSQL("CREATE INDEX idx_asset_refs_asset ON "
                    + TABLE_ASSET_REFERENCES + "(asset_id)");
            db.execSQL("CREATE INDEX idx_vector_project ON "
                    + TABLE_VECTOR_ASSETS + "(project_id)");
            db.execSQL("CREATE INDEX idx_font_project ON "
                    + TABLE_FONT_ASSETS + "(project_id)");
            db.execSQL("CREATE INDEX idx_drawable_project ON "
                    + TABLE_DRAWABLE_RESOURCES + "(project_id)");
        }
    }

    /** Appends one audit row. Call inside the caller's transaction. */
    public static void logHistory(SQLiteDatabase db, String projectId,
                                  String action, String detail) {
        db.execSQL("INSERT INTO " + TABLE_HISTORY
                        + " (project_id, action, detail, created_at) VALUES (?, ?, ?, ?)",
                new Object[]{projectId, action, detail, System.currentTimeMillis()});
    }
}
