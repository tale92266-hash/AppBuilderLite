package com.nobi.appbuilder.library;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One row of the component library: either a built-in component
 * (baseType = a registered type, variant = preset property
 * overrides) or a custom component (baseType = "custom",
 * customComponentId set). Favorites and recent-use live in the DB.
 */
public class ComponentLibraryItem {

    private final String id;
    private final String baseType;
    private final String variant;
    private final String displayName;
    private final ComponentCategory category;
    private final String description;
    private final Map<String, String> defaultProperties;
    private final boolean supported;
    private final String unsupportedReason;
    private final String customComponentId;
    private boolean favorite;
    private long lastUsedAt;

    public ComponentLibraryItem(String id, String baseType, String variant,
                                String displayName, ComponentCategory category,
                                String description,
                                Map<String, String> defaultProperties,
                                boolean supported, String unsupportedReason,
                                String customComponentId) {
        this.id = id;
        this.baseType = baseType;
        this.variant = variant;
        this.displayName = displayName;
        this.category = category;
        this.description = description;
        this.defaultProperties = defaultProperties != null
                ? new LinkedHashMap<>(defaultProperties) : new LinkedHashMap<>();
        this.supported = supported;
        this.unsupportedReason = unsupportedReason;
        this.customComponentId = customComponentId;
    }

    public static ComponentLibraryItem builtIn(String baseType,
                                               String variant,
                                               String displayName,
                                               ComponentCategory category,
                                               String description,
                                               Map<String, String> defaultProperties) {
        return new ComponentLibraryItem(
                baseType + (variant == null ? "" : ":" + variant),
                baseType, variant, displayName, category, description,
                defaultProperties, true, null, null);
    }

    public static ComponentLibraryItem unsupported(String baseType,
                                                   String displayName,
                                                   ComponentCategory category,
                                                   String description,
                                                   String reason) {
        return new ComponentLibraryItem("unsupported:" + baseType,
                baseType, null, displayName, category, description,
                new LinkedHashMap<>(), false, reason, null);
    }

    public String getId() {
        return id;
    }

    public String getBaseType() {
        return baseType;
    }

    public String getVariant() {
        return variant;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ComponentCategory getCategory() {
        return category;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, String> getDefaultProperties() {
        return new LinkedHashMap<>(defaultProperties);
    }

    public boolean isSupported() {
        return supported;
    }

    public String getUnsupportedReason() {
        return unsupportedReason;
    }

    public boolean isCustom() {
        return customComponentId != null;
    }

    public String getCustomComponentId() {
        return customComponentId;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }

    public long getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(long lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
