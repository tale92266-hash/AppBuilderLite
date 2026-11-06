package com.nobi.appbuilder.history;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.properties.PropertyBinding;
import com.nobi.appbuilder.properties.PropertyDefinition;
import com.nobi.appbuilder.properties.PropertyRegistry;
import com.nobi.appbuilder.properties.PropertyValidator;
import com.nobi.appbuilder.properties.PropertyValue;

/**
 * Changes one property (including visibility and lock state, which
 * PropertyBinding maps to the model property and editorMetadata).
 * Validation runs before execute, so an invalid value never lands.
 */
public class UpdatePropertyCommand implements EditorCommand {

    private final ComponentModel model;
    private final ComponentModel root;
    private final PropertyDefinition definition;
    private final PropertyValue oldValue;
    private final PropertyValue newValue;
    private String validationError;

    public UpdatePropertyCommand(ComponentModel model, ComponentModel root,
                                 PropertyDefinition definition,
                                 PropertyValue oldValue, PropertyValue newValue) {
        this.model = model;
        this.root = root;
        this.definition = definition;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    /** Convenience constructor for key-based updates. */
    public UpdatePropertyCommand(ComponentModel model, ComponentModel root,
                                 String key, String oldRaw, String newRaw) {
        this(model, root, PropertyRegistry.get(key),
                oldRaw == null ? null : PropertyValue.ofString(oldRaw),
                newRaw == null ? null : PropertyValue.ofString(newRaw));
    }

    @Override
    public boolean canExecute() {
        if (model == null || definition == null || newValue == null) {
            return false;
        }
        if (oldValue != null && oldValue.equals(newValue)) {
            return false;
        }
        validationError = PropertyValidator.validate(definition, newValue, root,
                model.getId());
        return validationError == null;
    }

    public String getValidationError() {
        return validationError;
    }

    @Override
    public void execute() {
        PropertyBinding.setValue(model, definition, newValue);
    }

    @Override
    public void undo() {
        if (oldValue != null) {
            PropertyBinding.setValue(model, definition, oldValue);
        }
    }

    @Override
    public String getDescription() {
        return "Change " + definition.getDisplayName();
    }
}
