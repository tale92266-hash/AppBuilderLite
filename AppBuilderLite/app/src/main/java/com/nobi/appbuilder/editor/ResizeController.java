package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;

/**
 * Eight-handle resizing. The handle decides which edges move; the
 * opposite edges stay put. Sizes snap to the grid, honour a minimum,
 * stay inside the parent, and ImageView corner handles keep the aspect
 * ratio. Resizing a MATCH_PARENT side converts it to explicit dp.
 */
public class ResizeController {

    private final CanvasState state;
    private boolean active;
    private ComponentModel target;
    private int handle;
    private final RectF startBounds = new RectF();
    private float startDesignX;
    private float startDesignY;
    private float startAspect;
    /** Phase 5: the target's own x/y/w/h when the resize started. */
    private final int[] startModel = new int[4];

    public ResizeController(CanvasState state) {
        this.state = state;
    }

    public boolean isActive() {
        return active;
    }

    public int getActiveHandle() {
        return handle;
    }

    /** Phase 5: target of the in-progress resize, or null. */
    public ComponentModel getTarget() {
        return target;
    }

    /** Phase 5: bounds the target had when the resize started (copy). */
    public RectF getStartBounds() {
        return new RectF(startBounds);
    }

    /** Phase 5: the target's x/y/w/h when the resize started (copy). */
    public int[] getStartModelBounds() {
        return new int[]{startModel[0], startModel[1], startModel[2], startModel[3]};
    }

    public void beginResize(ComponentModel target, int handle,
                            float designX, float designY) {
        ComponentModel root = state.getRoot();
        if (root == null || target == null || target.isLocked()) {
            return;
        }
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        RectF bounds = ComponentHitTester.resolveBoundsFor(
                root, target.getId(), screenBounds);
        if (bounds == null) {
            return;
        }
        this.target = target;
        this.handle = handle;
        startBounds.set(bounds);
        startModel[0] = target.getX();
        startModel[1] = target.getY();
        startModel[2] = target.getWidth();
        startModel[3] = target.getHeight();
        startDesignX = designX;
        startDesignY = designY;
        startAspect = bounds.width() > 0 ? bounds.width() / Math.max(1, bounds.height()) : 1;
        active = true;
    }

    public void updateResize(float designX, float designY) {
        if (!active || target == null) {
            return;
        }
        float dx = designX - startDesignX;
        float dy = designY - startDesignY;
        float left = startBounds.left;
        float top = startBounds.top;
        float right = startBounds.right;
        float bottom = startBounds.bottom;
        switch (handle) {
            case SelectionController.HANDLE_TOP_LEFT:
                left += dx; top += dy; break;
            case SelectionController.HANDLE_TOP_CENTER:
                top += dy; break;
            case SelectionController.HANDLE_TOP_RIGHT:
                top += dy; right += dx; break;
            case SelectionController.HANDLE_MIDDLE_LEFT:
                left += dx; break;
            case SelectionController.HANDLE_MIDDLE_RIGHT:
                right += dx; break;
            case SelectionController.HANDLE_BOTTOM_LEFT:
                left += dx; bottom += dy; break;
            case SelectionController.HANDLE_BOTTOM_CENTER:
                bottom += dy; break;
            case SelectionController.HANDLE_BOTTOM_RIGHT:
                right += dx; bottom += dy; break;
            default:
                return;
        }
        ComponentDefinition definition = ComponentRegistry.get(target.getType());
        float minW = definition != null && definition.getMinWidthDp() > 0
                ? definition.getMinWidthDp() : 16;
        float minH = definition != null && definition.getMinHeightDp() > 0
                ? definition.getMinHeightDp() : 16;
        boolean corner = handle == SelectionController.HANDLE_TOP_LEFT
                || handle == SelectionController.HANDLE_TOP_RIGHT
                || handle == SelectionController.HANDLE_BOTTOM_LEFT
                || handle == SelectionController.HANDLE_BOTTOM_RIGHT;
        boolean lockAspect = "ImageView".equals(target.getType()) && corner;
        float w = right - left;
        float h = bottom - top;
        if (lockAspect) {
            if (Math.abs(dx) >= Math.abs(dy)) {
                h = w / startAspect;
                if (handle == SelectionController.HANDLE_TOP_LEFT
                        || handle == SelectionController.HANDLE_TOP_RIGHT) {
                    top = bottom - h;
                }
            } else {
                w = h * startAspect;
                if (handle == SelectionController.HANDLE_TOP_LEFT
                        || handle == SelectionController.HANDLE_BOTTOM_LEFT) {
                    left = right - w;
                }
            }
        }
        w = SnapEngine.snapSize(w, minW, state.isSnapEnabled());
        h = SnapEngine.snapSize(h, minH, state.isSnapEnabled());
        // Keep the fixed edge fixed after snapping.
        switch (handle) {
            case SelectionController.HANDLE_TOP_LEFT:
                left = right - w; top = bottom - h; break;
            case SelectionController.HANDLE_TOP_CENTER:
                top = bottom - h; break;
            case SelectionController.HANDLE_TOP_RIGHT:
                top = bottom - h; break;
            case SelectionController.HANDLE_MIDDLE_LEFT:
                left = right - w; break;
            case SelectionController.HANDLE_BOTTOM_LEFT:
                left = right - w; break;
            default:
                break;
        }
        RectF parentBounds = parentBoundsOf(target);
        if (parentBounds != null) {
            float newLeft = left;
            float newTop = top;
            float newRight = left + w;
            float newBottom = top + h;
            if (newLeft < parentBounds.left) {
                newLeft = parentBounds.left;
                w = newRight - newLeft;
            }
            if (newTop < parentBounds.top) {
                newTop = parentBounds.top;
                h = newBottom - newTop;
            }
            if (newRight > parentBounds.right) {
                w = parentBounds.right - newLeft;
            }
            if (newBottom > parentBounds.bottom) {
                h = parentBounds.bottom - newTop;
            }
            left = newLeft;
            top = newTop;
            w = Math.max(minW, w);
            h = Math.max(minH, h);
        }
        target.setX(Math.round(left));
        target.setY(Math.round(top));
        target.setWidth(Math.round(w));
        target.setHeight(Math.round(h));
        LayoutModel layout = target.getLayout();
        layout.setWidth(LayoutModel.dimensionToString(target.getWidth()));
        layout.setHeight(LayoutModel.dimensionToString(target.getHeight()));
    }

    public void endResize() {
        active = false;
        target = null;
    }

    private RectF parentBoundsOf(ComponentModel model) {
        ComponentModel root = state.getRoot();
        ComponentModel parent = ComponentHitTester.findParent(root, model.getId());
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        if (parent == null) {
            return screenBounds;
        }
        RectF bounds = ComponentHitTester.resolveBoundsFor(root, parent.getId(), screenBounds);
        return bounds == null ? screenBounds : bounds;
    }
}
