package com.nobi.appbuilder.dataflow;

/**
 * One typed argument a screen accepts. Keys are stable identifiers
 * (lowercase, underscores); values travel through Intent extras as
 * strings, never as large objects or images.
 */
public class ScreenArgument {

    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_BOOLEAN = "boolean";

    private String key;
    private String type = TYPE_STRING;
    private boolean required;
    private String defaultValue;
    private String description;

    public ScreenArgument() {
    }

    public ScreenArgument(String key, String type, boolean required,
                          String defaultValue) {
        this.key = key;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /** Checks one supplied value against this argument's type. */
    public String validateValue(String value) {
        if (value == null || value.isEmpty()) {
            if (required) {
                return "argument '" + key + "' is required";
            }
            return null;
        }
        if (TYPE_NUMBER.equals(type)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return "argument '" + key + "' must be a number";
            }
        } else if (TYPE_BOOLEAN.equals(type)) {
            if (!"true".equalsIgnoreCase(value)
                    && !"false".equalsIgnoreCase(value)) {
                return "argument '" + key + "' must be true or false";
            }
        }
        return null;
    }
}
