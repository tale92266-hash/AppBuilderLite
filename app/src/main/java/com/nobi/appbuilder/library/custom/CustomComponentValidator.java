package com.nobi.appbuilder.library.custom;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/** Validates a custom component definition before it is saved. */
public final class CustomComponentValidator {

    private CustomComponentValidator() {
    }

    public static List<String> validate(CustomComponentDefinition definition) {
        List<String> errors = new ArrayList<>();
        if (definition == null) {
            errors.add("Missing custom component definition");
            return errors;
        }
        if (definition.getName() == null
                || definition.getName().trim().isEmpty()) {
            errors.add("Custom component needs a name");
        }
        if (definition.getComponentsJson() == null
                || definition.getComponentsJson().isEmpty()) {
            errors.add("Custom component has no components");
            return errors;
        }
        ComponentModel root;
        try {
            root = IRDeserializer.deserializeComponent(
                    definition.getComponentsJson());
        } catch (Exception e) {
            errors.add("Saved components are not valid JSON: "
                    + e.getMessage());
            return errors;
        }
        if (root == null) {
            errors.add("Saved components could not be read");
            return errors;
        }
        if (!ComponentRegistry.contains(root.getType())) {
            errors.add("Unknown root component type: " + root.getType());
        }
        for (CustomComponentDefinition.PublicProperty property
                : definition.getPublicProperties()) {
            if (property.targetComponentId == null
                    || root.findById(property.targetComponentId) == null) {
                errors.add("Public property '" + property.name
                        + "' points to a missing component");
            }
            if (property.propertyKey == null
                    || property.propertyKey.isEmpty()) {
                errors.add("Public property '" + property.name
                        + "' has no property key");
            }
            if (property.type == null || property.type.isEmpty()) {
                errors.add("Public property '" + property.name
                        + "' has no type");
            }
        }
        return errors;
    }
}
