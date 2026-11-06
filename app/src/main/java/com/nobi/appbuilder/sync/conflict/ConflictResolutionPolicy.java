package com.nobi.appbuilder.sync.conflict;

import com.nobi.appbuilder.sync.SynchronizationPolicy;

/** Decides whether a conflict is auto-resolved or must be asked. */
public class ConflictResolutionPolicy {

    private final SynchronizationPolicy policy;

    public ConflictResolutionPolicy(SynchronizationPolicy policy) {
        this.policy = policy;
    }

    /** Resolution to apply automatically, or null to ask the user. */
    public ConflictResolution autoResolution() {
        String configured = policy.getConflictPolicy();
        if (SynchronizationPolicy.POLICY_KEEP_VISUAL.equals(configured)) {
            return ConflictResolution.KEEP_VISUAL;
        }
        if (SynchronizationPolicy.POLICY_KEEP_CODE.equals(configured)) {
            return ConflictResolution.KEEP_CODE;
        }
        return null;
    }

    public boolean shouldAsk() {
        return autoResolution() == null;
    }
}
