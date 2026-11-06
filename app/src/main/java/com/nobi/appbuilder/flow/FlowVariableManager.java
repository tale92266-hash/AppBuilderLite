package com.nobi.appbuilder.flow;

/**
 * Typed access to a flow run's variables. Values are stored as
 * strings; getters parse them and fall back to the supplied default
 * instead of throwing.
 */
public class FlowVariableManager {

    private final FlowExecutionContext context;

    public FlowVariableManager(FlowExecutionContext context) {
        this.context = context;
    }

    /** Validates the type, then stores. Returns an error or null. */
    public String set(String name, String value, String type) {
        if (name == null || name.isEmpty()) {
            return "variable name is missing";
        }
        if (value == null) {
            return "variable '" + name + "' has no value";
        }
        if ("number".equals(type)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return "variable '" + name + "' must be a number";
            }
        } else if ("boolean".equals(type)) {
            if (!"true".equalsIgnoreCase(value)
                    && !"false".equalsIgnoreCase(value)) {
                return "variable '" + name + "' must be true or false";
            }
        }
        context.setVariable(name, value);
        return null;
    }

    public String get(String name) {
        return context.getVariable(name);
    }

    public String getString(String name, String fallback) {
        String value = context.getVariable(name);
        return value != null ? value : fallback;
    }

    public double getNumber(String name, double fallback) {
        String value = context.getVariable(name);
        if (value == null) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public boolean getBoolean(String name, boolean fallback) {
        String value = context.getVariable(name);
        if (value == null) {
            return fallback;
        }
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }
        return fallback;
    }
}
