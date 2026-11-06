package com.nobi.appbuilder.hierarchy;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.model.ComponentModel;

/**
 * Drag-and-drop reordering for the hierarchy: drag a row up/down to
 * reorder siblings, or drop it onto an expanded container row to
 * reparent under that container. Invalid drops are rejected by the
 * validator with a message instead of silently failing.
 */
public class HierarchyDragController extends ItemTouchHelper.Callback {

    private final ComponentTreeController controller;
    private final ComponentTreeAdapter adapter;
    private ItemTouchHelper touchHelper;

    public HierarchyDragController(ComponentTreeController controller,
                                   ComponentTreeAdapter adapter) {
        this.controller = controller;
        this.adapter = adapter;
    }

    public void attachToRecyclerView(RecyclerView recyclerView) {
        touchHelper = new ItemTouchHelper(this);
        touchHelper.attachToRecyclerView(recyclerView);
    }

    public void startDrag(ComponentTreeAdapter.RowHolder holder) {
        if (touchHelper != null) {
            touchHelper.startDrag(holder);
        }
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder,
                          @NonNull RecyclerView.ViewHolder target) {
        int from = viewHolder.getAdapterPosition();
        int to = target.getAdapterPosition();
        if (from == RecyclerView.NO_POSITION || to == RecyclerView.NO_POSITION
                || from == to) {
            return false;
        }
        ComponentTreeNode fromNode = adapter.getNodeAt(from);
        ComponentTreeNode toNode = adapter.getNodeAt(to);
        ComponentModel model = fromNode.getModel();
        ComponentModel root = controller.getRootModel();
        if (model.getId() == null || root == null
                || model.getId().equals(root.getId())) {
            return false;
        }
        if (toNode.hasChildren() && toNode.isExpanded()
                && !toNode.getModel().getId().equals(model.getId())) {
            ComponentModel newParent = toNode.getModel();
            String error = HierarchyValidator.validateReparent(model, newParent, root);
            if (error != null) {
                return false;
            }
            int newIndex = controller.moveNode(fromNode, newParent,
                    newParent.getChildren().size());
            if (newIndex < 0) {
                return false;
            }
            adapter.notifyItemMoved(from, newIndex);
            return true;
        }
        ComponentTreeNode toParent = toNode.getParent();
        if (toParent == null) {
            return false;
        }
        ComponentModel newParent = toParent.getModel();
        int index = newParent.getChildren().indexOf(toNode.getModel());
        if (from < to) {
            index++;
        }
        int newIndex = controller.moveNode(fromNode, newParent, index);
        if (newIndex < 0) {
            return false;
        }
        adapter.notifyItemMoved(from, newIndex);
        return true;
    }

    @Override
    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            controller.beginBatchMove();
        } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
            controller.endBatchMove();
        }
    }
}
