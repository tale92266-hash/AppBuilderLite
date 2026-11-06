package com.nobi.appbuilder.events;

import com.nobi.appbuilder.generator.mapping.EventMappingRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Validates one event definition. Codegen triggers go through the
 * Phase 7 validator; extended triggers are validated for action and
 * target only, and flagged as not generatable (reported, never
 * silently generated).
 */
public final class EventValidator {

    private EventValidator() {
    }

    public static List<String> validate(EventDefinition definition,
                                        Set<String> existingScreenIds) {
        List<String> errors = new ArrayList<>();
        if (definition == null) {
            errors.add("event is missing");
            return errors;
        }
        String trigger = definition.getTrigger();
        if (trigger == null || trigger.isEmpty()) {
            errors.add("event has no trigger");
            return errors;
        }
        if (!EventTrigger.isKnown(trigger)) {
            errors.add("unknown trigger '" + trigger + "'");
            return errors;
        }
        String action = definition.getAction();
        if (action == null || action.isEmpty()) {
            errors.add("event has no action");
            return errors;
        }
        if (!EventMappingRegistry.isSupportedAction(action)
                && !EventActionRegistry.isSupported(action)) {
            errors.add("unsupported action '" + action + "'");
            return errors;
        }
        if (EventTrigger.isCodegenSupported(trigger)) {
            boolean targetExists = definition.getTargetScreenId() == null
                    || existingScreenIds == null
                    || existingScreenIds.contains(definition.getTargetScreenId());
            String problem = EventMappingRegistry.validateEvent(
                    definition.toEventModel(), targetExists);
            if (problem != null) {
                errors.add(problem);
            }
            return errors;
        }
        // Extended trigger: same action/target rules, no codegen.
        if (EventAction.needsMessage(action)
                && (definition.getMessage() == null
                || definition.getMessage().isEmpty())) {
            errors.add("action '" + action + "' needs a message");
        }
        if (EventAction.needsTargetScreen(action)) {
            if (definition.getTargetScreenId() == null
                    || definition.getTargetScreenId().isEmpty()) {
                errors.add("navigate needs a target screen");
            } else if (existingScreenIds != null
                    && !existingScreenIds.contains(
                    definition.getTargetScreenId())) {
                errors.add("navigate target screen '"
                        + definition.getTargetScreenId()
                        + "' does not exist");
            }
        }
        return errors;
    }

    /** True when the event will be emitted by the Phase 7 generator. */
    public static boolean isGeneratable(EventDefinition definition) {
        return definition != null && definition.isGeneratable()
                && EventMappingRegistry.isSupportedAction(definition.getAction());
    }
}
