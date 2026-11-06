package com.nobi.appbuilder.ui.editor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.hierarchy.ComponentTreeAdapter;
import com.nobi.appbuilder.hierarchy.ComponentTreeController;
import com.nobi.appbuilder.hierarchy.ComponentTreeNode;
import com.nobi.appbuilder.hierarchy.HierarchyDragController;
import com.nobi.appbuilder.model.ComponentModel;

/**
 * The hierarchy bottom panel: collapsible tree with search, drag to
 * reorder/reparent, visibility and lock toggles, and multi-select.
 * Supersedes the Phase 3 HierarchyController + EditorPanelController
 * (their behaviour lives in the hierarchy package now).
 */
public class HierarchyPanelController {

    public interface HierarchyPanelListener {
        void onHierarchySelect(String componentId);

        void onHierarchyLongPress(ComponentModel model);

        void onMutationStarting();

        void onDocumentCommitted();

        void onTreeError(String message);
    }

    private final RecyclerView list;
    private final EditText searchView;
    private final ImageView chevron;
    private final TextView selectedInfo;
    private final ComponentTreeController treeController;
    private final ComponentTreeAdapter adapter;
    private boolean expanded;

    public HierarchyPanelController(View header, RecyclerView list, ImageView chevron,
                                    TextView selectedInfo, EditText searchView,
                                    CanvasState state, HierarchyPanelListener listener) {
        this.list = list;
        this.searchView = searchView;
        this.chevron = chevron;
        this.selectedInfo = selectedInfo;
        this.treeController = new ComponentTreeController(state,
                new ComponentTreeController.TreeListener() {
                    @Override
                    public void onSelectionChanged() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onTreeChanged() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onMutationStarting() {
                        listener.onMutationStarting();
                    }

                    @Override
                    public void onDocumentCommitted() {
                        listener.onDocumentCommitted();
                    }

                    @Override
                    public void onTreeError(String message) {
                        listener.onTreeError(message);
                    }
                });
        this.adapter = new ComponentTreeAdapter(state, treeController,
                new ComponentTreeAdapter.RowListener() {
                    @Override
                    public void onRowClick(ComponentModel model) {
                        if (treeController.isMultiSelectActive()) {
                            treeController.toggleMultiSelect(model);
                        } else {
                            treeController.select(model);
                            listener.onHierarchySelect(model.getId());
                        }
                    }

                    @Override
                    public void onRowLongPress(ComponentModel model) {
                        if (treeController.isMultiSelectActive()) {
                            treeController.toggleMultiSelect(model);
                        } else {
                            listener.onHierarchyLongPress(model);
                        }
                    }

                    @Override
                    public void onToggleExpanded(ComponentTreeNode node) {
                        treeController.toggleExpanded(node);
                    }

                    @Override
                    public void onToggleVisibility(ComponentModel model) {
                        boolean hidden = "gone".equals(
                                model.getProperty("visibility", "visible"));
                        treeController.setVisibility(model, hidden);
                    }

                    @Override
                    public void onToggleLocked(ComponentModel model) {
                        treeController.setLocked(model, !model.isLocked());
                    }

                    @Override
                    public void onStartDrag(ComponentTreeAdapter.RowHolder holder) {
                        dragController.startDrag(holder);
                    }
                });
        HierarchyDragController dragController = new HierarchyDragController(
                treeController, adapter);
        dragController.attachToRecyclerView(list);
        this.dragController = dragController;
        list.setLayoutManager(new LinearLayoutManager(list.getContext()));
        list.setAdapter(adapter);
        header.setOnClickListener(v -> toggle());
        searchView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                treeController.setQuery(s == null ? "" : s.toString());
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        setExpanded(false);
    }

    private final HierarchyDragController dragController;

    public ComponentTreeController getTreeController() {
        return treeController;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggle() {
        setExpanded(!expanded);
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        list.setVisibility(expanded ? View.VISIBLE : View.GONE);
        searchView.setVisibility(expanded ? View.VISIBLE : View.GONE);
        chevron.setRotation(expanded ? 180 : 0);
    }

    public void setSelectedInfo(String info) {
        selectedInfo.setText(info);
    }

    public void refresh() {
        treeController.refresh();
        adapter.notifyDataSetChanged();
    }

    public void syncSelection() {
        adapter.notifyDataSetChanged();
    }
}
