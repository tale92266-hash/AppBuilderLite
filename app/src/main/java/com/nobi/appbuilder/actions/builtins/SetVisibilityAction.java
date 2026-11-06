package com.nobi.appbuilder.actions.builtins;

import android.view.View;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.events.PreviewActionExecutor;

import java.util.Map;

/** Shows or hides a component ("visible" or "gone"). */
public final class SetVisibilityAction {

    private SetVisibilityAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.SET_VISIBILITY,
                "Set visibility", "Show or hide a component",
                ActionType.CATEGORY_UI)
                .param("targetComponentId", ActionParameter.TYPE_COMPONENT_ID,
                        false, null, "Component (default: source)")
                .param("visible", ActionParameter.TYPE_BOOLEAN, true,
                        "true", "true to show, false to hide")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String visibleRaw = params == null ? null : params.get("visible");
        boolean visible = "true".equalsIgnoreCase(visibleRaw);
        String targetId = params == null
                ? null : params.get("targetComponentId");
        if (context != null) {
            if (targetId == null || targetId.isEmpty()) {
                targetId = context.getComponentId();
            }
            context.log("Set visibility of " + targetId + " to "
                    + (visible ? "visible" : "gone"));
            context.putData("visibility:" + targetId,
                    visible ? "visible" : "gone");
            if (context.getPreviewContext() != null) {
                String wanted = visible ? "visible" : "gone";
                if (targetId.equals(context.getComponentId())
                        && context.getSourceView() != null) {
                    EventModel event = new EventModel(null,
                            context.getComponentId(), EventModel.EVENT_CLICK,
                            PreviewActionExecutor.ACTION_CHANGE_VISIBILITY,
                            null, wanted);
                    new PreviewActionExecutor().execute(event,
                            context.getSourceView(), context.getPreviewContext());
                } else {
                    View view = context.getPreviewContext()
                            .findView(targetId);
                    if (view != null) {
                        view.setVisibility(visible
                                ? View.VISIBLE : View.GONE);
                    }
                }
            }
        }
        return ActionResult.ok("Visibility set")
                .with("visible", String.valueOf(visible));
    }
}
