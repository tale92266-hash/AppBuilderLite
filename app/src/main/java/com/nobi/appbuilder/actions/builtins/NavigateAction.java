package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/**
 * Navigates to another screen. In preview it delegates to the Phase 6
 * PreviewActionExecutor (which switches the preview screen); in a
 * flow run it records the navigation event for the debug log.
 */
public final class NavigateAction {

    private NavigateAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.NAVIGATE, "Navigate",
                "Open another screen", ActionType.CATEGORY_NAVIGATION)
                .param("targetScreenId", ActionParameter.TYPE_SCREEN_ID, true,
                        null, "Screen to open")
                .param("transition", ActionParameter.TYPE_STRING, false,
                        "fade", "Transition id (none, fade, slide_left, ...)")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String target = params == null ? null : params.get("targetScreenId");
        if (target == null || target.isEmpty()) {
            return ActionResult.failure("navigate needs a target screen");
        }
        if (context != null) {
            context.recordNavigation(target);
            context.log("Navigate to " + target);
            if (context.getPreviewContext() != null
                    && context.getSourceView() != null) {
                EventModel event = new EventModel(null,
                        context.getComponentId(), EventModel.EVENT_CLICK,
                        PreviewActionExecutor.ACTION_NAVIGATE, target, null);
                new PreviewActionExecutor().execute(event,
                        context.getSourceView(), context.getPreviewContext());
            }
        }
        return ActionResult.ok("Navigated to " + target)
                .with("targetScreenId", target);
    }
}
