package com.nobi.appbuilder.build;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Per-build cancellation tokens. Cancelling never deletes files;
 * the running engine stops at the next safe point. */
public class BuildCancellationManager {

    private final Map<String, Boolean> cancelled = new ConcurrentHashMap<>();

    public void register(String buildId) {
        cancelled.put(buildId, false);
    }

    public void cancel(String buildId) {
        cancelled.put(buildId, true);
    }

    public boolean isCancelled(String buildId) {
        Boolean value = cancelled.get(buildId);
        return value != null && value;
    }

    public void unregister(String buildId) {
        cancelled.remove(buildId);
    }
}
