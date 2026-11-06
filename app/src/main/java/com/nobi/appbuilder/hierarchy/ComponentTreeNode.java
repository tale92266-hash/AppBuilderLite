package com.nobi.appbuilder.hierarchy;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/** One row of the hierarchy tree: a component plus its place in it. */
public class ComponentTreeNode {

    private final ComponentModel model;
    private final ComponentTreeNode parent;
    private final int depth;
    private final boolean expanded;
    private final List<ComponentTreeNode> children = new ArrayList<>();

    public ComponentTreeNode(ComponentModel model, ComponentTreeNode parent,
                             int depth, boolean expanded) {
        this.model = model;
        this.parent = parent;
        this.depth = depth;
        this.expanded = expanded;
    }

    public ComponentModel getModel() {
        return model;
    }

    public ComponentTreeNode getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean hasChildren() {
        return !model.getChildren().isEmpty();
    }

    public List<ComponentTreeNode> getChildren() {
        return children;
    }

    public void addChild(ComponentTreeNode child) {
        children.add(child);
    }
}
