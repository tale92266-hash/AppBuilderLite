package com.nobi.appbuilder.ui.editor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The component tree: expandable rows (type + id), indented by depth.
 * Tapping a row selects the same component on the canvas; tapping the
 * arrow expands or collapses a container; long-press opens its menu.
 */
public class HierarchyController {

    public interface HierarchyListener {
        void onHierarchySelect(String componentId);

        void onHierarchyLongPress(ComponentModel model);
    }

    private static class Row {
        final ComponentModel model;
        final int depth;
        final boolean hasChildren;
        final boolean expanded;

        Row(ComponentModel model, int depth, boolean hasChildren, boolean expanded) {
            this.model = model;
            this.depth = depth;
            this.hasChildren = hasChildren;
            this.expanded = expanded;
        }
    }

    private final CanvasState state;
    private final HierarchyListener listener;
    private final List<Row> rows = new ArrayList<>();
    private final HierarchyAdapter adapter = new HierarchyAdapter();

    public HierarchyController(RecyclerView recyclerView, CanvasState state,
                               HierarchyListener listener) {
        this.state = state;
        this.listener = listener;
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public void refresh() {
        rows.clear();
        ComponentModel root = state.getRoot();
        if (root != null) {
            addRows(root, 0);
        }
        adapter.notifyDataSetChanged();
    }

    public void syncSelection() {
        adapter.notifyDataSetChanged();
    }

    private void addRows(ComponentModel node, int depth) {
        boolean hasChildren = !node.getChildren().isEmpty();
        boolean expanded = state.isExpanded(node.getId());
        rows.add(new Row(node, depth, hasChildren, expanded));
        if (hasChildren && expanded) {
            for (ComponentModel child : node.getChildren()) {
                addRows(child, depth + 1);
            }
        }
    }

    private class HierarchyAdapter extends RecyclerView.Adapter<HierarchyAdapter.RowHolder> {

        @NonNull
        @Override
        public RowHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_hierarchy, parent, false);
            return new RowHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RowHolder holder, int position) {
            Row row = rows.get(position);
            ComponentModel model = row.model;
            holder.label.setText(model.getType() + " — " + model.getId());
            int indentPx = (int) (row.depth * 28
                    * holder.itemView.getResources().getDisplayMetrics().density + 8);
            holder.itemView.setPadding(indentPx, holder.itemView.getPaddingTop(),
                    holder.itemView.getPaddingRight(), holder.itemView.getPaddingBottom());
            if (row.hasChildren) {
                holder.arrow.setVisibility(View.VISIBLE);
                holder.arrow.setRotation(row.expanded ? 90 : 0);
                holder.arrow.setOnClickListener(v -> {
                    state.toggleExpanded(model.getId());
                    refresh();
                });
            } else {
                holder.arrow.setVisibility(View.INVISIBLE);
                holder.arrow.setOnClickListener(null);
            }
            holder.itemView.setSelected(state.isSelected(model.getId()));
            holder.itemView.setOnClickListener(v -> {
                state.select(model.getId());
                listener.onHierarchySelect(model.getId());
                adapter.notifyDataSetChanged();
            });
            holder.itemView.setOnLongClickListener(v -> {
                listener.onHierarchyLongPress(model);
                return true;
            });
        }

        @Override
        public int getItemCount() {
            return rows.size();
        }

        class RowHolder extends RecyclerView.ViewHolder {
            final ImageView arrow;
            final TextView label;

            RowHolder(View itemView) {
                super(itemView);
                arrow = itemView.findViewById(R.id.image_hierarchy_arrow);
                label = itemView.findViewById(R.id.text_hierarchy_label);
            }
        }
    }
}
