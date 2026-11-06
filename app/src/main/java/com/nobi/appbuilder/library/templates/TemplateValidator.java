package com.nobi.appbuilder.library.templates;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/** Validates a template definition before it is saved or applied. */
public final class TemplateValidator {

    private TemplateValidator() {
    }

    public static List<String> validate(TemplateDefinition template) {
        List<String> errors = new ArrayList<>();
        if (template == null) {
            errors.add("Missing template");
            return errors;
        }
        if (template.getName() == null || template.getName().trim().isEmpty()) {
            errors.add("Template needs a name");
        }
        if (template.getScreenName() == null
                || template.getScreenName().trim().isEmpty()) {
            errors.add("Template needs a screen name");
        }
        if (template.getScreenJson() == null
                || template.getScreenJson().isEmpty()) {
            errors.add("Template has no screen content");
            return errors;
        }
        ComponentModel root;
        try {
            root = IRDeserializer.deserializeComponent(template.getScreenJson());
        } catch (Exception e) {
            errors.add("Template screen is not valid JSON: " + e.getMessage());
            return errors;
        }
        if (root == null) {
            errors.add("Template screen could not be read");
            return errors;
        }
        collectUnknown(root, errors);
        return errors;
    }

    private static void collectUnknown(ComponentModel model,
                                       List<String> errors) {
        if (!ComponentRegistry.contains(model.getType())) {
            errors.add("Template uses unknown component type: "
                    + model.getType());
        }
        for (ComponentModel child : model.getChildren()) {
            collectUnknown(child, errors);
        }
    }
}
