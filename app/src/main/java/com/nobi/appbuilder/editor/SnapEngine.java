package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.utils.CoordinateUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Grid snapping + alignment snapping for moves. Grid first, then
 * alignment guides (both within a small threshold), then clamp inside
 * the parent — in that order, so snapping can never throw a component
 * across the canvas.
 */
public final class SnapEngine {

    public static final float GRID_DP = 8f;
    public static final float SNAP_THRESHOLD_DP = 8f;

    /** Snapped position plus the guides the renderer should draw. */
    public static class SnapResult {
        public float x;
        public float y;
        public final List<AlignmentEngine.Guide> guides = new ArrayList<>();
    }

    private SnapEngine() {
    }

    /**
     * Snaps a proposed top-left for the moving model. Siblings are the
     * other components in the same parent (their bounds are resolved
     * against parentBounds); pass an empty list to snap to grid only.
     */
    public static SnapResult snapMove(ComponentModel moving, float proposedX, float proposedY,
                                      List<ComponentModel> siblings, RectF parentBounds,
                                      boolean snapEnabled) {
        SnapResult result = new SnapResult();
        float x = proposedX;
        float y = proposedY;
        RectF movingBounds = ComponentHitTester.resolveBounds(moving, parentBounds);
        float w = movingBounds.width();
        float h = movingBounds.height();
        if (snapEnabled) {
            x = CoordinateUtils.snapToGrid(x, GRID_DP);
            y = CoordinateUtils.snapToGrid(y, GRID_DP);
            List<RectF> siblingBounds = new ArrayList<>();
            for (ComponentModel sibling : siblings) {
                siblingBounds.add(ComponentHitTester.resolveBounds(sibling, parentBounds));
            }
            RectF atProposed = new RectF(x, y, x + w, y + h);
            AlignmentEngine.Alignment alignment = AlignmentEngine.findAlignment(
                    atProposed, siblingBounds, parentBounds, SNAP_THRESHOLD_DP);
            x += alignment.dx;
            y += alignment.dy;
            result.guides.addAll(alignment.guides);
        }
        if (parentBounds != null) {
            if (w >= parentBounds.width()) {
                x = parentBounds.left;
            } else {
                x = CoordinateUtils.clamp(x, parentBounds.left, parentBounds.right - w);
            }
            if (h >= parentBounds.height()) {
                y = parentBounds.top;
            } else {
                y = CoordinateUtils.clamp(y, parentBounds.top, parentBounds.bottom - h);
            }
        }
        result.x = x;
        result.y = y;
        return result;
    }

    /** Snaps a size to the grid, honouring a minimum. */
    public static float snapSize(float sizeDp, float minDp, boolean snapEnabled) {
        float size = Math.max(minDp, sizeDp);
        return snapEnabled ? CoordinateUtils.snapToGrid(size, GRID_DP) : size;
    }
}
