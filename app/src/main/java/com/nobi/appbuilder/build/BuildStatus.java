package com.nobi.appbuilder.build;

/** Lifecycle of one build run. */
public enum BuildStatus {
    PENDING,
    RUNNING,
    SUCCESS,
    FAILED,
    CANCELLED,
    /** Local compilation is unavailable; the ZIP workflow applies. */
    EXTERNAL_BUILD_REQUIRED;

    public boolean isTerminal() {
        return this == SUCCESS || this == FAILED || this == CANCELLED
                || this == EXTERNAL_BUILD_REQUIRED;
    }
}
