package com.nobi.appbuilder.sync;

/**
 * Where a change came from. Every sync decision records its origin
 * so a generated update is never mistaken for an independent manual
 * edit — the core of the infinite-loop prevention.
 */
public enum ChangeOrigin {
    VISUAL_EDITOR,
    CODE_EDITOR,
    EXTERNAL_FILE,
    GENERATOR,
    PREVIEW,
    RECOVERY
}
