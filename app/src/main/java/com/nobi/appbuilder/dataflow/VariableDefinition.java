package com.nobi.appbuilder.dataflow;

/**
 * One named variable with a fixed type and an optional default.
 * Scopes: "screen" (one screen) or "global" (whole project).
 */
public class VariableDefinition {

    public static final String SCOPE_SCREEN = "screen";
    public static final String SCOPE_GLOBAL = "global";

    private String name;
    private String type = ScreenArgument.TYPE_STRING;
    private String defaultValue;
    private String scope = SCOPE_SCREEN;

    public VariableDefinition() {
    }

    public VariableDefinition(String name, String type, String defaultValue,
                              String scope) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
        this.scope = scope;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    /** Validates one value against this variable's type. */
    public String validateValue(String value) {
        if (value == null) {
            return null;
        }
        if (ScreenArgument.TYPE_NUMBER.equals(type)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return "variable '" + name + "' must be a number";
            }
        } else if (ScreenArgument.TYPE_BOOLEAN.equals(type)) {
            if (!"true".equalsIgnoreCase(value)
                    && !"false".equalsIgnoreCase(value)) {
                return "variable '" + name + "' must be true or false";
            }
        }
        return null;
    }
}
