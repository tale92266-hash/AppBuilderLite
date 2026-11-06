package com.nobi.appbuilder.events;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionRegistry;
import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;

import java.util.List;

/**
 * Bridges the workflow event model and the action registry: an event
 * action is supported when the Phase 7 mapping knows it AND the
 * action registry has a built-in for it.
 */
public final class EventActionRegistry {

    private EventActionRegistry() {
    }

    public static boolean isSupported(String action) {
        return EventMappingRegistry.isSupportedAction(action)
                && ActionRegistry.contains(normalize(action));
    }

    public static ActionDefinition definitionFor(String action) {
        return ActionRegistry.get(normalize(action));
    }

    public static List<ActionDefinition> getAll() {
        return ActionRegistry.getAll();
    }

    /** Normalizes aliases the mapping accepts to registry ids. */
    public static String normalize(String action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case EventMappingRegistry.ACTION_TOAST:
                return EventMappingRegistry.ACTION_SHOW_TOAST;
            case EventMappingRegistry.ACTION_FINISH:
                return EventMappingRegistry.ACTION_FINISH_ACTIVITY;
            case EventMappingRegistry.ACTION_CHANGE_VISIBILITY:
                return EventMappingRegistry.ACTION_SET_VISIBILITY;
            default:
                return action;
        }
    }
}
