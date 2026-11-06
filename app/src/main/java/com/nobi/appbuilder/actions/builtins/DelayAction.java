package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/**
 * Records a wait of N milliseconds. The engine never sleeps the UI
 * thread — the delay is logged and the flow continues; a real timed
 * wait belongs in generated code, not in preview execution.
 */
public final class DelayAction {

    public static final int MAX_DELAY_MS = 10000;

    private DelayAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.DELAY, "Delay",
                "Wait before the next step (recorded, never blocks)",
                ActionType.CATEGORY_CONTROL)
                .param("durationMs", ActionParameter.TYPE_NUMBER, true,
                        "500", "Milliseconds to wait (max 10000)")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        int duration = 500;
        if (params != null && params.get("durationMs") != null) {
            try {
                duration = Integer.parseInt(params.get("durationMs"));
            } catch (NumberFormatException e) {
                return ActionResult.failure("delay needs a number");
            }
        }
        if (duration < 0 || duration > MAX_DELAY_MS) {
            return ActionResult.failure(
                    "delay must be 0-" + MAX_DELAY_MS + "ms");
        }
        if (context != null) {
            context.log("Delay " + duration + "ms (recorded, not blocking)");
        }
        return ActionResult.ok("Delay recorded")
                .with("durationMs", String.valueOf(duration));
    }
}
