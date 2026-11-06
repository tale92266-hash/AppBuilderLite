package com.nobi.appbuilder.library;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Static metadata for one component type, built on top of the
 * Phase 2 ComponentDefinition. A type is only addable when its
 * canvas, preview and XML mappings all exist — anything else is
 * listed as unsupported, never faked.
 */
public class ComponentMetadata {

    private final String type;
    private final String displayName;
    private final ComponentCategory category;
    private final String description;
    private final Map<String, String> defaultProperties;
    private final List<String> supportedProperties;
    private final List<String> allowedParents;
    private final boolean childSupport;
    private final boolean previewSupported;
    private final boolean xmlSupported;
    private final List<String> requiredDependencies;
    private final int minSdk;
    private final String version;

    public ComponentMetadata(String type, String displayName,
                             ComponentCategory category, String description,
                             Map<String, String> defaultProperties,
                             List<String> supportedProperties,
                             List<String> allowedParents,
                             boolean childSupport,
                             boolean previewSupported,
                             boolean xmlSupported,
                             List<String> requiredDependencies,
                             int minSdk, String version) {
        this.type = type;
        this.displayName = displayName;
        this.category = category;
        this.description = description;
        this.defaultProperties = defaultProperties != null
                ? new LinkedHashMap<>(defaultProperties) : new LinkedHashMap<>();
        this.supportedProperties = supportedProperties != null
                ? new ArrayList<>(supportedProperties) : new ArrayList<>();
        this.allowedParents = allowedParents != null
                ? new ArrayList<>(allowedParents) : new ArrayList<>();
        this.childSupport = childSupport;
        this.previewSupported = previewSupported;
        this.xmlSupported = xmlSupported;
        this.requiredDependencies = requiredDependencies != null
                ? new ArrayList<>(requiredDependencies) : new ArrayList<>();
        this.minSdk = minSdk;
        this.version = version;
    }

    /** Metadata derived from a registered ComponentDefinition. */
    public static ComponentMetadata fromDefinition(ComponentDefinition definition,
                                                   ComponentCategory category,
                                                   String description) {
        return new ComponentMetadata(
                definition.getType(), definition.getDisplayName(),
                category, description,
                definition.getDefaultProperties(),
                new ArrayList<>(definition.getDefaultProperties().keySet()),
                definition.getSupportedParents(),
                definition.isContainer(),
                definition.getPreviewBuilder() != null,
                definition.getXmlProvider() != null,
                new ArrayList<>(), 26, "1.0");
    }

    /** A catalog-only entry for a type that is NOT addable yet. */
    public static ComponentMetadata unsupported(String type, String displayName,
                                                ComponentCategory category,
                                                String description,
                                                List<String> requiredDependencies) {
        return new ComponentMetadata(type, displayName, category, description,
                new LinkedHashMap<>(), new ArrayList<>(), new ArrayList<>(),
                false, false, false, requiredDependencies, 26, "1.0");
    }

    public List<String> validate() {
        List<String> errors = new ArrayList<>();
        if (type == null || type.isEmpty()) {
            errors.add("metadata: missing type");
        }
        if (displayName == null || displayName.isEmpty()) {
            errors.add("metadata: missing display name for " + type);
        }
        if (description == null || description.isEmpty()) {
            errors.add("metadata: missing description for " + type);
        }
        if (minSdk < 21 || minSdk > 35) {
            errors.add("metadata: minSdk out of range for " + type);
        }
        return errors;
    }

    public boolean isAddable() {
        return ComponentRegistry.contains(type)
                && previewSupported && xmlSupported;
    }

    public String getType() {
        return type;
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

    public List<String> getSupportedProperties() {
        return new ArrayList<>(supportedProperties);
    }

    public List<String> getAllowedParents() {
        return new ArrayList<>(allowedParents);
    }

    public boolean isChildSupport() {
        return childSupport;
    }

    public boolean isPreviewSupported() {
        return previewSupported;
    }

    public boolean isXmlSupported() {
        return xmlSupported;
    }

    public List<String> getRequiredDependencies() {
        return new ArrayList<>(requiredDependencies);
    }

    public int getMinSdk() {
        return minSdk;
    }

    public String getVersion() {
        return version;
    }
}
