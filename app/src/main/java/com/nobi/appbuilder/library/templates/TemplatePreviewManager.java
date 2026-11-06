package com.nobi.appbuilder.library.templates;

import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.model.ComponentModel;

/** Preview metadata for a template: counts and types, no rendering. */
public final class TemplatePreviewManager {

    private TemplatePreviewManager() {
    }

    public static int componentCount(TemplateDefinition template) {
        if (template == null || template.getScreenJson() == null) {
            return 0;
        }
        try {
            return count(IRDeserializer.deserializeComponent(
                    template.getScreenJson()));
        } catch (Exception e) {
            return 0;
        }
    }

    private static int count(ComponentModel model) {
        if (model == null) {
            return 0;
        }
        int total = 1;
        for (ComponentModel child : model.getChildren()) {
            total += count(child);
        }
        return total;
    }

    public static String previewSummary(TemplateDefinition template) {
        if (template == null) {
            return "";
        }
        return template.getName() + " · "
                + template.getCategory().getDisplayName() + " · "
                + componentCount(template) + " components · "
                + String.join(", ", template.getRequiredTypes());
    }
}
