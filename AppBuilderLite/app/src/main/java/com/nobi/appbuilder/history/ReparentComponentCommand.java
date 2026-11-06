package com.nobi.appbuilder.history;

import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.hierarchy.HierarchyValidator;
import com.nobi.appbuilder.model.ComponentModel;

/** Moves a component under a new parent (hierarchy drag or drop). */
public class ReparentComponentCommand implements EditorCommand {

    private final ComponentModel root;
    private final ComponentModel model;
    private final ComponentModel newParent;
    private final int newIndex;
    private ComponentModel oldParent;
    private int oldIndex = -1;

    public ReparentComponentCommand(ComponentModel root, ComponentModel model,
                                    ComponentModel newParent, int newIndex) {
        this.root = root;
        this.model = model;
        this.newParent = newParent;
        this.newIndex = newIndex;
        resolveOld();
    }

    private void resolveOld() {
        oldParent = ComponentHitTester.findParent(root, model.getId());
        oldIndex = oldParent == null ? -1 : oldParent.getChildren().indexOf(model);
    }

    @Override
    public boolean canExecute() {
        if (model == null || newParent == null || model.isLocked()) {
            return false;
        }
        if (oldParent == null || oldIndex < 0) {
            return false;
        }
        if (oldParent == newParent && oldIndex == newIndex) {
            return false;
        }
        return HierarchyValidator.validateReparent(model, newParent, root) == null;
    }

    @Override
    public void execute() {
        resolveOld();
        moveTo(newParent, newIndex);
    }

    @Override
    public void undo() {
        moveTo(oldParent, oldIndex);
    }

    private void moveTo(ComponentModel parent, int index) {
        if (parent == null) {
            return;
        }
        ComponentModel current = ComponentHitTester.findParent(root, model.getId());
        if (current != null) {
            current.getChildren().remove(model);
        }
        int at = Math.max(0, Math.min(index, parent.getChildren().size()));
        parent.getChildren().add(at, model);
    }

    @Override
    public String getDescription() {
        return "Move " + model.getType() + " to " + newParent.getType();
    }
}
