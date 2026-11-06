package com.nobi.appbuilder.history;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

/** Duplicates a component (whole subtree, fresh ids) next to the original. */
public class DuplicateComponentCommand implements EditorCommand {

    private final ComponentModel root;
    private final ComponentModel source;
    private final ComponentModel copy;
    private ComponentModel parent;

    public DuplicateComponentCommand(ComponentModel root, ComponentModel source) {
        this.root = root;
        this.source = source;
        this.copy = ComponentFactory.cloneTree(source);
        this.copy.setX(source.getX() + 16);
        this.copy.setY(source.getY() + 16);
        resolveParent();
    }

    private void resolveParent() {
        parent = ComponentHitTester.findParent(root, source.getId());
        if (parent == null) {
            parent = root;
        }
    }

    @Override
    public boolean canExecute() {
        return source != null && copy != null && parent != null;
    }

    @Override
    public void execute() {
        resolveParent();
        if (!parent.getChildren().contains(copy)) {
            parent.addChild(copy);
        }
    }

    @Override
    public void undo() {
        if (parent != null) {
            parent.getChildren().remove(copy);
        }
    }

    @Override
    public String getDescription() {
        return "Duplicate " + source.getType();
    }

    public ComponentModel getCopy() {
        return copy;
    }
}
