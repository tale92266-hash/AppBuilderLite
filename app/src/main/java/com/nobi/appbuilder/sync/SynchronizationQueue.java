package com.nobi.appbuilder.sync;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

/**
 * Bounded queue of pending sync events. Events for the same screen
 * and kind collapse into the latest one, so a burst of keystrokes
 * produces one sync run, not fifty.
 */
public class SynchronizationQueue {

    public static final int MAX_EVENTS = 50;

    private final Deque<SynchronizationEvent> queue = new ArrayDeque<>();

    public synchronized void enqueue(SynchronizationEvent event) {
        Iterator<SynchronizationEvent> it = queue.iterator();
        while (it.hasNext()) {
            SynchronizationEvent existing = it.next();
            if (existing.getKind().equals(event.getKind())
                    && existing.getScreenId() != null
                    && existing.getScreenId().equals(event.getScreenId())) {
                it.remove();
            }
        }
        queue.addLast(event);
        while (queue.size() > MAX_EVENTS) {
            queue.removeFirst();
        }
    }

    public synchronized SynchronizationEvent poll() {
        return queue.pollFirst();
    }

    public synchronized int size() {
        return queue.size();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }

    public synchronized void clear() {
        queue.clear();
    }
}
