package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Moves components: drags the selection with grid + alignment snapping,
 * clamps to parent bounds, moves whole subtrees together, and drops new
 * components from the library into the deepest valid container.
 */
public class DragController {

    private final CanvasState state;
    private final CanvasViewport viewport;
    private boolean dragging;
    private List<ComponentModel> dragModels = new ArrayList<>();
    private final Map<String, float[]> startPositions = new HashMap<>();
    private float startDesignX;
    private float startDesignY;
    private List<AlignmentEngine.Guide> currentGuides = new ArrayList<>();
    private boolean moved;

    public DragController(CanvasState state, CanvasViewport viewport) {
        this.state = state;
        this.viewport = viewport;
    }

    public boolean isDragging() {
        return dragging;
    }

    public List<AlignmentEngine.Guide> getCurrentGuides() {
        return currentGuides;
    }

    /** Starts a drag; nested selections are reduced to their topmost nodes. */
    public void beginDrag(List<ComponentModel> models, float designX, float designY) {
        dragModels = topmostOnly(models);
        startPositions.clear();
        for (ComponentModel model : dragModels) {
            startPositions.put(model.getId(), new float[]{model.getX(), model.getY()});
        }
        startDesignX = designX;
        startDesignY = designY;
        currentGuides = new ArrayList<>();
        moved = false;
        dragging = true;
    }

    /** Applies the drag delta (snapped for the primary model). */
    public void updateDrag(float designX, float designY) {
        if (!dragging || dragModels.isEmpty()) {
            return;
        }
        float dx = designX - startDesignX;
        float dy = designY - startDesignY;
        if (dx != 0 || dy != 0) {
            moved = true;
        }
        ComponentModel primary = dragModels.get(0);
        float[] primaryStart = startPositions.get(primary.getId());
        RectF parentBounds = parentBoundsOf(primary);
        List<ComponentModel> siblings = siblingsOf(primary);
        SnapEngine.SnapResult snapped = SnapEngine.snapMove(primary,
                primaryStart[0] + dx, primaryStart[1] + dy,
                siblings, parentBounds, state.isSnapEnabled());
        currentGuides = snapped.guides;
        float appliedDx = snapped.x - primaryStart[0];
        float appliedDy = snapped.y - primaryStart[1];
        for (ComponentModel model : dragModels) {
            float[] start = startPositions.get(model.getId());
            moveSubtreeTo(model, start[0] + appliedDx, start[1] + appliedDy, parentBoundsOf(model));
        }
    }

    /** Ends the drag; true when anything actually moved. */
    public boolean endDrag() {
        dragging = false;
        currentGuides = new ArrayList<>();
        dragModels = new ArrayList<>();
        startPositions.clear();
        return moved;
    }

