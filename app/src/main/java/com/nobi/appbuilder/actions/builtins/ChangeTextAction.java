package com.nobi.appbuilder.actions.builtins;

import android.view.View;
import android.widget.TextView;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/**
 * Sets the text of a component. In preview it delegates to the Phase 6
 * executor when the target is the source view, otherwise it writes to
 * the target view directly through the PreviewContext registry.
 */
public final class ChangeTextAction {

    private ChangeTextAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.CHANGE_TEXT, "Change text",
                "Set a component's text", ActionType.CATEGORY_UI)
                .param("targetComponentId", ActionParameter.TYPE_COMPONENT_ID,
                        false, null, "Component to change (default: source)")
                .param("text", ActionParameter.TYPE_STRING, true,
                        null, "New text")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_TEXT_CHANGED);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String text = params == null ? null : params.get("text");
        if (text == null) {
            return ActionResult.failure("changeText needs text");
        }
        String targetId = params == null
                ? null : params.get("targetComponentId");
        if (context != null) {
            if (targetId == null || targetId.isEmpty()) {
                targetId = context.getComponentId();
            }
            context.log("Change text of " + targetId + " to '" + text + "'");
            context.putData("text:" + targetId, text);
            if (context.getPreviewContext() != null) {
                if (targetId.equals(context.getComponentId())
                        && context.getSourceView() != null) {
                    EventModel event = new EventModel(null,
                            context.getComponentId(), EventModel.EVENT_CLICK,
                            PreviewActionExecutor.ACTION_CHANGE_TEXT, null, text);
                    new PreviewActionExecutor().execute(event,
                            context.getSourceView(), context.getPreviewContext());
                } else {
                    View view = context.getPreviewContext()
                            .findView(targetId);
                    if (view instanceof TextView) {
                        ((TextView) view).setText(text);
                    }
                }
            }
        }
        return ActionResult.ok("Text changed").with("text", text);
    }
}
