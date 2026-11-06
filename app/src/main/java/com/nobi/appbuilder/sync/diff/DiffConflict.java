package com.nobi.appbuilder.sync.diff;

import com.nobi.appbuilder.sync.conflict.ConflictType;

/** One property where the visual and code versions disagree. */
public class DiffConflict {

    private final String componentId;
    private final String propertyKey;
    private final String visualValue;
    private final String codeValue;
    private final ConflictType type;

    public DiffConflict(String componentId, String propertyKey,
                        String visualValue, String codeValue,
                        ConflictType type) {
        this.componentId = componentId;
        this.propertyKey = propertyKey;
        this.visualValue = visualValue;
        this.codeValue = codeValue;
        this.type = type;
    }

    public String getComponentId() {
        return componentId;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getVisualValue() {
        return visualValue;
    }

    public String getCodeValue() {
        return codeValue;
    }

    public ConflictType getType() {
        return type;
    }
}
