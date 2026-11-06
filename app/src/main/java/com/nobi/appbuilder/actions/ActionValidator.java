package com.nobi.appbuilder.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Validates supplied action parameters against the action's
 * definition: required values present, numbers parse, booleans are
 * true/false, screen ids exist when a screen set is given.
 */
public final class ActionValidator {

    private ActionValidator() {
    }

    public static List<String> validate(ActionDefinition definition,
                                        Map<String, String> params,
                                        Set<String> existingScreenIds) {
        List<String> errors = new ArrayList<>();
        if (definition == null) {
            errors.add("unknown action");
            return errors;
        }
        for (ActionParameter param : definition.getParameters()) {
            String value = params == null ? null : params.get(param.getName());
            if (value == null || value.isEmpty()) {
                value = param.getDefaultValue();
            }
            if ((value == null || value.isEmpty()) && param.isRequired()) {
                errors.add("action '" + definition.getId()
                        + "' needs parameter '" + param.getName() + "'");
                continue;
            }
            if (value == null || value.isEmpty()) {
                continue;
            }
            switch (param.getType()) {
                case ActionParameter.TYPE_NUMBER:
                    try {
                        Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        errors.add("parameter '" + param.getName()
                                + "' must be a number");
                    }
                    break;
                case ActionParameter.TYPE_BOOLEAN:
                    if (!"true".equalsIgnoreCase(value)
                            && !"false".equalsIgnoreCase(value)) {
                        errors.add("parameter '" + param.getName()
                                + "' must be true or false");
                    }
                    break;
                case ActionParameter.TYPE_SCREEN_ID:
                    if (existingScreenIds != null
                            && !existingScreenIds.contains(value)) {
                        errors.add("parameter '" + param.getName()
                                + "': screen '" + value + "' does not exist");
                    }
                    break;
                default:
                    break;
            }
        }
        return errors;
    }

    public static List<String> validate(ActionDefinition definition,
                                        Map<String, String> params) {
        return validate(definition, params, null);
    }
}
