package com.nobi.appbuilder.assets;

/** Kinds of assets the manager handles. */
public enum AssetType {
    IMAGE("Image"),
    VECTOR("Vector drawable"),
    FONT("Font"),
    COLOR("Colour"),
    DRAWABLE("Drawable"),
    RAW("File");

    private final String displayName;

    AssetType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static AssetType fromName(String name) {
        if (name == null) {
            return RAW;
        }
        for (AssetType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return RAW;
    }

    public static AssetType forFileName(String fileName) {
        if (fileName == null) {
            return RAW;
        }
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png") || lower.endsWith(".jpg")
                || lower.endsWith(".jpeg") || lower.endsWith(".webp")
                || lower.endsWith(".gif")) {
            return IMAGE;
        }
        if (lower.endsWith(".xml")) {
            return VECTOR;
        }
        if (lower.endsWith(".ttf") || lower.endsWith(".otf")) {
            return FONT;
        }
        return RAW;
    }
}
