package com.nobi.appbuilder.ui.editor;

import android.view.View;
import android.widget.Button;

import com.nobi.appbuilder.R;

/**
 * The contextual selection toolbar (Phase 5): appears above the
 * hierarchy panel whenever something is selected, with group /
 * ungroup / delete / lock / visibility / select-all actions.
 */
public class SelectionToolbarController {

    public interface SelectionToolbarListener {
        void onGroup();

        void onUngroup();

        void onDelete();

        void onToggleLock();

        void onVisibility();

        void onSelectAll();
    }

    private final View toolbar;
    private final Button groupButton;
    private final Button ungroupButton;
    private final Button deleteButton;
    private final Button lockButton;
    private final Button visibilityButton;

    public SelectionToolbarController(View toolbar, SelectionToolbarListener listener) {
        this.toolbar = toolbar;
        groupButton = toolbar.findViewById(R.id.btn_sel_group);
        ungroupButton = toolbar.findViewById(R.id.btn_sel_ungroup);
        deleteButton = toolbar.findViewById(R.id.btn_sel_delete);
        lockButton = toolbar.findViewById(R.id.btn_sel_lock);
        visibilityButton = toolbar.findViewById(R.id.btn_sel_visibility);
        toolbar.findViewById(R.id.btn_sel_all).setOnClickListener(v -> listener.onSelectAll());
        groupButton.setOnClickListener(v -> listener.onGroup());
        ungroupButton.setOnClickListener(v -> listener.onUngroup());
        deleteButton.setOnClickListener(v -> listener.onDelete());
        lockButton.setOnClickListener(v -> listener.onToggleLock());
        visibilityButton.setOnClickListener(v -> listener.onVisibility());
    }

    /**
     * Shows the toolbar for the current selection: group needs 2+
     * siblings, ungroup needs exactly one container selected, lock
     * shows Unlock when everything selected is already locked.
     */
    public void show(int selectionCount, boolean canGroup, boolean canUngroup,
                     boolean allLocked) {
        toolbar.setVisibility(View.VISIBLE);
        groupButton.setEnabled(canGroup);
        ungroupButton.setEnabled(canUngroup);
        deleteButton.setEnabled(selectionCount > 0);
        lockButton.setEnabled(selectionCount > 0);
        lockButton.setText(allLocked ? R.string.sel_unlock : R.string.sel_lock);
        visibilityButton.setEnabled(selectionCount > 0);
    }

    public void hide() {
        toolbar.setVisibility(View.GONE);
    }
}
