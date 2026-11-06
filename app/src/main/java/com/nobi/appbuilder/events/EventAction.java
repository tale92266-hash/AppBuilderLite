package com.nobi.appbuilder.events;

import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;

/**
 * Action ids, re-exported from the Phase 7 EventMappingRegistry so
 * the workflow engine, the preview and the generator never drift
 * apart. Display names and parameter needs live here.
 */
public final class EventAction {

    public static final String NONE = EventMappingRegistry.ACTION_NONE;
    public static final String SHOW_TOAST = EventMappingRegistry.ACTION_SHOW_TOAST;
    public static final String TOAST = EventMappingRegistry.ACTION_TOAST;
    public static final String NAVIGATE = EventMappingRegistry.ACTION_NAVIGATE;
    public static final String CHANGE_TEXT = EventMappingRegistry.ACTION_CHANGE_TEXT;
    public static final String VALIDATE_INPUT = EventMappingRegistry.ACTION_VALIDATE_INPUT;
    public static final String FINISH_ACTIVITY = EventMappingRegistry.ACTION_FINISH_ACTIVITY;
    public static final String FINISH = EventMappingRegistry.ACTION_FINISH;
    public static final String SET_VISIBILITY = EventMappingRegistry.ACTION_SET_VISIBILITY;
    public static final String CHANGE_VISIBILITY = EventMappingRegistry.ACTION_CHANGE_VISIBILITY;
    public static final String OPEN_URL = EventMappingRegistry.ACTION_OPEN_URL;
    public static final String SET_ENABLED = EventMappingRegistry.ACTION_SET_ENABLED;

    private EventAction() {
    }

    public static String displayName(String action) {
        if (action == null) {
            return "None";
        }
        switch (action) {
            case SHOW_TOAST:
            case TOAST:
                return "Show toast";
            case NAVIGATE:
                return "Navigate";
            case CHANGE_TEXT:
                return "Change text";
            case VALIDATE_INPUT:
                return "Validate input";
            case FINISH_ACTIVITY:
            case FINISH:
                return "Finish screen";
            case SET_VISIBILITY:
            case CHANGE_VISIBILITY:
                return "Set visibility";
            case OPEN_URL:
                return "Open URL";
            case SET_ENABLED:
                return "Set enabled";
            case NONE:
                return "None";
            default:
                return action;
        }
    }

    public static boolean needsMessage(String action) {
        return SHOW_TOAST.equals(action) || TOAST.equals(action)
                || CHANGE_TEXT.equals(action) || OPEN_URL.equals(action);
    }

    public static boolean needsTargetScreen(String action) {
        return NAVIGATE.equals(action);
    }
}
