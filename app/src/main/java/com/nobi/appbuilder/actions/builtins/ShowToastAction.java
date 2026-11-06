package com.nobi.appbuilder.actions.builtins;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/** Shows a short Toast message. */
public final class ShowToastAction {

    private ShowToastAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.SHOW_TOAST, "Show toast",
                "Show a short message", ActionType.CATEGORY_FEEDBACK)
                .param("message", ActionParameter.TYPE_STRING, true,
                        null, "Message to show")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String message = params == null ? null : params.get("message");
        if (message == null || message.isEmpty()) {
            return ActionResult.failure("toast needs a message");
        }
        if (context != null) {
            context.log("Toast: " + message);
            if (context.getPreviewContext() != null
                    && context.getSourceView() != null) {
                EventModel event = new EventModel(null,
                        context.getComponentId(), EventModel.EVENT_CLICK,
                        PreviewActionExecutor.ACTION_SHOW_TOAST, null, message);
                new PreviewActionExecutor().execute(event,
                        context.getSourceView(), context.getPreviewContext());
            }
        }
        return ActionResult.ok("Toast shown").with("message", message);
    }
}
