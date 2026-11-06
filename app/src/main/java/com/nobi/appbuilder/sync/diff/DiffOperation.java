package com.nobi.appbuilder.sync.diff;

/** One difference between two representations of a component. */
public class DiffOperation {

    public enum Type {
        ADD,
        REMOVE,
        UPDATE,
        MOVE
    }

    private final Type type;
    private final String componentId;
    private final String propertyKey;
    private final String oldValue;
    private final String newValue;

    public DiffOperation(Type type, String componentId, String propertyKey,
                         String oldValue, String newValue) {
        this.type = type;
        this.componentId = componentId;
        this.propertyKey = propertyKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public static DiffOperation update(String componentId, String propertyKey,
                                       String oldValue, String newValue) {
        return new DiffOperation(Type.UPDATE, componentId, propertyKey,
                oldValue, newValue);
    }

    public static DiffOperation add(String componentId) {
        return new DiffOperation(Type.ADD, componentId, null, null, null);
    }

    public static DiffOperation remove(String componentId) {
        return new DiffOperation(Type.REMOVE, componentId, null, null, null);
    }

    public static DiffOperation move(String componentId) {
        return new DiffOperation(Type.MOVE, componentId, null, null, null);
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
        if (type == Type.UPDATE) {
            return componentId + "." + propertyKey + ": "
                    + oldValue + " -> " + newValue;
        }
        return type + " " + componentId;
    }
}
