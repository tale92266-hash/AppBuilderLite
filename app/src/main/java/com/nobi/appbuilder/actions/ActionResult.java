package com.nobi.appbuilder.actions;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The outcome of running one action. Failure always carries a reason;
 * success may carry data (e.g. a condition result or a new value).
 */
public class ActionResult {

    private final boolean success;
    private final String message;
    private final Map<String, String> data = new LinkedHashMap<>();

    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResult ok(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult ok() {
        return new ActionResult(true, null);
    }

    public static ActionResult failure(String message) {
        return new ActionResult(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getData() {
        return data;
    }

    public ActionResult with(String key, String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
        return this;
    }
}
