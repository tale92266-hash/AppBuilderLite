package com.nobi.appbuilder.assets;

/** Folders/sections of the asset library UI. */
public enum AssetCategory {
    ALL("All Assets"),
    IMAGES("Images"),
    VECTORS("Vectors"),
    FONTS("Fonts"),
    COLORS("Colours"),
    DRAWABLES("Drawables"),
    OTHER("Other");

    private final String displayName;

    AssetCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AssetCategory forType(AssetType type) {
        if (type == null) {
            return OTHER;
        }
        switch (type) {
            case IMAGE:
                return IMAGES;
            case VECTOR:
                return VECTORS;
            case FONT:
                return FONTS;
            case COLOR:
                return COLORS;
            case DRAWABLE:
                return DRAWABLES;
            default:
                return OTHER;
        }
    }

    public static AssetCategory fromName(String name) {
        if (name == null) {
            return ALL;
        }
        for (AssetCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return ALL;
    }
}
