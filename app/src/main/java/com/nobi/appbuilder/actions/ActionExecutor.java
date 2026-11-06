package com.nobi.appbuilder.actions;

import com.nobi.appbuilder.actions.builtins.ChangeTextAction;
import com.nobi.appbuilder.actions.builtins.ConditionalAction;
import com.nobi.appbuilder.actions.builtins.DelayAction;
import com.nobi.appbuilder.actions.builtins.FinishScreenAction;
import com.nobi.appbuilder.actions.builtins.NavigateAction;
import com.nobi.appbuilder.actions.builtins.OpenUrlAction;
import com.nobi.appbuilder.actions.builtins.SetEnabledAction;
import com.nobi.appbuilder.actions.builtins.SetVariableAction;
import com.nobi.appbuilder.actions.builtins.SetVisibilityAction;
import com.nobi.appbuilder.actions.builtins.ShowDialogAction;
import com.nobi.appbuilder.actions.builtins.ShowToastAction;
import com.nobi.appbuilder.actions.builtins.ValidateFormAction;
import com.nobi.appbuilder.events.EventExecutionContext;

import java.util.List;
import java.util.Map;

/**
 * The restricted executor used by preview and by flow execution.
 * It only runs the twelve registered built-in actions, validates
 * parameters first, and never executes arbitrary generated Java.
 * Unknown actions fail with a reason instead of running.
 */
public class ActionExecutor {

    /** Executes one action. Returns failure for unknown actions or
     * invalid parameters — never throws for a bad action id. */
    public ActionResult execute(String actionId, Map<String, String> params,
                                EventExecutionContext context) {
        if (actionId == null || actionId.isEmpty()) {
            return ActionResult.failure("no action given");
        }
        ActionDefinition definition = ActionRegistry.get(actionId);
        if (definition == null) {
            return ActionResult.failure(
                    "unsupported action '" + actionId + "'");
        }
        List<String> problems = ActionValidator.validate(definition, params);
        if (!problems.isEmpty()) {
            return ActionResult.failure(problems.get(0));
        }
        switch (actionId) {
            case ActionType.NAVIGATE:
                return NavigateAction.run(params, context);
            case ActionType.SHOW_TOAST:
                return ShowToastAction.run(params, context);
            case ActionType.CHANGE_TEXT:
                return ChangeTextAction.run(params, context);
            case ActionType.SET_VISIBILITY:
                return SetVisibilityAction.run(params, context);
            case ActionType.SET_ENABLED:
                return SetEnabledAction.run(params, context);
            case ActionType.OPEN_URL:
                return OpenUrlAction.run(params, context);
            case ActionType.FINISH_SCREEN:
                return FinishScreenAction.run(params, context);
            case ActionType.VALIDATE_FORM:
                return ValidateFormAction.run(params, context);
            case ActionType.SET_VARIABLE:
                return SetVariableAction.run(params, context);
            case ActionType.SHOW_DIALOG:
                return ShowDialogAction.run(params, context);
            case ActionType.DELAY:
                return DelayAction.run(params, context);
            case ActionType.CONDITIONAL:
                return ConditionalAction.run(params, context);
            default:
                return ActionResult.failure(
                        "unsupported action '" + actionId + "'");
        }
    }

    public boolean isSupported(String actionId) {
        return ActionRegistry.contains(actionId);
    }
}
