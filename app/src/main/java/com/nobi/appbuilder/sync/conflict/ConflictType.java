package com.nobi.appbuilder.sync.conflict;

/** What kind of disagreement a conflict is. */
public enum ConflictType {
    VISUAL_VS_CODE,
    EXTERNAL_CHANGE,
    DELETED_WHILE_OPEN,
    UNSUPPORTED_STRUCTURE,
    GENERATED_MODIFIED
}
