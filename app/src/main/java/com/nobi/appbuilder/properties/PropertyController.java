package com.nobi.appbuilder.properties;

import android.content.Context;
import android.view.View;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;

/**
 * The single entry point for property edits. Validates first, writes
 * second, notifies last — so the canvas, the hierarchy and the JSON
 * document can never disagree about a value.
 */
public class PropertyController {

    /** Row-level callback: one editor produced a new value. */
    public interface ValueListener {
        void onValue(PropertyValue newValue);
    }

    /** Contract every property editor (properties/editors/*) implements. */
    public interface Editor {
        /** Builds the editor widget for its row. */
        View createRow(Context context);

        /** Shows a new value without firing the listener. */
        void setValue(PropertyValue value);
    }

    private final PropertyChangeListener listener;

    public PropertyController(PropertyChangeListener listener) {
        this.listener = listener;
    }

    public List<PropertyDefinition> getDefinitionsFor(ComponentModel model,
                                                      ComponentModel root) {
        String parentType = null;
        if (root != null) {
            ComponentModel parent = ComponentHitTester.findParent(root, model.getId());
            parentType = parent == null ? null : parent.getType();
        }
        return PropertyRegistry.getDefinitionsFor(model.getType(), parentType);
    }

    public PropertyValue getValue(ComponentModel model, PropertyDefinition definition) {
        return PropertyBinding.getValue(model, definition);
    }

    /**
     * Validates and applies one edit. Returns null on success, or the
     * validation error when the value was rejected (model untouched).
     */
    public String setValue(ComponentModel model, PropertyDefinition definition,
                           PropertyValue value, ComponentModel root) {
        String error = PropertyValidator.validate(definition, value, root, model.getId());
        if (error != null) {
            if (listener != null) {
                listener.onPropertyValidationFailed(definition, error);
            }
            return error;
        }
        PropertyValue oldValue = PropertyBinding.getValue(model, definition);
        if (oldValue.equals(value)) {
            return null;
        }
        PropertyBinding.setValue(model, definition, value);
        if (listener != null) {
            listener.onPropertyChanged(model, definition, oldValue, value);
        }
        return null;
    }

    /** Resets a component to its schema defaults (id and type kept). */
    public void resetToDefaults(ComponentModel model, ComponentModel root) {
        List<PropertyDefinition> definitions = getDefinitionsFor(model, root);
        PropertyBinding.resetToDefaults(model, definitions);
    }
}
