package com.nobi.appbuilder.actions.builtins;

import android.view.View;
import android.widget.EditText;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/**
 * Validates one input field. Rules are fixed strings: "required"
 * (non-empty), "email" (contains @) and "min_length" (param
 * minLength). Returns failure with the error message when invalid.
 */
public final class ValidateFormAction {

    private ValidateFormAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.VALIDATE_FORM, "Validate input",
                "Check a field and report problems", ActionType.CATEGORY_UI)
                .param("targetComponentId", ActionParameter.TYPE_COMPONENT_ID,
                        false, null, "Field to validate (default: source)")
                .param("rule", ActionParameter.TYPE_STRING, false,
                        "required", "required, email or min_length")
                .param("minLength", ActionParameter.TYPE_NUMBER, false,
                        "1", "Minimum length for min_length")
                .param("message", ActionParameter.TYPE_STRING, false,
                        "Please enter a value", "Error message when invalid")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String rule = params == null ? "required" : params.get("rule");
        if (rule == null || rule.isEmpty()) {
            rule = "required";
        }
        String message = params == null ? null : params.get("message");
        if (message == null || message.isEmpty()) {
            message = "Please enter a value";
        }
        String targetId = params == null
                ? null : params.get("targetComponentId");
        String value = null;
        if (context != null) {
            if (targetId == null || targetId.isEmpty()) {
                targetId = context.getComponentId();
            }
            value = context.getData("text:" + targetId);
            if (value == null && context.getPreviewContext() != null) {
                View view = targetId.equals(context.getComponentId())
                        && context.getSourceView() != null
                        ? context.getSourceView()
                        : context.getPreviewContext().findView(targetId);
                if (view instanceof EditText) {
                    value = ((EditText) view).getText().toString();
                }
            }
            if (value == null) {
                value = "";
            }
            context.log("Validate " + targetId + " (" + rule + ")");
            if (context.getPreviewContext() != null
                    && targetId.equals(context.getComponentId())
                    && context.getSourceView() != null) {
                EventModel event = new EventModel(null,
                        context.getComponentId(), EventModel.EVENT_CLICK,
                        PreviewActionExecutor.ACTION_VALIDATE_INPUT, null,
                        message);
                new PreviewActionExecutor().execute(event,
                        context.getSourceView(), context.getPreviewContext());
            }
        }
        if (value == null) {
            value = "";
        }
        String problem = check(rule, value, params);
        if (problem != null) {
            return ActionResult.failure(message).with("valid", "false");
        }
        return ActionResult.ok("Valid").with("valid", "true");
    }

    /** Pure check, shared with the flow engine. Returns null when valid. */
    public static String check(String rule, String value,
                               Map<String, String> params) {
        if (value == null) {
            value = "";
        }
        switch (rule) {
            case "email":
                if (!value.contains("@") || !value.contains(".")) {
                    return "not a valid email";
                }
                return null;
            case "min_length": {
                int min = 1;
                if (params != null && params.get("minLength") != null) {
                    try {
                        min = Integer.parseInt(params.get("minLength"));
                    } catch (NumberFormatException ignored) {
                    }
                }
                if (value.trim().length() < min) {
                    return "too short";
                }
                return null;
            }
            case "required":
            default:
                if (value.trim().isEmpty()) {
                    return "required";
                }
                return null;
        }
    }
}
