package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/** Finishes (closes) the current screen. */
public final class FinishScreenAction {

    private FinishScreenAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.FINISH_SCREEN, "Finish screen",
                "Close the current screen", ActionType.CATEGORY_NAVIGATION)
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        if (context != null) {
            context.log("Finish screen");
            context.putData("finished", "true");
            if (context.getPreviewContext() != null
                    && context.getSourceView() != null) {
                EventModel event = new EventModel(null,
                        context.getComponentId(), EventModel.EVENT_CLICK,
                        PreviewActionExecutor.ACTION_FINISH, null, null);
                new PreviewActionExecutor().execute(event,
                        context.getSourceView(), context.getPreviewContext());
            }
        }
        return ActionResult.ok("Screen finished");
    }
}
