package com.nobi.appbuilder.events;

import com.nobi.appbuilder.model.EventModel;

/**
 * The workflow view of an event. EventModel stays the canonical
 * store (it is what the JSON IR, the preview and the Phase 7
 * generator all read); this class wraps it with the extended trigger
 * set and convenience accessors, and converts back without loss.
 */
public class EventDefinition {

    private final EventModel model;

    public EventDefinition(EventModel model) {
        this.model = model == null ? new EventModel() : model;
    }

    public EventDefinition(String id, String componentId, String trigger,
                           String action, String targetScreenId,
                           String message) {
        this.model = new EventModel(id, componentId, trigger, action,
                targetScreenId, message);
    }

    public static EventDefinition fromEventModel(EventModel model) {
        return new EventDefinition(model);
    }

    public EventModel toEventModel() {
        return model;
    }

    public String getId() {
        return model.getId();
    }

    public void setId(String id) {
        model.setId(id);
    }

    public String getComponentId() {
        return model.getComponentId();
    }

    public void setComponentId(String componentId) {
        model.setComponentId(componentId);
    }

    public String getTrigger() {
        return model.getEventType();
    }

    public void setTrigger(String trigger) {
        model.setEventType(trigger);
    }

    public String getAction() {
        return model.getAction();
    }

    public void setAction(String action) {
        model.setAction(action);
    }

    public String getTargetScreenId() {
        return model.getTargetScreenId();
    }

    public void setTargetScreenId(String targetScreenId) {
        model.setTargetScreenId(targetScreenId);
    }

    public String getMessage() {
        return model.getMessage();
    }

    public void setMessage(String message) {
        model.setMessage(message);
    }

    /** True when the Phase 7 generator can emit this event. */
    public boolean isGeneratable() {
        return EventTrigger.isCodegenSupported(getTrigger());
    }

    public String summary() {
        String trigger = EventTrigger.displayName(getTrigger());
        String action = EventAction.displayName(getAction());
        if (getTargetScreenId() != null && !getTargetScreenId().isEmpty()) {
            return trigger + " → " + action + " (" + getTargetScreenId() + ")";
        }
        if (getMessage() != null && !getMessage().isEmpty()) {
            return trigger + " → " + action + " (" + getMessage() + ")";
        }
        return trigger + " → " + action;
    }
}
