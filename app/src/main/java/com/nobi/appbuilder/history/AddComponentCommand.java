package com.nobi.appbuilder.history;

import com.nobi.appbuilder.model.ComponentModel;

/** Inserts a new component into a parent at an index (or at the end). */
public class AddComponentCommand implements EditorCommand {

    private final ComponentModel parent;
    private final ComponentModel model;
    private final int index;

    public AddComponentCommand(ComponentModel parent, ComponentModel model, int index) {
        this.parent = parent;
        this.model = model;
        this.index = index;
    }

    @Override
    public boolean canExecute() {
        return parent != null && model != null && parent.findById(model.getId()) == null;
    }

    @Override
    public void execute() {
        if (index >= 0 && index <= parent.getChildren().size()) {
            parent.getChildren().add(index, model);
        } else {
            parent.addChild(model);
        }
    }

    @Override
    public void undo() {
        parent.getChildren().remove(model);
    }

    @Override
    public String getDescription() {
        return "Add " + model.getType();
    }

    public ComponentModel getModel() {
        return model;
    }
}
