package com.nobi.appbuilder.ir;

/**
 * Constants for the JSON document engine: schema versions, document keys
 * and validation limits. Nothing here is user data; keep it in sync with
 * IRDocument, IRValidator and IRVersionManager.
 */
public final class IRConstants {

    private IRConstants() {
    }

    /** Current document schema version. Bump only with a migration. */
    public static final int CURRENT_SCHEMA_VERSION = 1;

    /** Oldest schema version the engine can still migrate. */
    public static final int MIN_SUPPORTED_SCHEMA_VERSION = 1;

    /** Top-level JSON keys of an IR document. */
    public static final String KEY_SCHEMA_VERSION = "schemaVersion";
    public static final String KEY_PROJECT = "project";
    public static final String KEY_SCREENS = "screens";
    public static final String KEY_THEME = "theme";
    public static final String KEY_ASSETS = "assets";
    public static final String KEY_EDITOR_METADATA = "editorMetadata";
    public static final String KEY_ROOT = "root";
    public static final String KEY_DESIGN_JSON = "designJson";

    /** Component JSON keys. */
    public static final String KEY_ID = "id";
    public static final String KEY_TYPE = "type";
    public static final String KEY_PROPERTIES = "properties";
    public static final String KEY_LAYOUT = "layout";
    public static final String KEY_CHILDREN = "children";
    public static final String KEY_EVENTS = "events";

    /** Validation limits. */
    public static final int MIN_SDK = 26;
    public static final int MAX_SDK = 34;
    public static final int MAX_DIMENSION_DP = 4000;
    public static final int MAX_COMPONENTS_PER_SCREEN = 1000;
    public static final int MAX_TEXT_LENGTH = 10000;
    public static final int MAX_SCREENS_PER_PROJECT = 50;

    /** Event actions understood by the Java generator (Phase 7). */
    public static final String ACTION_NONE = "none";
    public static final String ACTION_TOAST = "toast";
    public static final String ACTION_NAVIGATE = "navigate";
}
