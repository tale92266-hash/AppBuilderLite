package com.nobi.appbuilder.hierarchy;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the hierarchy tree: builds the flat node list from the model,
 * and performs select / rename / delete / duplicate / copy / paste /
 * reorder / reparent / visibility / lock operations, each validated
 * before it touches the model.
 */
public class ComponentTreeController {

    public interface TreeListener {
        void onSelectionChanged();

        void onTreeChanged();

        void onMutationStarting();

        void onDocumentCommitted();

        void onTreeError(String message);
    }

    private final CanvasState state;
    private final TreeListener listener;
    private final List<ComponentTreeNode> flatNodes = new ArrayList<>();
    private boolean multiSelectActive;
    private boolean batchMove;
    private String query = "";

    public ComponentTreeController(CanvasState state, TreeListener listener) {
        this.state = state;
        this.listener = listener;
    }

    public List<ComponentTreeNode> getFlatNodes() {
        return flatNodes;
    }

    public ComponentModel getRootModel() {
        return state.getRoot();
    }

    /** Drag sessions push one undo snapshot at the start, not per step. */
    public void beginBatchMove() {
        if (!batchMove) {
            batchMove = true;
            listener.onMutationStarting();
        }
    }

    public void endBatchMove() {
        if (batchMove) {
            batchMove = false;
            listener.onTreeChanged();
            listener.onDocumentCommitted();
        }
    }

    public boolean isMultiSelectActive() {
        return multiSelectActive;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.trim().toLowerCase();
        refresh();
    }

    public void refresh() {
        flatNodes.clear();
        ComponentModel root = state.getRoot();
        if (root != null) {
            addNode(root, null, 0);
        }
        if (!query.isEmpty()) {
            List<ComponentTreeNode> filtered = new ArrayList<>();
            for (ComponentTreeNode node : flatNodes) {
                ComponentModel model = node.getModel();
                String id = model.getId() == null ? "" : model.getId().toLowerCase();
                String type = model.getType() == null ? "" : model.getType().toLowerCase();
                if (id.contains(query) || type.contains(query)) {
                    filtered.add(node);
                }
            }
            flatNodes.clear();
            flatNodes.addAll(filtered);
        }
    }

    private void addNode(ComponentModel model, ComponentTreeNode parent, int depth) {
        boolean expanded = state.isExpanded(model.getId());
        ComponentTreeNode node = new ComponentTreeNode(model, parent, depth, expanded);
        if (parent != null) {
            parent.addChild(node);
        }
        flatNodes.add(node);
        if (expanded) {
            for (ComponentModel child : model.getChildren()) {
                addNode(child, node, depth + 1);
            }
        }
    }

    public void toggleExpanded(ComponentTreeNode node) {
        state.toggleExpanded(node.getModel().getId());
        refresh();
        listener.onTreeChanged();
    }

    public void select(ComponentModel model) {
        state.select(model.getId());
        listener.onSelectionChanged();
    }

    public void toggleMultiSelect(ComponentModel model) {
        multiSelectActive = true;
        state.toggleSelection(model.getId());
        if (!state.hasSelection()) {
            multiSelectActive = false;
        }
        listener.onSelectionChanged();
    }

    public void exitMultiSelect() {
        multiSelectActive = false;
    }

    public String rename(ComponentModel model, String newId) {
        String error = HierarchyValidator.validateId(
                state.getRoot(), newId, model.getId());
        if (error != null) {
            return error;
        }
        listener.onMutationStarting();
        model.setId(newId.trim());
        state.select(newId.trim());
        refresh();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
        return null;
    }

