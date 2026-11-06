package com.nobi.appbuilder.build;

import java.util.ArrayDeque;
import java.util.Queue;

/** One build runs at a time; the rest wait in a bounded queue. */
public class BuildQueueManager {

    public static final int MAX_QUEUE = 5;

    private final Queue<BuildRequest> queue = new ArrayDeque<>();
    private BuildRequest active;

    public synchronized boolean enqueue(BuildRequest request) {
        if (queue.size() >= MAX_QUEUE) {
            return false;
        }
        queue.add(request);
        return true;
    }

    public synchronized BuildRequest startNext() {
        if (active != null) {
            return null;
        }
        active = queue.poll();
        return active;
    }

    public synchronized void finishActive() {
        active = null;
    }

    public synchronized BuildRequest getActive() {
        return active;
    }

    public synchronized int getQueuedCount() {
        return queue.size();
    }

    public synchronized boolean isBusy() {
        return active != null;
    }

    public synchronized boolean cancelQueued(String buildId) {
        return queue.removeIf(r -> r.getBuildId().equals(buildId));
    }
}
