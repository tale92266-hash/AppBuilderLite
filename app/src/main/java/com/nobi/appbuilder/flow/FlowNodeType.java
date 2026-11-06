package com.nobi.appbuilder.flow;

/**
 * Workflow node types. START has one "next" port, END has none,
 * CONDITION has "true"/"false" ports, everything else has "next".
 */
public enum FlowNodeType {
    START,
    EVENT,
    ACTION,
    CONDITION,
    NAVIGATION,
    VARIABLE,
    END;

    public static FlowNodeType fromName(String name) {
        if (name == null) {
            return ACTION;
        }
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ACTION;
        }
    }

    public String displayName() {
        switch (this) {
            case START:
                return "Start";
            case EVENT:
                return "Event";
            case ACTION:
                return "Action";
            case CONDITION:
                return "Condition";
            case NAVIGATION:
                return "Navigation";
            case VARIABLE:
                return "Variable";
            case END:
                return "End";
            default:
                return name();
        }
    }
}
