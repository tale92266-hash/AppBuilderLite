package com.nobi.appbuilder.ui.editor;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

/**
 * The collapsible hierarchy panel at the bottom of the editor: a header
 * row (title, selected-component info, chevron) and the tree list.
 * Collapsed by default so the canvas gets the screen.
 */
public class EditorPanelController {

    private final RecyclerView list;
    private final ImageView chevron;
    private final TextView selectedInfo;
    private boolean expanded;

    public EditorPanelController(View header, RecyclerView list,
                                 ImageView chevron, TextView selectedInfo) {
        this.list = list;
        this.chevron = chevron;
        this.selectedInfo = selectedInfo;
        header.setOnClickListener(v -> toggle());
        setExpanded(false);
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
        chevron.setRotation(expanded ? 180 : 0);
    }

    public void setSelectedInfo(String info) {
        selectedInfo.setText(info);
    }
}
