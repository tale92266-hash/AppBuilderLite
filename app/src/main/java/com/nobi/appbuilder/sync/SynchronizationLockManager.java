package com.nobi.appbuilder.sync;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One re-entrant lock per project. A sync run holds its project's
 * lock for the whole run so two runs (visual + code) can never
 * interleave and corrupt the IR or the XML.
 */
public class SynchronizationLockManager {

    private static final Map<String, ReentrantLock> LOCKS = new HashMap<>();

    public synchronized boolean tryLock(String projectId) {
        ReentrantLock lock = LOCKS.get(projectId);
        if (lock == null) {
            lock = new ReentrantLock();
            LOCKS.put(projectId, lock);
        }
        return lock.tryLock();
    }

    public synchronized void unlock(String projectId) {
        ReentrantLock lock = LOCKS.get(projectId);
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    public synchronized boolean isLocked(String projectId) {
        ReentrantLock lock = LOCKS.get(projectId);
        return lock != null && lock.isLocked();
    }
}
