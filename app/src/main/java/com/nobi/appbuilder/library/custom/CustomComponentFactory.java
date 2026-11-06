package com.nobi.appbuilder.library.custom;

import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds a custom component definition from a component subtree.
 * Public properties are derived from the subtree's editable text
 * and colour properties so instances can override them.
 */
public final class CustomComponentFactory {

    private CustomComponentFactory() {
    }

    public static CustomComponentDefinition create(String projectId,
                                                   String name,
                                                   String description,
                                                   ComponentModel root) {
        CustomComponentDefinition definition = new CustomComponentDefinition();
        definition.setId(IDGenerator.newId());
        definition.setProjectId(projectId);
        definition.setName(name);
        definition.setDescription(description == null ? "" : description);
        definition.setRootType(root.getType());
        definition.setComponentsJson(IRSerializer.serializeComponent(root));
        definition.setVersion(1);
        long now = System.currentTimeMillis();
        definition.setCreatedAt(now);
        definition.setUpdatedAt(now);
        definition.setPublicProperties(derivePublicProperties(root));
        return definition;
    }

    private static List<CustomComponentDefinition.PublicProperty>
            derivePublicProperties(ComponentModel root) {
        List<CustomComponentDefinition.PublicProperty> out = new ArrayList<>();
        collect(root, out);
        return out;
    }

    private static void collect(ComponentModel model,
                                List<CustomComponentDefinition.PublicProperty> out) {
        addIfPresent(model, out, "text", "string");
        addIfPresent(model, out, "backgroundColor", "color");
        addIfPresent(model, out, "textColor", "color");
        for (ComponentModel child : model.getChildren()) {
            collect(child, out);
        }
    }

    private static void addIfPresent(ComponentModel model,
                                     List<CustomComponentDefinition.PublicProperty> out,
                                     String key, String type) {
        String value = model.getProperty(key);
        if (value == null) {
            return;
        }
        String label = key.equals("text") ? "Text"
                : key.equals("backgroundColor") ? "Background color"
                : "Text color";
        out.add(new CustomComponentDefinition.PublicProperty(
                label + " (" + model.getId() + ")", model.getId(), key,
                type, value));
    }
}
