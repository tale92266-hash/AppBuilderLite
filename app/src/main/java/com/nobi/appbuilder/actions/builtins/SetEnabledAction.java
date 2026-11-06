package com.nobi.appbuilder.actions.builtins;

import android.view.View;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/**
 * Enables or disables a component. The Phase 6 preview executor has
 * no enabled action, so this writes to the view directly when a
 * preview is running, and records the change otherwise.
 */
public final class SetEnabledAction {

    private SetEnabledAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.SET_ENABLED, "Set enabled",
                "Enable or disable a component", ActionType.CATEGORY_UI)
                .param("targetComponentId", ActionParameter.TYPE_COMPONENT_ID,
                        false, null, "Component (default: source)")
                .param("enabled", ActionParameter.TYPE_BOOLEAN, true,
                        "true", "true to enable, false to disable")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String enabledRaw = params == null ? null : params.get("enabled");
        boolean enabled = "true".equalsIgnoreCase(enabledRaw);
        String targetId = params == null
                ? null : params.get("targetComponentId");
        if (context != null) {
            if (targetId == null || targetId.isEmpty()) {
                targetId = context.getComponentId();
            }
            context.log("Set enabled of " + targetId + " to " + enabled);
            context.putData("enabled:" + targetId, String.valueOf(enabled));
            if (context.getPreviewContext() != null) {
                if (targetId.equals(context.getComponentId())
                        && context.getSourceView() != null) {
                    context.getSourceView().setEnabled(enabled);
                } else {
                    View view = context.getPreviewContext()
                            .findView(targetId);
                    if (view != null) {
                        view.setEnabled(enabled);
                    }
                }
            }
        }
        return ActionResult.ok("Enabled set")
                .with("enabled", String.valueOf(enabled));
    }
}
