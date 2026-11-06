package com.nobi.appbuilder.library.templates;

import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A screen template: metadata plus the screen's root component as
 * JSON IR. Applying a template always generates fresh component
 * ids, so two screens from one template never collide.
 */
public class TemplateDefinition {

    private String id;
    private String projectId;
    private String name;
    private TemplateCategory category;
    private String description;
    private int version;
    private String screenName;
    private String screenJson;
    private boolean builtIn;
    private long createdAt;

    public TemplateDefinition() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TemplateCategory getCategory() {
        return category;
    }

    public void setCategory(TemplateCategory category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getScreenJson() {
        return screenJson;
    }

    public void setScreenJson(String screenJson) {
        this.screenJson = screenJson;
    }

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    /** Component types used by this template, in first-use order. */
    public List<String> getRequiredTypes() {
        List<String> types = new ArrayList<>();
        if (screenJson == null || screenJson.isEmpty()) {
            return types;
        }
        try {
            ComponentModel root = IRDeserializer.deserializeComponent(screenJson);
            collectTypes(root, new LinkedHashSet<String>(), types);
        } catch (Exception ignored) {
        }
        return types;
    }

    private void collectTypes(ComponentModel model, Set<String> seen,
                              List<String> out) {
        if (model == null || !seen.add(model.getType())) {
            return;
        }
        out.add(model.getType());
        for (ComponentModel child : model.getChildren()) {
            collectTypes(child, seen, out);
        }
    }
}
