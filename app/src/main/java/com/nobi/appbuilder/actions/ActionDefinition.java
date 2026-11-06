package com.nobi.appbuilder.actions;

import java.util.ArrayList;
import java.util.List;

/**
 * The static definition of one action: its id, display name,
 * parameters and the triggers it may be attached to.
 */
public class ActionDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final String category;
    private final List<ActionParameter> parameters = new ArrayList<>();
    private final List<String> supportedTriggers = new ArrayList<>();

    public ActionDefinition(String id, String displayName, String description,
                            String category) {
        this.id = id;
        this.displayName = displayName;
        this.description = description;
        this.category = category;
    }

    public ActionDefinition param(String name, String type, boolean required,
                                  String defaultValue, String description) {
        parameters.add(new ActionParameter(name, type, required,
                defaultValue, description));
        return this;
    }

    public ActionDefinition trigger(String trigger) {
        supportedTriggers.add(trigger);
        return this;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public List<ActionParameter> getParameters() {
        return parameters;
    }

    public List<String> getSupportedTriggers() {
        return supportedTriggers;
    }

    public ActionParameter getParameter(String name) {
        for (ActionParameter param : parameters) {
            if (name != null && name.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }
}
