package com.nobi.appbuilder.clipboard;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * What a copy puts on the clipboard: cloned component subtrees (fresh
 * ids, full properties and nested children) plus the screen they came
 * from, so cross-screen paste can be detected. Editor-only state
 * (selection handles, gesture state, canvas coordinates beyond the
 * model's own x/y) is never copied.
 */
public class ClipboardData {

    private final List<ComponentModel> models = new ArrayList<>();
    private final String sourceScreenId;
    private final long copiedAt;

    public ClipboardData(List<ComponentModel> models, String sourceScreenId) {
        for (ComponentModel model : models) {
            this.models.add(model);
        }
        this.sourceScreenId = sourceScreenId;
        this.copiedAt = System.currentTimeMillis();
    }

    public List<ComponentModel> getModels() {
        return models;
    }

    public String getSourceScreenId() {
        return sourceScreenId;
    }

    public long getCopiedAt() {
        return copiedAt;
    }

    public boolean isEmpty() {
        return models.isEmpty();
    }

    public int size() {
        return models.size();
    }

    public boolean isCrossScreen(String targetScreenId) {
        return sourceScreenId != null && !sourceScreenId.equals(targetScreenId);
    }
}
