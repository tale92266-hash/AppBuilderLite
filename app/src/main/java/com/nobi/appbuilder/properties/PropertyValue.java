package com.nobi.appbuilder.properties;

/**
 * A typed property value. Editors produce these; PropertyBinding writes
 * them into a ComponentModel. The string form is the canonical stored
 * form (JSON properties are strings); typed accessors parse it.
 */
public class PropertyValue {

    private final PropertyType type;
    private final String stringValue;

    private PropertyValue(PropertyType type, String stringValue) {
        this.type = type;
        this.stringValue = stringValue == null ? "" : stringValue;
    }

    public static PropertyValue ofString(String value) {
        return new PropertyValue(PropertyType.STRING, value);
    }

    public static PropertyValue ofInt(int value) {
        return new PropertyValue(PropertyType.INTEGER, String.valueOf(value));
    }

    public static PropertyValue ofFloat(float value) {
        return new PropertyValue(PropertyType.FLOAT, String.valueOf(value));
    }

    public static PropertyValue ofBoolean(boolean value) {
        return new PropertyValue(PropertyType.BOOLEAN, String.valueOf(value));
    }

    public static PropertyValue ofColor(String hex) {
        return new PropertyValue(PropertyType.COLOR, hex);
    }

    public static PropertyValue ofDimension(String value) {
        return new PropertyValue(PropertyType.DIMENSION, value);
    }

    public static PropertyValue ofEnum(String value) {
        return new PropertyValue(PropertyType.ENUM, value);
    }

    public static PropertyValue of(PropertyType type, String value) {
        return new PropertyValue(type, value);
    }

    public PropertyType getType() {
        return type;
    }

    public String asString() {
        return stringValue;
    }

    public int asInt(int fallback) {
        try {
            return Integer.parseInt(stringValue.trim());
        } catch (NumberFormatException e) {
            try {
                return Math.round(Float.parseFloat(stringValue.trim()));
            } catch (NumberFormatException ignored) {
                return fallback;
            }
        }
    }

    public float asFloat(float fallback) {
        try {
            return Float.parseFloat(stringValue.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean asBoolean(boolean fallback) {
        if ("true".equalsIgnoreCase(stringValue.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(stringValue.trim())) {
            return false;
        }
        return fallback;
    }

    /** Dimension in dp, or -1 for match_parent, -2 for wrap_content. */
    public int asDimensionDp(int fallback) {
        String value = stringValue.trim().toLowerCase();
        if ("match_parent".equals(value) || "fill_parent".equals(value)) {
            return -1;
        }
        if ("wrap_content".equals(value)) {
            return -2;
        }
        if (value.endsWith("dp") || value.endsWith("sp")) {
            value = value.substring(0, value.length() - 2);
        }
        try {
            return Math.round(Float.parseFloat(value));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof PropertyValue)) {
            return false;
        }
        PropertyValue that = (PropertyValue) other;
        return type == that.type && stringValue.equals(that.stringValue);
    }

    @Override
    public int hashCode() {
        return 31 * type.hashCode() + stringValue.hashCode();
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
