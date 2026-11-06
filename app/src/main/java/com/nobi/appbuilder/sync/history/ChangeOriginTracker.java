package com.nobi.appbuilder.sync.history;

import com.nobi.appbuilder.sync.ChangeOrigin;

import java.util.HashMap;
import java.util.Map;

/**
 * Remembers which origin last touched each component. The manager
 * consults this before re-applying a change, so a change the sync
 * engine itself just made is never treated as a fresh manual edit.
 */
public class ChangeOriginTracker {

    private static class Entry {
        final ChangeOrigin origin;
        final long timestamp;

        Entry(ChangeOrigin origin, long timestamp) {
            this.origin = origin;
            this.timestamp = timestamp;
        }
    }

    private final Map<String, Entry> entries = new HashMap<>();

    public void mark(String componentId, ChangeOrigin origin) {
        entries.put(componentId,
                new Entry(origin, System.currentTimeMillis()));
    }

    public ChangeOrigin lastOrigin(String componentId) {
        Entry entry = entries.get(componentId);
        return entry == null ? null : entry.origin;
    }

    public boolean wasChangedBy(String componentId, ChangeOrigin origin) {
        return origin == lastOrigin(componentId);
    }

    public void clear(String componentId) {
        entries.remove(componentId);
    }

    public void clearAll() {
        entries.clear();
    }
}
