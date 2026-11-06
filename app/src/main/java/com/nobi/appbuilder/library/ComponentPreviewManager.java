package com.nobi.appbuilder.library;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;

/**
 * Lightweight previews for library cards: one small real View built
 * from the item's base type + variant overrides, or a plain label
 * when the type cannot be previewed. Never builds a whole screen.
 */
public class ComponentPreviewManager {

    public View buildPreview(Context context, ComponentLibraryItem item) {
        if (item == null || !item.isSupported()) {
            TextView label = new TextView(context);
            label.setText(item == null ? "?" : item.getDisplayName());
            return label;
        }
        ComponentDefinition definition =
                ComponentRegistry.get(item.getBaseType());
        if (definition == null || definition.getPreviewBuilder() == null) {
            TextView label = new TextView(context);
            label.setText(item.getDisplayName());
            return label;
        }
        ComponentModel model = ComponentFactory.create(item.getBaseType());
        for (java.util.Map.Entry<String, String> entry
                : item.getDefaultProperties().entrySet()) {
            model.setProperty(entry.getKey(), entry.getValue());
        }
        try {
            return definition.getPreviewBuilder().build(context, model);
        } catch (Exception e) {
            TextView label = new TextView(context);
            label.setText(item.getDisplayName());
            return label;
        }
    }

    /** One-line text summary for a details panel. */
    public String previewSummary(ComponentLibraryItem item) {
        if (item == null) {
            return "";
        }
        if (!item.isSupported()) {
            return item.getDisplayName() + " — not available: "
                    + item.getUnsupportedReason();
        }
        return item.getDisplayName() + " · " + item.getBaseType()
                + " · " + item.getCategory().getDisplayName();
    }
}
