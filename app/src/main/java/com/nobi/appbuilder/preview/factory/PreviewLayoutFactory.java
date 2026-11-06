package com.nobi.appbuilder.preview.factory;

import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.utils.DimensionUtils;

/**
 * Turns a component's layout configuration into real LayoutParams
 * for its parent type: LinearLayout (weight, gravity), FrameLayout
 * (gravity), ConstraintLayout (constraints, bias) and ScrollView.
 * Dimensions come from the LayoutModel strings first
 * ("match_parent" / "wrap_content" / "120dp") and fall back to the
 * model's int fields. Unsupported constraints are left unset — never
 * silently replaced with arbitrary positioning.
 */
public class PreviewLayoutFactory {

    /** Width/height in px, or MATCH_PARENT / WRAP_CONTENT. */
    public static int resolveSize(android.content.Context context,
                                  String dimensionString, int modelValue) {
        if (dimensionString != null && !dimensionString.isEmpty()) {
            String value = dimensionString.trim();
            if (LayoutModel.MATCH_PARENT.equals(value)) {
                return ViewGroup.LayoutParams.MATCH_PARENT;
            }
            if (LayoutModel.WRAP_CONTENT.equals(value)) {
                return ViewGroup.LayoutParams.WRAP_CONTENT;
            }
            int dp = LayoutModel.parseDimension(value, Integer.MIN_VALUE);
            if (dp != Integer.MIN_VALUE) {
                return DimensionUtils.dpToPx(context, dp);
            }
        }
        if (modelValue == ComponentModel.MATCH_PARENT) {
            return ViewGroup.LayoutParams.MATCH_PARENT;
        }
        if (modelValue == ComponentModel.WRAP_CONTENT) {
            return ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        return DimensionUtils.dpToPx(context, modelValue);
    }

    public ViewGroup.LayoutParams create(ComponentModel model, ViewGroup parent,
                                         PreviewContext context) {
        android.content.Context androidContext = context.getAndroidContext();
        LayoutModel layout = model.getLayout();
        String widthString = layout == null ? null : layout.getWidth();
        String heightString = layout == null ? null : layout.getHeight();
        int width = resolveSize(androidContext, widthString, model.getWidth());
        int height = resolveSize(androidContext, heightString, model.getHeight());

        ViewGroup.LayoutParams params;
        if (parent instanceof LinearLayout) {
            float weight = layout == null ? 0f : layout.getWeight();
            LinearLayout.LayoutParams linear =
                    new LinearLayout.LayoutParams(width, height, weight);
            applyMargins(androidContext, linear, layout);
            String gravity = layout == null ? null : layout.getLayoutGravity();
            if (gravity != null) {
                linear.gravity = ComponentDefinition.parseGravity(gravity);
            }
            params = linear;
        } else if (parent instanceof ConstraintLayout) {
            ConstraintLayout.LayoutParams constraint =
                    new ConstraintLayout.LayoutParams(width, height);
            applyMargins(androidContext, constraint, layout);
            applyConstraints(model, constraint, context);
            params = constraint;
        } else if (parent instanceof ScrollView) {
            FrameLayout.LayoutParams frame =
                    new FrameLayout.LayoutParams(width, height);
            applyMargins(androidContext, frame, layout);
            params = frame;
        } else if (parent instanceof FrameLayout) {
            FrameLayout.LayoutParams frame =
                    new FrameLayout.LayoutParams(width, height);
            applyMargins(androidContext, frame, layout);
            String gravity = layout == null ? null : layout.getLayoutGravity();
            if (gravity != null) {
                frame.gravity = ComponentDefinition.parseGravity(gravity);
            }
            params = frame;
        } else if (parent != null) {
            ViewGroup.MarginLayoutParams margins =
                    new ViewGroup.MarginLayoutParams(width, height);
            applyMargins(androidContext, margins, layout);
            params = margins;
        } else {
            params = new ViewGroup.LayoutParams(width, height);
        }
        return params;
    }

    private void applyMargins(android.content.Context context,
                              ViewGroup.MarginLayoutParams params,
                              LayoutModel layout) {
        if (layout == null) {
            return;
        }
        params.leftMargin = DimensionUtils.dpToPx(context, layout.getMarginLeft());
        params.topMargin = DimensionUtils.dpToPx(context, layout.getMarginTop());
        params.rightMargin = DimensionUtils.dpToPx(context, layout.getMarginRight());
        params.bottomMargin = DimensionUtils.dpToPx(context, layout.getMarginBottom());
    }

    /**
     * Constraint targets: "parent" binds to the parent, "none" leaves
     * the side free, anything else must be a sibling component id
     * (already validated; an unknown id simply stays unbound).
     */
    private void applyConstraints(ComponentModel model,
                                  ConstraintLayout.LayoutParams params,
                                  PreviewContext context) {
        params.topToTop = resolveTarget(model, "constraintTop", context);
        params.bottomToBottom = resolveTarget(model, "constraintBottom", context);
        params.startToStart = resolveTarget(model, "constraintStart", context);
        params.endToEnd = resolveTarget(model, "constraintEnd", context);
        String biasH = model.getProperty("biasHorizontal");
        if (biasH != null) {
            try {
                params.horizontalBias = Float.parseFloat(biasH.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String biasV = model.getProperty("biasVertical");
        if (biasV != null) {
            try {
                params.verticalBias = Float.parseFloat(biasV.trim());
            } catch (NumberFormatException ignored) {
            }
        }
    }

    private int resolveTarget(ComponentModel model, String key,
                              PreviewContext context) {
        String target = model.getProperty(key);
        if (target == null || "none".equals(target)) {
            return ConstraintLayout.LayoutParams.UNSET;
        }
        if ("parent".equals(target)) {
            return ConstraintLayout.LayoutParams.PARENT_ID;
        }
        int viewId = context.getViewId(target);
        return viewId == ViewGroup.NO_ID
                ? ConstraintLayout.LayoutParams.UNSET : viewId;
    }
}
