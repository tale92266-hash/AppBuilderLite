package com.nobi.appbuilder.sync;

import java.util.ArrayList;
import java.util.List;

/** A batch of related ChangeOperations for one screen. */
public class ChangeSet {

    private final String screenId;
    private final ChangeOrigin origin;
    private final long timestamp;
    private final List<ChangeOperation> operations = new ArrayList<>();

    public ChangeSet(String screenId, ChangeOrigin origin) {
        this.screenId = screenId;
        this.origin = origin;
        this.timestamp = System.currentTimeMillis();
    }

    public void add(ChangeOperation operation) {
        operations.add(operation);
    }

    public void addAll(List<ChangeOperation> operations) {
        this.operations.addAll(operations);
    }

    public boolean isEmpty() {
        return operations.isEmpty();
    }

    public int size() {
        return operations.size();
    }

    public String getScreenId() {
        return screenId;
    }

    public ChangeOrigin getOrigin() {
        return origin;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<ChangeOperation> getOperations() {
        return operations;
    }
}
