package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import com.nobi.appbuilder.model.ComponentModel;

/**
 * Selection logic: tap-to-select, additive selection, handle hit-testing
 * for the eight resize handles of a single selection. The selection
 * itself lives in CanvasState.
 */
public class SelectionController {

    /** Handle order: TL, TC, TR, ML, MR, BL, BC, BR. */
    public static final int HANDLE_TOP_LEFT = 0;
    public static final int HANDLE_TOP_CENTER = 1;
    public static final int HANDLE_TOP_RIGHT = 2;
    public static final int HANDLE_MIDDLE_LEFT = 3;
    public static final int HANDLE_MIDDLE_RIGHT = 4;
    public static final int HANDLE_BOTTOM_LEFT = 5;
    public static final int HANDLE_BOTTOM_CENTER = 6;
    public static final int HANDLE_BOTTOM_RIGHT = 7;
    public static final int NO_HANDLE = -1;

    private final CanvasState state;
    private final CanvasViewport viewport;

    public SelectionController(CanvasState state, CanvasViewport viewport) {
        this.state = state;
        this.viewport = viewport;
    }

    /**
     * Selects the component at a design point. Additive toggles the hit
     * component; otherwise a miss clears the selection. Returns the hit
     * component, or null.
     */
    public ComponentModel selectAt(float designX, float designY, boolean additive) {
        ComponentModel root = state.getRoot();
        if (root == null) {
            return null;
        }
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        ComponentModel hit = ComponentHitTester.hitTest(root, designX, designY, screenBounds);
        if (hit == null) {
            if (!additive) {
                state.clearSelection();
            }
            return null;
        }
        if (hit == root) {
            if (!additive) {
                state.clearSelection();
            }
            return hit;
        }
        if (additive) {
            state.toggleSelection(hit.getId());
        } else {
            state.select(hit.getId());
        }
        return hit;
    }

    /** Which resize handle (if any) sits under a screen point. */
    public int getHandleAt(float screenX, float screenY) {
        ComponentModel selected = state.getSingleSelection();
        if (selected == null) {
            return NO_HANDLE;
        }
        RectF screen = getSingleSelectionScreenRect();
        if (screen == null) {
            return NO_HANDLE;
        }
        float density = viewport.getTransform().getDensity();
        float radius = Math.max(16 * density, 10 * density);
        float[] xs = {screen.left, screen.centerX(), screen.right};
        float[] ys = {screen.top, screen.centerY(), screen.bottom};
        int handle = 0;
        for (float y : ys) {
            for (float x : xs) {
                float dx = screenX - x;
                float dy = screenY - y;
                if (dx * dx + dy * dy <= radius * radius) {
                    return handle;
                }
                handle++;
            }
        }
        return NO_HANDLE;
    }

    /** Screen bounds of the single selection, or null. */
    public RectF getSingleSelectionScreenRect() {
        ComponentModel selected = state.getSingleSelection();
        if (selected == null) {
            return null;
        }
        return getModelScreenRect(selected);
    }

    /** Screen bounds of one model, resolved through its parent chain. */
    public RectF getModelScreenRect(ComponentModel model) {
        ComponentModel root = state.getRoot();
        if (root == null || model == null) {
            return null;
        }
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        RectF design = ComponentHitTester.resolveBoundsFor(
                root, model.getId(), screenBounds);
        if (design == null) {
            return null;
        }
        RectF screen = new RectF();
        viewport.getTransform().designToScreenRect(design, screen);
        return screen;
    }

    public void clearSelection() {
        state.clearSelection();
    }
}
