package com.nobi.appbuilder.history;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

/** Removes a component (with its whole subtree); undo restores it. */
public class DeleteComponentCommand implements EditorCommand {

    private final ComponentModel root;
    private final ComponentModel model;
    private ComponentModel parent;
    private int index = -1;

    public DeleteComponentCommand(ComponentModel root, ComponentModel model) {
        this.root = root;
        this.model = model;
        resolveParent();
    }

    private void resolveParent() {
        parent = ComponentHitTester.findParent(root, model.getId());
        index = parent == null ? -1 : parent.getChildren().indexOf(model);
    }

    @Override
    public boolean canExecute() {
        return model != null && !model.isLocked() && parent != null && index >= 0;
    }

    @Override
    public void execute() {
        resolveParent();
        if (parent != null) {
            parent.getChildren().remove(model);
        }
    }

    @Override
    public void undo() {
        if (parent == null) {
            return;
        }
        int at = Math.max(0, Math.min(index, parent.getChildren().size()));
        parent.getChildren().add(at, model);
    }

    @Override
    public String getDescription() {
        return "Delete " + model.getType();
    }
}
