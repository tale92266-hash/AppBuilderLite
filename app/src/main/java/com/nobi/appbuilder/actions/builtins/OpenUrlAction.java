package com.nobi.appbuilder.actions.builtins;

import android.content.Intent;
import android.net.Uri;

import com.nobi.appbuilder.actions.ActionDefinition;
import com.nobi.appbuilder.actions.ActionParameter;
import com.nobi.appbuilder.actions.ActionResult;
import com.nobi.appbuilder.actions.ActionType;
import com.nobi.appbuilder.events.EventExecutionContext;
import com.nobi.appbuilder.model.EventModel;

import java.util.Map;

/**
 * Opens a URL in the browser. Only http/https URLs are opened; anything
 * else is recorded and refused. Without an Activity (flow run) the
 * open is recorded but not performed.
 */
public final class OpenUrlAction {

    private OpenUrlAction() {
    }

    public static ActionDefinition definition() {
        return new ActionDefinition(ActionType.OPEN_URL, "Open URL",
                "Open a web link in the browser", ActionType.CATEGORY_NAVIGATION)
                .param("url", ActionParameter.TYPE_STRING, true,
                        null, "http(s) URL to open")
                .trigger(EventModel.EVENT_CLICK);
    }

    public static ActionResult run(Map<String, String> params,
                                   EventExecutionContext context) {
        String url = params == null ? null : params.get("url");
        if (url == null || url.isEmpty()) {
            return ActionResult.failure("openUrl needs a URL");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return ActionResult.failure(
                    "openUrl only opens http/https URLs");
        }
        if (context != null) {
            context.log("Open URL " + url);
            if (context.getActivity() != null) {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(url));
                    context.getActivity().startActivity(intent);
                } catch (Exception e) {
                    return ActionResult.failure(
                            "could not open URL: " + e.getMessage());
                }
            }
        }
        return ActionResult.ok("URL opened").with("url", url);
    }
}
