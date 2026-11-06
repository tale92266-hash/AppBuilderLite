package com.nobi.appbuilder.generator.mapping;

import com.nobi.appbuilder.model.EventModel;

/**
 * The predefined event actions code generation supports, with what
 * each one needs. Anything outside this set is rejected by
 * validation — never generated, never executed.
 */
public final class EventMappingRegistry {

    private EventMappingRegistry() {
    }

    public static final String ACTION_SHOW_TOAST = "showToast";
    public static final String ACTION_TOAST = "toast";
    public static final String ACTION_NAVIGATE = "navigate";
    public static final String ACTION_CHANGE_TEXT = "changeText";
    public static final String ACTION_VALIDATE_INPUT = "validateInput";
    public static final String ACTION_FINISH_ACTIVITY = "finishActivity";
    public static final String ACTION_FINISH = "finish";
    public static final String ACTION_SET_VISIBILITY = "setVisibility";
    public static final String ACTION_CHANGE_VISIBILITY = "changeVisibility";
    public static final String ACTION_OPEN_URL = "openUrl";
    public static final String ACTION_SET_ENABLED = "setEnabled";
    public static final String ACTION_NONE = "none";

    public static boolean isSupportedAction(String action) {
        if (action == null) {
            return false;
        }
        switch (action) {
            case ACTION_SHOW_TOAST:
            case ACTION_TOAST:
            case ACTION_NAVIGATE:
            case ACTION_CHANGE_TEXT:
            case ACTION_VALIDATE_INPUT:
            case ACTION_FINISH_ACTIVITY:
            case ACTION_FINISH:
            case ACTION_SET_VISIBILITY:
            case ACTION_CHANGE_VISIBILITY:
            case ACTION_OPEN_URL:
            case ACTION_SET_ENABLED:
            case ACTION_NONE:
                return true;
            default:
                return false;
        }
    }

    public static boolean isSupportedTrigger(String trigger) {
        return EventModel.EVENT_CLICK.equals(trigger)
                || EventModel.EVENT_LONG_CLICK.equals(trigger)
                || EventModel.EVENT_TEXT_CHANGED.equals(trigger);
    }

    /**
     * Human-readable validation of one event. Returns null when the
     * event is generatable, otherwise the reason it is not.
     */
    public static String validateEvent(EventModel event,
                                       boolean targetScreenExists) {
        if (event == null) {
            return "null event";
        }
        if (!isSupportedTrigger(event.getEventType())) {
            return "unsupported trigger '" + event.getEventType() + "'";
        }
        if (!isSupportedAction(event.getAction())) {
            return "unsupported action '" + event.getAction() + "'";
        }
        String action = event.getAction();
        if (ACTION_SHOW_TOAST.equals(action) || ACTION_TOAST.equals(action)
                || ACTION_CHANGE_TEXT.equals(action)) {
            if (event.getMessage() == null || event.getMessage().isEmpty()) {
                return "action '" + action + "' needs a message";
            }
        }
        if (ACTION_NAVIGATE.equals(action)) {
            if (event.getTargetScreenId() == null
                    || event.getTargetScreenId().isEmpty()) {
                return "navigate needs a target screen";
            }
            if (!targetScreenExists) {
                return "navigate target screen '"
                        + event.getTargetScreenId() + "' does not exist";
            }
        }
        if (ACTION_OPEN_URL.equals(action)) {
            String url = event.getMessage();
            if (url == null || !(url.startsWith("http://")
                    || url.startsWith("https://"))) {
                return "openUrl needs an http(s) URL";
            }
        }
        if (ACTION_SET_VISIBILITY.equals(action)
                || ACTION_CHANGE_VISIBILITY.equals(action)) {
            String wanted = event.getMessage();
            if (wanted != null && !"visible".equalsIgnoreCase(wanted)
                    && !"invisible".equalsIgnoreCase(wanted)
                    && !"gone".equalsIgnoreCase(wanted)) {
                return "setVisibility needs visible, invisible or gone";
            }
        }
        if (ACTION_SET_ENABLED.equals(action)) {
            String wanted = event.getMessage();
            if (wanted != null && !"true".equalsIgnoreCase(wanted)
                    && !"false".equalsIgnoreCase(wanted)) {
                return "setEnabled needs true or false";
            }
        }
        return null;
    }
}
