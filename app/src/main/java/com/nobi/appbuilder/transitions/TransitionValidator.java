package com.nobi.appbuilder.transitions;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a transition choice: known id, duration inside the safe
 * range. Returns the list of problems (empty = valid).
 */
public final class TransitionValidator {

    private TransitionValidator() {
    }

    public static List<String> validate(String transitionId, int durationMs) {
        List<String> errors = new ArrayList<>();
        if (transitionId == null || transitionId.isEmpty()) {
            errors.add("transition is missing");
            return errors;
        }
        if (!TransitionRegistry.contains(transitionId)) {
            errors.add("unknown transition '" + transitionId + "'");
        }
        if (durationMs < TransitionDefinition.MIN_DURATION_MS
                || durationMs > TransitionDefinition.MAX_DURATION_MS) {
            errors.add("duration " + durationMs + "ms is outside "
                    + TransitionDefinition.MIN_DURATION_MS + "-"
                    + TransitionDefinition.MAX_DURATION_MS + "ms");
        }
        return errors;
    }

    public static List<String> validate(TransitionDefinition definition) {
        if (definition == null) {
            List<String> errors = new ArrayList<>();
            errors.add("transition is missing");
            return errors;
        }
        return validate(definition.getId(), definition.getDurationMs());
    }
}
