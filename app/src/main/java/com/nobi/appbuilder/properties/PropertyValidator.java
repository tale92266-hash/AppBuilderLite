package com.nobi.appbuilder.properties;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.regex.Pattern;

/**
 * Validates a value against its definition. Returns a human-readable
 * error, or null when the value is fine. Invalid values are never
 * written to the model.
 */
public final class PropertyValidator {

    private static final Pattern COLOR_PATTERN =
            Pattern.compile("^#([0-9a-fA-F]{3}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$");
    private static final Pattern DIMENSION_PATTERN =
            Pattern.compile("^(match_parent|fill_parent|wrap_content|[0-9]+(\\.[0-9]+)?(dp|sp)?)$");
    private static final Pattern ID_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");

    private PropertyValidator() {
    }

    /**
     * Validates a value. root + currentId are used only for id uniqueness
     * and constraint targets; pass null when the screen is unknown.
     */
    public static String validate(PropertyDefinition definition, PropertyValue value,
                                  ComponentModel root, String currentId) {
        if (definition == null || value == null) {
            return "Missing property or value.";
        }
        String raw = value.asString();
        String key = definition.getKey();
        if ("id".equals(key)) {
            if (!ID_PATTERN.matcher(raw.trim()).matches()) {
                return "IDs must start with a letter and use only letters, digits and underscore.";
            }
            if (root != null && !raw.trim().equals(currentId)
                    && root.findById(raw.trim()) != null) {
                return "That ID is already used on this screen.";
            }
            return null;
        }
        if (key.startsWith("constraint")) {
            if ("parent".equals(raw) || "none".equals(raw) || raw.isEmpty()) {
                return null;
            }
            if (root != null && root.findById(raw.trim()) == null) {
                return "No component with that ID exists on this screen.";
            }
            return null;
        }
        switch (definition.getType()) {
            case INTEGER: {
                int intValue = value.asInt(Integer.MIN_VALUE);
                if (intValue == Integer.MIN_VALUE && !isIntegerText(raw)) {
                    return "Enter a whole number.";
                }
                return rangeError(definition, intValue);
            }
            case FLOAT: {
                float floatValue;
                try {
                    floatValue = Float.parseFloat(raw.trim());
                } catch (NumberFormatException e) {
                    return "Enter a number.";
                }
                return rangeError(definition, floatValue);
            }
            case COLOR:
                if (raw.isEmpty()) {
                    return null;
                }
                if (!COLOR_PATTERN.matcher(raw.trim()).matches()) {
                    return "Enter a color like #0B4499.";
                }
                return null;
            case DIMENSION:
                if (!DIMENSION_PATTERN.matcher(raw.trim().toLowerCase()).matches()) {
                    return "Use a number with dp or sp, or match_parent / wrap_content.";
                }
                return null;
            case ENUM:
            case GRAVITY:
            case ALIGNMENT:
                if (!definition.getAllowedValues().isEmpty()
                        && !definition.getAllowedValues().contains(raw)) {
                    return "Choose one of the listed values.";
                }
                return null;
            case STRING:
                if (raw.length() > 500) {
                    return "Text is too long (500 characters max).";
                }
                return null;
            case BOOLEAN:
            case RESOURCE:
            default:
                return null;
        }
    }

    private static boolean isIntegerText(String raw) {
        try {
            Integer.parseInt(raw.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static String rangeError(PropertyDefinition definition, float value) {
        if (value < definition.getMinValue()) {
            return "Minimum is " + formatNumber(definition.getMinValue()) + ".";
        }
        if (value > definition.getMaxValue()) {
            return "Maximum is " + formatNumber(definition.getMaxValue()) + ".";
        }
        return null;
    }

    private static String formatNumber(float value) {
        if (value == (long) value) {
            return String.valueOf((long) value);
        }
        return String.valueOf(value);
    }
}
