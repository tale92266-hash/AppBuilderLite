package com.nobi.appbuilder.sync;

/** One atomic change inside a ChangeSet. */
public class ChangeOperation {

    public enum Type {
        ADD_COMPONENT,
        REMOVE_COMPONENT,
        UPDATE_PROPERTY,
        MOVE_COMPONENT,
        REORDER_CHILDREN,
        RENAME_COMPONENT
    }

    private final Type type;
    private final String componentId;
    private final String propertyKey;
    private final String oldValue;
    private final String newValue;

    public ChangeOperation(Type type, String componentId, String propertyKey,
                           String oldValue, String newValue) {
        this.type = type;
        this.componentId = componentId;
        this.propertyKey = propertyKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static ChangeOperation updateProperty(String componentId,
                                                 String propertyKey,
                                                 String oldValue,
                                                 String newValue) {
        return new ChangeOperation(Type.UPDATE_PROPERTY, componentId,
                propertyKey, oldValue, newValue);
    }

    public static ChangeOperation add(String componentId) {
        return new ChangeOperation(Type.ADD_COMPONENT, componentId,
                null, null, null);
    }

    public static ChangeOperation remove(String componentId) {
        return new ChangeOperation(Type.REMOVE_COMPONENT, componentId,
                null, null, null);
    }

    public Type getType() {
        return type;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getOldValue() {
        return oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    @Override
    public String toString() {
        if (type == Type.UPDATE_PROPERTY) {
            return componentId + "." + propertyKey + ": "
                    + oldValue + " -> " + newValue;
        }
        return type + " " + componentId;
    }
}
