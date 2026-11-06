package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/** Sets one flow variable (typed value, validated by the executor). */
public final class SetVariableAction {

    private SetVariableAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.SET_VARIABLE, "Set variable",
                "Store a value in a flow variable", ActionType.CATEGORY_DATA)
                .param("name", ActionParameter.TYPE_STRING, true,
                        null, "Variable name")
                .param("value", ActionParameter.TYPE_STRING, true,
                        null, "Value to store")
                .param("type", ActionParameter.TYPE_STRING, false,
                        "string", "string, number or boolean")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String name = params == null ? null : params.get("name");
        String value = params == null ? null : params.get("value");
        if (name == null || name.isEmpty()) {
            return ActionResult.failure("setVariable needs a name");
        }
        if (value == null) {
            return ActionResult.failure("setVariable needs a value");
        }
        String type = params.get("type");
        if (type == null || type.isEmpty()) {
            type = "string";
        }
        if ("number".equals(type)) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return ActionResult.failure(
                        "variable '" + name + "' must be a number");
            }
        } else if ("boolean".equals(type)) {
            if (!"true".equalsIgnoreCase(value)
                    && !"false".equalsIgnoreCase(value)) {
                return ActionResult.failure(
                        "variable '" + name + "' must be true or false");
            }
        }
        if (context != null) {
            context.setVariable(name, value);
            context.log("Set variable " + name + " = " + value);
        }
        return ActionResult.ok("Variable set")
                .with("name", name).with("value", value);
    }
}
