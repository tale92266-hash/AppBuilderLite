package com.nobi.appbuilder.properties;

/** Collapsible sections of the properties inspector. */
public enum PropertyGroup {
    GENERAL("General"),
    LAYOUT("Layout"),
    SPACING("Spacing"),
    APPEARANCE("Appearance"),
    TYPOGRAPHY("Typography"),
    ADVANCED("Advanced");

    private final String displayName;

    PropertyGroup(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
