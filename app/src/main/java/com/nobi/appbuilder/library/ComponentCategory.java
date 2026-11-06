package com.nobi.appbuilder.library;

/**
 * Library browse categories. These are the sections of the
 * component library UI — separate from the editor palette groups
 * in components/ComponentCategory.
 */
public enum ComponentCategory {
    ALL("All Components"),
    LAYOUTS("Layouts"),
    TEXT("Text"),
    BUTTONS("Buttons"),
    INPUTS("Inputs"),
    IMAGES("Images"),
    SELECTION("Selection Controls"),
    CONTAINERS("Containers"),
    INDICATORS("Indicators"),
    DISPLAY("Display"),
    CUSTOM("Custom Components");

    private final String displayName;

    ComponentCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ComponentCategory fromName(String name) {
        if (name == null) {
            return ALL;
        }
        for (ComponentCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return ALL;
    }
}
