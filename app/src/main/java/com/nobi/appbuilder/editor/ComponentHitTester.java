package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Hit-testing and bounds resolution in design (dp) coordinates.
 * All component positions are absolute in screen design space; a
 * MATCH_PARENT dimension fills the parent's remaining space, and
 * WRAP_CONTENT is estimated from the component's own content.
 */
public final class ComponentHitTester {

    private ComponentHitTester() {
    }

    /** Bounds of a model inside its parent bounds (both in design dp). */
    public static void resolveBoundsInto(ComponentModel model, RectF parentBounds,
                                         RectF out) {
        float x = model.getX();
        float y = model.getY();
        float w = resolveDimension(model.getWidth(), model, parentBounds, true);
        float h = resolveDimension(model.getHeight(), model, parentBounds, false);
        out.set(x, y, x + Math.max(0, w), y + Math.max(0, h));
    }

    /** Bounds of a model inside its parent bounds (both in design dp). */
    public static RectF resolveBounds(ComponentModel model, RectF parentBounds) {
        float x = model.getX();
        float y = model.getY();
        float w = resolveDimension(model.getWidth(), model, parentBounds, true);
        float h = resolveDimension(model.getHeight(), model, parentBounds, false);
        return new RectF(x, y, x + Math.max(0, w), y + Math.max(0, h));
    }

    private static float resolveDimension(int size, ComponentModel model,
                                          RectF parentBounds, boolean isWidth) {
        if (size == ComponentModel.MATCH_PARENT) {
            float parentSize = isWidth ? parentBounds.width() : parentBounds.height();
            float start = isWidth ? model.getX() - parentBounds.left
                    : model.getY() - parentBounds.top;
            return Math.max(0, parentSize - start);
        }
        if (size == ComponentModel.WRAP_CONTENT) {
            return estimateWrapSize(model, parentBounds, isWidth);
        }
        return size;
    }

    private static float estimateWrapSize(ComponentModel model, RectF parentBounds,
                                          boolean isWidth) {
        String type = model.getType();
        float textSize = 16;
        try {
            textSize = Float.parseFloat(model.getProperty("textSize", "16"));
        } catch (NumberFormatException ignored) {
        }
        String text = model.getProperty("text", "");
        if (text.isEmpty()) {
            text = model.getProperty("hint", "");
        }
        if ("ImageView".equals(type)) {
            return isWidth ? 120 : 120;
        }
        if ("ProgressBar".equals(type)) {
            return isWidth ? 200 : 24;
        }
        if ("TextView".equals(type) || "Button".equals(type)
                || "CheckBox".equals(type) || "Switch".equals(type)
                || "EditText".equals(type)) {
            if (isWidth) {
                float charWidth = textSize * 0.55f;
                float extra = "CheckBox".equals(type) || "Switch".equals(type) ? 56 : 24;
                return Math.max(48, text.length() * charWidth + extra);
            }
            return textSize * 1.8f + 16;
        }
        // Containers: extent of children, or a sensible minimum.
        if (isWidth) {
            float extent = 0;
            for (ComponentModel child : model.getChildren()) {
                RectF childBounds = resolveBounds(child, parentBounds);
                extent = Math.max(extent, childBounds.right - model.getX());
            }
            return Math.max(120, extent);
        }
        float extent = 0;
        for (ComponentModel child : model.getChildren()) {
            RectF childBounds = resolveBounds(child, parentBounds);
            extent = Math.max(extent, childBounds.bottom - model.getY());
        }
        return Math.max(100, extent);
    }

    /**
     * Deepest, topmost component containing the design point. Children are
     * checked last-drawn-first so the visually-top component wins.
     */
    public static ComponentModel hitTest(ComponentModel root, float designX,
                                         float designY, RectF screenBounds) {
        if (root == null) {
            return null;
        }
        return hitTestRecursive(root, designX, designY, screenBounds);
    }

