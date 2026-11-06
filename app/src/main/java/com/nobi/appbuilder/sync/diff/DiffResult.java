package com.nobi.appbuilder.sync.diff;

import java.util.ArrayList;
import java.util.List;

/** Structured result of a diff: every change, no regeneration. */
public class DiffResult {

    private final List<DiffOperation> operations = new ArrayList<>();

    public void add(DiffOperation operation) {
        operations.add(operation);
    }

    public void addAll(List<DiffOperation> operations) {
        this.operations.addAll(operations);
    }

    public List<DiffOperation> getOperations() {
        return operations;
    }

    public boolean hasChanges() {
        return !operations.isEmpty();
    }

    public int size() {
        return operations.size();
    }

    public String getSummary() {
        if (operations.isEmpty()) {
            return "No differences";
        }
        StringBuilder out = new StringBuilder();
        for (DiffOperation operation : operations) {
            if (out.length() > 0) {
                out.append("; ");
            }
            out.append(operation.toString());
        }
        return out.toString();
    }
}