    /** Drops a new library component at a design point; returns it or null. */
    public ComponentModel dropNewComponent(String type, float designX, float designY) {
        if (!ComponentRegistry.contains(type)) {
            return null;
        }
        ComponentModel root = state.getRoot();
        if (root == null) {
            return null;
        }
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        ComponentModel parent = ComponentHitTester.findDeepestContainer(
                root, designX, designY, screenBounds);
        if (parent == null) {
            parent = root;
        }
        ComponentDefinition definition = ComponentRegistry.get(type);
        if (definition != null && !definition.supportsParent(parent.getType())) {
            return null;
        }
        if ("ScrollView".equals(parent.getType()) && !parent.getChildren().isEmpty()) {
            ComponentModel inner = parent.getChildren().get(0);
            if (ComponentHitTester.isContainer(inner)) {
                parent = inner;
            } else {
                return null;
            }
        }
        ComponentModel model = ComponentFactory.create(type);
        RectF parentDesign = ComponentHitTester.resolveBoundsFor(
                root, parent.getId(), screenBounds);
        if (parentDesign == null) {
            parentDesign = screenBounds;
        }
        float w = model.getWidth();
        float h = model.getHeight();
        if (w == ComponentModel.MATCH_PARENT) {
            w = ComponentHitTester.isContainer(model)
                    ? parentDesign.width() - 16 : 200;
        } else if (w == ComponentModel.WRAP_CONTENT) {
            w = ComponentHitTester.resolveBounds(model, parentDesign).width();
        }
        if (h == ComponentModel.MATCH_PARENT) {
            h = ComponentHitTester.isContainer(model)
                    ? Math.min(160, parentDesign.height() - 16) : 48;
        } else if (h == ComponentModel.WRAP_CONTENT) {
            h = ComponentHitTester.resolveBounds(model, parentDesign).height();
        }
        model.setWidth(Math.round(w));
        model.setHeight(Math.round(h));
        float x = designX - w / 2f;
        float y = designY - h / 2f;
        if (state.isSnapEnabled()) {
            x = com.nobi.appbuilder.utils.CoordinateUtils.snapToGrid(x, SnapEngine.GRID_DP);
            y = com.nobi.appbuilder.utils.CoordinateUtils.snapToGrid(y, SnapEngine.GRID_DP);
        }
        x = com.nobi.appbuilder.utils.CoordinateUtils.clamp(
                x, parentDesign.left, Math.max(parentDesign.left, parentDesign.right - w));
        y = com.nobi.appbuilder.utils.CoordinateUtils.clamp(
                y, parentDesign.top, Math.max(parentDesign.top, parentDesign.bottom - h));
        model.setX(Math.round(x));
        model.setY(Math.round(y));
        parent.addChild(model);
        return model;
    }

    private List<ComponentModel> topmostOnly(List<ComponentModel> models) {
        List<ComponentModel> out = new ArrayList<>();
        for (ComponentModel model : models) {
            boolean nested = false;
            for (ComponentModel other : models) {
                if (other != model
                        && ComponentHitTester.isDescendantOf(other, model)) {
                    nested = true;
                    break;
                }
            }
            if (!nested) {
                out.add(model);
            }
        }
        return out;
    }

    private void moveSubtreeTo(ComponentModel model, float newX, float newY,
                               RectF parentBounds) {
        float dx = newX - model.getX();
        float dy = newY - model.getY();
        moveSubtreeBy(model, dx, dy);
        if (parentBounds != null) {
            RectF bounds = ComponentHitTester.resolveBounds(model, parentBounds);
            float fixX = 0;
            float fixY = 0;
            if (bounds.left < parentBounds.left) {
                fixX = parentBounds.left - bounds.left;
            } else if (bounds.right > parentBounds.right) {
                fixX = parentBounds.right - bounds.right;
            }
            if (bounds.top < parentBounds.top) {
                fixY = parentBounds.top - bounds.top;
            } else if (bounds.bottom > parentBounds.bottom) {
                fixY = parentBounds.bottom - bounds.bottom;
            }
            if (fixX != 0 || fixY != 0) {
                moveSubtreeBy(model, fixX, fixY);
            }
        }
    }

    private void moveSubtreeBy(ComponentModel model, float dx, float dy) {
        model.setX(Math.round(model.getX() + dx));
        model.setY(Math.round(model.getY() + dy));
        for (ComponentModel child : model.getChildren()) {
            moveSubtreeBy(child, dx, dy);
        }
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

    private List<ComponentModel> siblingsOf(ComponentModel model) {
        ComponentModel root = state.getRoot();
        ComponentModel parent = ComponentHitTester.findParent(root, model.getId());
        List<ComponentModel> siblings = new ArrayList<>();
        List<ComponentModel> source = parent == null
                ? root.getChildren() : parent.getChildren();
        Set<String> draggedIds = new HashSet<>();
        for (ComponentModel dragged : dragModels) {
            draggedIds.add(dragged.getId());
        }
        for (ComponentModel sibling : source) {
            if (!draggedIds.contains(sibling.getId())) {
                siblings.add(sibling);
            }
        }
        return siblings;
    }
}
