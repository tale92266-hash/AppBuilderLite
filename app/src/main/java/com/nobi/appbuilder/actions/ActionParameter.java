package com.nobi.appbuilder.actions;

/**
 * One named parameter of an action. Types are fixed strings — the
 * validator checks supplied values against them.
 */
public class ActionParameter {

    public static final String TYPE_STRING = "string";
    public static final String TYPE_NUMBER = "number";
    public static final String TYPE_BOOLEAN = "boolean";
    public static final String TYPE_SCREEN_ID = "screen_id";
    public static final String TYPE_COMPONENT_ID = "component_id";

    private final String name;
    private final String type;
    private final boolean required;
    private final String defaultValue;
    private final String description;

    public ActionParameter(String name, String type, boolean required,
                           String defaultValue, String description) {
        this.name = name;
        this.type = type;
        this.required = required;
        this.defaultValue = defaultValue;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }
}