    private static ComponentModel hitTestRecursive(ComponentModel node, float designX,
                                                   float designY, RectF parentBounds) {
        RectF bounds = resolveBounds(node, parentBounds);
        List<ComponentModel> children = node.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            ComponentModel child = children.get(i);
            ComponentModel hit = hitTestRecursive(child, designX, designY, bounds);
            if (hit != null) {
                return hit;
            }
        }
        if (designX >= bounds.left && designX <= bounds.right
                && designY >= bounds.top && designY <= bounds.bottom) {
            return node;
        }
        return null;
    }

    /** Deepest container containing the point (drop target for new children). */
    public static ComponentModel findDeepestContainer(ComponentModel root, float designX,
                                                      float designY, RectF screenBounds) {
        if (root == null) {
            return null;
        }
        return findContainerRecursive(root, designX, designY, screenBounds);
    }

    private static ComponentModel findContainerRecursive(ComponentModel node, float designX,
                                                         float designY, RectF parentBounds) {
        RectF bounds = resolveBounds(node, parentBounds);
        if (designX < bounds.left || designX > bounds.right
                || designY < bounds.top || designY > bounds.bottom) {
            return null;
        }
        List<ComponentModel> children = node.getChildren();
        for (int i = children.size() - 1; i >= 0; i--) {
            ComponentModel child = children.get(i);
            if (!isContainer(child)) {
                continue;
            }
            ComponentModel found = findContainerRecursive(child, designX, designY, bounds);
            if (found != null) {
                return found;
            }
        }
        return isContainer(node) ? node : null;
    }

    public static boolean isContainer(ComponentModel model) {
        if (model == null) {
            return false;
        }
        return com.nobi.appbuilder.components.ComponentRegistry.contains(model.getType())
                && com.nobi.appbuilder.components.ComponentRegistry
                        .get(model.getType()).isContainer();
    }

    /** Parent of a node, or null for the root / not found. */
    public static ComponentModel findParent(ComponentModel root, String childId) {
        if (root == null || childId == null) {
            return null;
        }
        for (ComponentModel child : root.getChildren()) {
            if (childId.equals(child.getId())) {
                return root;
            }
            ComponentModel found = findParent(child, childId);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** Bounds of one node, resolved against its real parent chain. */
    public static RectF resolveBoundsFor(ComponentModel root, String targetId,
                                         RectF screenBounds) {
        if (root == null || targetId == null) {
            return null;
        }
        if (targetId.equals(root.getId())) {
            return resolveBounds(root, screenBounds);
        }
        return resolveBoundsForRecursive(root, targetId, screenBounds);
    }

    private static RectF resolveBoundsForRecursive(ComponentModel node, String targetId,
                                                   RectF parentBounds) {
        RectF bounds = resolveBounds(node, parentBounds);
        for (ComponentModel child : node.getChildren()) {
            if (targetId.equals(child.getId())) {
                return resolveBounds(child, bounds);
            }
            RectF found = resolveBoundsForRecursive(child, targetId, bounds);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /** All nodes in the tree except the given ids (snap candidates). */
    public static List<ComponentModel> collectAllExcept(ComponentModel root,
                                                        java.util.Set<String> excludeIds) {
        List<ComponentModel> out = new ArrayList<>();
        collectRecursive(root, excludeIds, out);
        return out;
    }

    private static void collectRecursive(ComponentModel node, java.util.Set<String> excludeIds,
                                         List<ComponentModel> out) {
        if (node == null) {
            return;
        }
        if (excludeIds == null || !excludeIds.contains(node.getId())) {
            out.add(node);
        }
        for (ComponentModel child : node.getChildren()) {
            collectRecursive(child, excludeIds, out);
        }
    }

    /** True when ancestor contains model anywhere below it. */
    public static boolean isDescendantOf(ComponentModel ancestor, ComponentModel model) {
        if (ancestor == null || model == null) {
            return false;
        }
        for (ComponentModel child : ancestor.getChildren()) {
            if (child.getId().equals(model.getId()) || isDescendantOf(child, model)) {
                return true;
            }
        }
        return false;
    }
}
