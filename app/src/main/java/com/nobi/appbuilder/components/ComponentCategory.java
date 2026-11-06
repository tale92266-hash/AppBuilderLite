package com.nobi.appbuilder.components;

/** Palette groups shown in the editor's component library (Phase 3). */
public enum ComponentCategory {
    LAYOUT("Layout"),
    BASIC("Basic"),
    INPUT("Input"),
    DISPLAY("Display");

    private final String displayName;

    ComponentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
