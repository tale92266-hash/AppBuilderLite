package com.nobi.appbuilder.properties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * One property in the schema: what it is, where it lives, what values
 * it accepts, and which Android XML attribute it maps to for Phase 7.
 * A null xmlAttribute means the property is editor-only (x, y, locked).
 */
public class PropertyDefinition {

    private final String key;
    private final String displayName;
    private final PropertyGroup group;
    private final PropertyType type;
    private final String defaultValue;
    private final float minValue;
    private final float maxValue;
    private final List<String> allowedValues;
    private final boolean editable;
    private final boolean affectsLayout;
    private final boolean affectsAppearance;
    private final String xmlAttribute;
    private final Set<String> appliesToTypes;
    private final boolean appliesToAll;

    private PropertyDefinition(Builder builder) {
        this.key = builder.key;
        this.displayName = builder.displayName;
        this.group = builder.group;
        this.type = builder.type;
        this.defaultValue = builder.defaultValue;
        this.minValue = builder.minValue;
        this.maxValue = builder.maxValue;
        this.allowedValues = builder.allowedValues == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(builder.allowedValues));
        this.editable = builder.editable;
        this.affectsLayout = builder.affectsLayout;
        this.affectsAppearance = builder.affectsAppearance;
        this.xmlAttribute = builder.xmlAttribute;
        this.appliesToTypes = builder.appliesToTypes == null
                ? Collections.emptySet()
                : Collections.unmodifiableSet(new HashSet<>(builder.appliesToTypes));
        this.appliesToAll = builder.appliesToAll;
    }

    public String getKey() {
        return key;
    }

    public String getDisplayName() {
        return displayName;
    }

    public PropertyGroup getGroup() {
        return group;
    }

    public PropertyType getType() {
        return type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public List<String> getAllowedValues() {
        return allowedValues;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isAffectsLayout() {
        return affectsLayout;
    }

    public boolean isAffectsAppearance() {
        return affectsAppearance;
    }

    /** Android XML attribute for Phase 7, or null when editor-only. */
    public String getXmlAttribute() {
        return xmlAttribute;
    }

    public boolean appliesTo(String componentType) {
        return appliesToAll || appliesToTypes.contains(componentType);
    }

    public static class Builder {
        private final String key;
        private final String displayName;
        private final PropertyGroup group;
        private final PropertyType type;
        private String defaultValue = "";
        private float minValue = Float.MIN_VALUE;
        private float maxValue = Float.MAX_VALUE;
        private List<String> allowedValues;
        private boolean editable = true;
        private boolean affectsLayout;
        private boolean affectsAppearance;
        private String xmlAttribute;
        private Set<String> appliesToTypes;
        private boolean appliesToAll = true;

        public Builder(String key, String displayName, PropertyGroup group, PropertyType type) {
            this.key = key;
            this.displayName = displayName;
            this.group = group;
            this.type = type;
        }

        public Builder defaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder range(float min, float max) {
            this.minValue = min;
            this.maxValue = max;
            return this;
        }

        public Builder allowedValues(String... values) {
            this.allowedValues = Arrays.asList(values);
            return this;
        }

        public Builder allowedValues(List<String> values) {
            this.allowedValues = values;
            return this;
        }

        public Builder editable(boolean editable) {
            this.editable = editable;
            return this;
        }

        public Builder affectsLayout(boolean affectsLayout) {
            this.affectsLayout = affectsLayout;
            return this;
        }

        public Builder affectsAppearance(boolean affectsAppearance) {
            this.affectsAppearance = affectsAppearance;
            return this;
        }

        public Builder xmlAttribute(String xmlAttribute) {
            this.xmlAttribute = xmlAttribute;
            return this;
        }

        public Builder appliesTo(String... types) {
            this.appliesToTypes = new HashSet<>(Arrays.asList(types));
            this.appliesToAll = false;
            return this;
        }

        public PropertyDefinition build() {
            return new PropertyDefinition(this);
        }
    }
}
