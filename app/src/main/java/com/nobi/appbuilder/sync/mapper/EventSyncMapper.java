package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;
import com.nobi.appbuilder.model.EventModel;

/**
 * Maps the single canonical EventModel to and from generated Java.
 * Only recognized generated listener patterns are understood —
 * arbitrary Java is never parsed as events.
 */
public class EventSyncMapper {

    /** The Android listener method for an event trigger. */
    public static String listenerMethodFor(String eventType) {
        if (EventModel.EVENT_CLICK.equals(eventType)) {
            return "setOnClickListener";
        }
        if (EventModel.EVENT_LONG_CLICK.equals(eventType)) {
            return "setOnLongClickListener";
        }
        if (EventModel.EVENT_TEXT_CHANGED.equals(eventType)) {
            return "addTextChangedListener";
        }
        return null;
    }

    /** Recognizes a generated listener call back to an event type. */
    public static String eventTypeForListenerMethod(String methodName) {
        if ("setOnClickListener".equals(methodName)) {
            return EventModel.EVENT_CLICK;
        }
        if ("setOnLongClickListener".equals(methodName)) {
            return EventModel.EVENT_LONG_CLICK;
        }
        if ("addTextChangedListener".equals(methodName)) {
            return EventModel.EVENT_TEXT_CHANGED;
        }
        return null;
    }

    /** True when the action is one the generator supports. */
    public static boolean isSupportedAction(String action) {
        return EventModel.ACTION_NONE.equals(action)
                || EventModel.ACTION_TOAST.equals(action)
                || EventModel.ACTION_NAVIGATE.equals(action)
                || EventMappingRegistry.ACTION_SHOW_TOAST.equals(action)
                || EventMappingRegistry.ACTION_CHANGE_TEXT.equals(action)
                || EventMappingRegistry.ACTION_VALIDATE_INPUT.equals(action)
                || EventMappingRegistry.ACTION_FINISH_ACTIVITY.equals(action)
                || EventMappingRegistry.ACTION_FINISH.equals(action)
                || EventMappingRegistry.ACTION_SET_VISIBILITY.equals(action)
                || EventMappingRegistry.ACTION_CHANGE_VISIBILITY
                        .equals(action)
                || EventMappingRegistry.ACTION_OPEN_URL.equals(action)
                || EventMappingRegistry.ACTION_SET_ENABLED.equals(action);
    }
}
