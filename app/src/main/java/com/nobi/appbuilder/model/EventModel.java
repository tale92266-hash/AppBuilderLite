package com.nobi.appbuilder.model;

/**
 * One interaction attached to a component: e.g. a Button click that shows
 * a Toast or navigates to another screen. Phase 7 (Java generator) turns
 * these into real click listeners.
 */
public class EventModel {

    public static final String EVENT_CLICK = "click";
    public static final String EVENT_LONG_CLICK = "long_click";
    public static final String EVENT_TEXT_CHANGED = "text_changed";

    public static final String ACTION_NONE = "none";
    public static final String ACTION_TOAST = "toast";
    public static final String ACTION_NAVIGATE = "navigate";

    private String id;
    private String componentId;
    private String eventType;
    private String action;
    private String targetScreenId;
    private String message;

    public EventModel() {
    }

    public EventModel(String id, String componentId, String eventType,
                      String action, String targetScreenId, String message) {
        this.id = id;
        this.componentId = componentId;
        this.eventType = eventType;
        this.action = action;
        this.targetScreenId = targetScreenId;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getTargetScreenId() {
        return targetScreenId;
    }

    public void setTargetScreenId(String targetScreenId) {
        this.targetScreenId = targetScreenId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