    public void deleteSelected() {
        List<ComponentModel> selected = state.getSelectedModels();
        if (selected.isEmpty()) {
            return;
        }
        listener.onMutationStarting();
        for (ComponentModel model : selected) {
            if (model.isLocked()) {
                continue;
            }
            ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                    model.getId());
            if (parent != null) {
                parent.getChildren().remove(model);
            }
        }
        state.clearSelection();
        multiSelectActive = false;
        refresh();
        listener.onSelectionChanged();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }

    public void duplicate(ComponentModel model) {
        ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                model.getId());
        if (parent == null) {
            parent = state.getRoot();
        }
        if (parent == null) {
            return;
        }
        listener.onMutationStarting();
        ComponentModel copy = ComponentFactory.cloneTree(model);
        copy.setX(model.getX() + 16);
        copy.setY(model.getY() + 16);
        parent.addChild(copy);
        state.select(copy.getId());
        refresh();
        listener.onSelectionChanged();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }

    public void copy(ComponentModel model) {
        state.setClipboard(ComponentFactory.cloneTree(model));
    }

    public void paste() {
        ComponentModel clipboard = state.getClipboard();
        if (clipboard == null || state.getRoot() == null) {
            listener.onTreeError("Nothing copied yet.");
            return;
        }
        ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                clipboard.getId());
        if (parent == null) {
            parent = state.getRoot();
        }
        listener.onMutationStarting();
        ComponentModel copy = ComponentFactory.cloneTree(clipboard);
        copy.setX(copy.getX() + 16);
        copy.setY(copy.getY() + 16);
        parent.addChild(copy);
        state.select(copy.getId());
        refresh();
        listener.onSelectionChanged();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }

    /**
     * Moves a node under a new parent at an index (reparent or reorder).
     * Returns the node's new flat-list index, or -1 when rejected. During
     * a drag batch the adapter is notified by the caller (notifyItemMoved)
     * instead of a full refresh, so the drag is not cancelled.
     */
    public int moveNode(ComponentTreeNode node, ComponentModel newParent, int newIndex) {
        ComponentModel model = node.getModel();
        String error = HierarchyValidator.validateReparent(model, newParent, state.getRoot());
        if (error != null) {
            listener.onTreeError(error);
            return -1;
        }
        ComponentModel oldParent = ComponentHitTester.findParent(state.getRoot(),
                model.getId());
        if (oldParent == null) {
            return -1;
        }
        int fromIndex = oldParent.getChildren().indexOf(model);
        if (!batchMove) {
            listener.onMutationStarting();
        }
        oldParent.getChildren().remove(model);
        int index = newIndex;
        if (oldParent == newParent && fromIndex >= 0 && fromIndex < index) {
            index--;
        }
        index = Math.max(0, Math.min(newParent.getChildren().size(), index));
        newParent.getChildren().add(index, model);
        state.setExpanded(newParent.getId(), true);
        refresh();
        if (!batchMove) {
            listener.onTreeChanged();
            listener.onDocumentCommitted();
        }
        for (int i = 0; i < flatNodes.size(); i++) {
            if (flatNodes.get(i).getModel().getId().equals(model.getId())) {
                return i;
            }
        }
        return -1;
    }

    public void reorder(ComponentModel model, boolean forward) {
        ComponentModel parent = ComponentHitTester.findParent(state.getRoot(),
                model.getId());
        if (parent == null) {
            return;
        }
        List<ComponentModel> children = parent.getChildren();
        int index = children.indexOf(model);
        int swap = forward ? index + 1 : index - 1;
        String error = HierarchyValidator.validateReorder(model, parent, swap);
        if (error != null) {
            return;
        }
        listener.onMutationStarting();
        children.set(index, children.get(swap));
        children.set(swap, model);
        refresh();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }

    public void setVisibility(ComponentModel model, boolean visible) {
        listener.onMutationStarting();
        model.setProperty("visibility", visible ? "visible" : "gone");
        refresh();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }

    public void setLocked(ComponentModel model, boolean locked) {
        listener.onMutationStarting();
        model.setLocked(locked);
        refresh();
        listener.onTreeChanged();
        listener.onDocumentCommitted();
    }
}
