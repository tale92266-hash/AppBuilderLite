package com.nobi.appbuilder.hierarchy;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.model.ComponentModel;

/**
 * RecyclerView adapter for the hierarchy tree: type thumb, id label,
 * expand arrow, visibility eye and lock toggle per row. Row taps select,
 * long-press toggles multi-select / opens the menu, the thumb starts a
 * drag (reorder / reparent via HierarchyDragController).
 */
public class ComponentTreeAdapter
        extends RecyclerView.Adapter<ComponentTreeAdapter.RowHolder> {

    public interface RowListener {
        void onRowClick(ComponentModel model);

        void onRowLongPress(ComponentModel model);

        void onToggleExpanded(ComponentTreeNode node);

        void onToggleVisibility(ComponentModel model);

        void onToggleLocked(ComponentModel model);

        void onStartDrag(RowHolder holder);
    }

    private final CanvasState state;
    private final ComponentTreeController controller;
    private final RowListener listener;

    public ComponentTreeAdapter(CanvasState state, ComponentTreeController controller,
                                RowListener listener) {
        this.state = state;
        this.controller = controller;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hierarchy, parent, false);
        return new RowHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RowHolder holder, int position) {
        ComponentTreeNode node = controller.getFlatNodes().get(position);
        ComponentModel model = node.getModel();
        // Phase 14: ⚡ marks components that carry at least one event.
        boolean hasEvents = model.getEvents() != null
                && !model.getEvents().isEmpty();
        holder.label.setText(model.getType() + " — " + model.getId()
                + (hasEvents ? "  ⚡" : ""));
        holder.thumb.setText(model.getType() == null || model.getType().isEmpty()
                ? "?" : model.getType().substring(0, 1).toUpperCase());
        int indentPx = (int) (node.getDepth() * 28
                * holder.itemView.getResources().getDisplayMetrics().density + 8);
        holder.itemView.setPadding(indentPx, holder.itemView.getPaddingTop(),
                holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
        if (node.hasChildren()) {
            holder.arrow.setVisibility(View.VISIBLE);
            holder.arrow.setRotation(node.isExpanded() ? 90 : 0);
            holder.arrow.setOnClickListener(v -> listener.onToggleExpanded(node));
        } else {
            holder.arrow.setVisibility(View.INVISIBLE);
            holder.arrow.setOnClickListener(null);
        }
        boolean hidden = "gone".equals(model.getProperty("visibility", "visible"))
                || "invisible".equals(model.getProperty("visibility", "visible"));
        holder.eye.setImageResource(hidden
                ? R.drawable.ic_visibility_off : R.drawable.ic_visibility);
        holder.eye.setOnClickListener(v -> listener.onToggleVisibility(model));
        holder.lock.setImageResource(model.isLocked()
                ? R.drawable.ic_lock : R.drawable.ic_lock_open);
        holder.lock.setOnClickListener(v -> listener.onToggleLocked(model));
        holder.thumb.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                listener.onStartDrag(holder);
                return true;
            }
            return false;
        });
        holder.itemView.setSelected(state.isSelected(model.getId()));
        holder.itemView.setOnClickListener(v -> listener.onRowClick(model));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onRowLongPress(model);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return controller.getFlatNodes().size();
    }

    public ComponentTreeNode getNodeAt(int position) {
        return controller.getFlatNodes().get(position);
    }

    public static class RowHolder extends RecyclerView.ViewHolder {
        final ImageView arrow;
        final TextView thumb;
        final TextView label;
        final ImageButton eye;
        final ImageButton lock;

        RowHolder(View itemView) {
            super(itemView);
            arrow = itemView.findViewById(R.id.image_hierarchy_arrow);
            thumb = itemView.findViewById(R.id.text_hierarchy_thumb);
            label = itemView.findViewById(R.id.text_hierarchy_label);
            eye = itemView.findViewById(R.id.button_hierarchy_eye);
            lock = itemView.findViewById(R.id.button_hierarchy_lock);
        }
    }
}
