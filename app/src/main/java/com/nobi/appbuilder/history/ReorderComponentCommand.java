package com.nobi.appbuilder.history;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;

/** Moves a component to another index inside its parent. */
public class ReorderComponentCommand implements EditorCommand {

    private final ComponentModel root;
    private final ComponentModel model;
    private final int toIndex;
    private ComponentModel parent;
    private int fromIndex = -1;

    public ReorderComponentCommand(ComponentModel root, ComponentModel model, int toIndex) {
        this.root = root;
        this.model = model;
        this.toIndex = toIndex;
        resolveParent();
    }

    private void resolveParent() {
        parent = ComponentHitTester.findParent(root, model.getId());
        fromIndex = parent == null ? -1 : parent.getChildren().indexOf(model);
    }

    @Override
    public boolean canExecute() {
        return model != null && !model.isLocked() && parent != null && fromIndex >= 0
                && toIndex >= 0 && toIndex < parent.getChildren().size()
                && toIndex != fromIndex;
    }

    @Override
    public void execute() {
        resolveParent();
        moveTo(toIndex);
    }

    @Override
    public void undo() {
        moveTo(fromIndex);
    }

    private void moveTo(int index) {
        if (parent == null) {
            return;
        }
        List<ComponentModel> children = parent.getChildren();
        children.remove(model);
        int at = Math.max(0, Math.min(index, children.size()));
        children.add(at, model);
    }

    @Override
    public String getDescription() {
        return "Reorder " + model.getType();
    }
}
