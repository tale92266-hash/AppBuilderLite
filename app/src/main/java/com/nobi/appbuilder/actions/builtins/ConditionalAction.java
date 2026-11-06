package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/**
 * Evaluates one typed condition and reports true/false in the result.
 * Operators: equals, not_equals, contains, is_empty, is_not_empty,
 * greater_than, less_than, is_true, is_false. No expression
 * evaluation — operands are a variable reference ("var:name") or a
 * literal, and types are checked before comparing.
 */
public final class ConditionalAction {

    private ConditionalAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.CONDITIONAL, "Condition",
                "Branch on a typed comparison", ActionType.CATEGORY_CONTROL)
                .param("operator", ActionParameter.TYPE_STRING, true,
                        "equals", "equals, not_equals, contains, is_empty, "
                        + "is_not_empty, greater_than, less_than, is_true, "
                        + "is_false")
                .param("left", ActionParameter.TYPE_STRING, true,
                        null, "Left value or var:name")
                .param("right", ActionParameter.TYPE_STRING, false,
                        null, "Right value (not needed for empty/boolean)")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String operator = params == null ? null : params.get("operator");
        String leftRaw = params == null ? null : params.get("left");
        String right = params == null ? null : params.get("right");
        if (operator == null || operator.isEmpty()
                || leftRaw == null) {
            return ActionResult.failure("condition needs operator and left");
        }
        String left = resolve(leftRaw, context);
        Boolean result = evaluate(operator, left, right);
        if (result == null) {
            return ActionResult.failure(
                    "unknown operator '" + operator + "'");
        }
        if (context != null) {
            context.log("Condition " + operator + " (" + left
                    + (right != null ? ", " + right : "") + ") = " + result);
        }
        return ActionResult.ok("Condition evaluated")
                .with("result", String.valueOf(result));
    }

    /** Resolves "var:name" from the context, otherwise the literal. */
    public static String resolve(String raw, EventExecutionContext context) {
        if (raw != null && raw.startsWith("var:")
                && context != null) {
            String value = context.getVariable(raw.substring(4));
            return value != null ? value : "";
        }
        return raw == null ? "" : raw;
    }

    /** Pure typed comparison. Returns null for an unknown operator. */
    public static Boolean evaluate(String operator, String left,
                                   String right) {
        if (left == null) {
            left = "";
        }
        switch (operator) {
            case "equals":
                return left.equals(right == null ? "" : right);
            case "not_equals":
                return !left.equals(right == null ? "" : right);
            case "contains":
                return right != null && left.contains(right);
            case "is_empty":
                return left.trim().isEmpty();
            case "is_not_empty":
                return !left.trim().isEmpty();
            case "greater_than":
            case "less_than": {
                try {
                    double l = Double.parseDouble(left.trim());
                    double r = Double.parseDouble(
                            right == null ? "" : right.trim());
                    return "greater_than".equals(operator) ? l > r : l < r;
                } catch (NumberFormatException e) {
                    return false;
                }
            }
            case "is_true":
                return "true".equalsIgnoreCase(left.trim());
            case "is_false":
                return "false".equalsIgnoreCase(left.trim());
            default:
                return null;
        }
    }
}
