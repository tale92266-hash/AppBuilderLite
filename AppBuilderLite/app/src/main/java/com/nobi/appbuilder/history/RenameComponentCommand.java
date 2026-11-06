package com.nobi.appbuilder.history;

import com.nobi.appbuilder.hierarchy.HierarchyValidator;
import com.nobi.appbuilder.model.ComponentModel;

/** Renames a component id; undo restores the previous id. */
public class RenameComponentCommand implements EditorCommand {

    private final ComponentModel root;
    private final ComponentModel model;
    private final String oldId;
    private final String newId;
    private String validationError;

    public RenameComponentCommand(ComponentModel root, ComponentModel model, String newId) {
        this.root = root;
        this.model = model;
        this.oldId = model.getId();
        this.newId = newId == null ? "" : newId.trim();
    }

    @Override
    public boolean canExecute() {
        validationError = HierarchyValidator.validateId(root, newId, oldId);
        return validationError == null && !newId.equals(oldId);
    }

    public String getValidationError() {
        return validationError;
    }

    @Override
    public void execute() {
        model.setId(newId);
    }

    @Override
    public void undo() {
        model.setId(oldId);
    }

    @Override
    public String getDescription() {
        return "Rename to " + newId;
    }
}
