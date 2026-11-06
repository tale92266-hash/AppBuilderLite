package com.nobi.appbuilder.selection;

import android.graphics.RectF;

import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Multi-selection on top of CanvasState's selection set: tap, additive
 * select, select-all, deselect-all, and marquee (rectangle) selection.
 * Marquee selects topmost components only: once a container is
 * selected, its descendants are not added, so a parent is never
 * selected together with its own children by accident.
 */
public class MultiSelectionManager {

    private final CanvasState state;

    public MultiSelectionManager(CanvasState state) {
        this.state = state;
    }

    public void select(ComponentModel model) {
        if (model != null) {
            state.select(model.getId());
        }
    }

    public void add(ComponentModel model) {
        if (model != null) {
            state.addToSelection(model.getId());
        }
    }

    public void remove(ComponentModel model) {
        if (model != null) {
            state.removeFromSelection(model.getId());
        }
    }

    public void toggle(ComponentModel model) {
        if (model != null) {
            state.toggleSelection(model.getId());
        }
    }

    public void selectAll(ComponentModel root) {
        if (root == null) {
            return;
        }
        state.clearSelection();
        collectAll(root, true);
    }

    private void collectAll(ComponentModel model, boolean isRoot) {
        if (!isRoot) {
            state.addToSelection(model.getId());
        }
        for (ComponentModel child : model.getChildren()) {
            collectAll(child, false);
        }
    }

    public void deselectAll() {
        state.clearSelection();
    }

    public List<ComponentModel> getSelection() {
        return state.getSelectedModels();
    }

    public boolean hasSelection() {
        return state.hasSelection();
    }

    public boolean isMulti() {
        return state.getSelectedIds().size() > 1;
    }

    public SelectionBounds getBounds(ComponentModel root) {
        return SelectionBounds.compute(state.getSelectedModels(), root);
    }

    /**
     * Selects every component whose bounds intersect the design-space
     * rectangle, topmost first; descendants of a selected component
     * are skipped. Replaces the current selection.
     */
    public List<ComponentModel> selectInRect(ComponentModel root, RectF designRect) {
        List<ComponentModel> found = new ArrayList<>();
        if (root == null) {
            return found;
        }
        RectF screenBounds = new RectF(0, 0, 360, 640);
        collectIntersecting(root, designRect, screenBounds, found, true);
        state.clearSelection();
        for (ComponentModel model : found) {
            state.addToSelection(model.getId());
        }
        return found;
    }

    private void collectIntersecting(ComponentModel model, RectF designRect,
                                     RectF parentBounds, List<ComponentModel> found,
                                     boolean isRoot) {
        RectF bounds = ComponentHitTester.resolveBounds(model, parentBounds);
        if (bounds == null || !RectF.intersects(bounds, designRect)) {
            return;
        }
        if (!isRoot) {
            found.add(model);
            return;
        }
        for (ComponentModel child : model.getChildren()) {
            collectIntersecting(child, designRect, bounds, found, false);
        }
    }
}
