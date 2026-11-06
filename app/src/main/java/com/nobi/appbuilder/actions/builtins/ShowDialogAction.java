package com.nobi.appbuilder.actions.builtins;

import android.app.AlertDialog;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/**
 * Shows a simple message dialog. Without an Activity (flow run) the
 * dialog is recorded but not shown.
 */
public final class ShowDialogAction {

    private ShowDialogAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.SHOW_DIALOG, "Show dialog",
                "Show a message dialog", ActionType.CATEGORY_FEEDBACK)
                .param("title", ActionParameter.TYPE_STRING, false,
                        "Message", "Dialog title")
                .param("message", ActionParameter.TYPE_STRING, true,
                        null, "Dialog message")
                .trigger(EventModel.EVENT_CLICK)
                .trigger(EventModel.EVENT_LONG_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String message = params == null ? null : params.get("message");
        if (message == null || message.isEmpty()) {
            return ActionResult.failure("showDialog needs a message");
        }
        String title = params == null ? null : params.get("title");
        if (title == null || title.isEmpty()) {
            title = "Message";
        }
        if (context != null) {
            context.log("Dialog: " + title + " — " + message);
            if (context.getActivity() != null) {
                new AlertDialog.Builder(context.getActivity())
                        .setTitle(title)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, null)
                        .show();
            }
        }
        return ActionResult.ok("Dialog shown").with("message", message);
    }
}
