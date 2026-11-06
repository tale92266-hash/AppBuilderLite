package com.nobi.appbuilder.preview.factory;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.components.ButtonPreview;
import com.nobi.appbuilder.preview.components.CheckBoxPreview;
import com.nobi.appbuilder.preview.components.ConstraintLayoutPreview;
import com.nobi.appbuilder.preview.components.EditTextPreview;
import com.nobi.appbuilder.preview.components.FrameLayoutPreview;
import com.nobi.appbuilder.preview.components.ImageViewPreview;
import com.nobi.appbuilder.preview.components.LinearLayoutPreview;
import com.nobi.appbuilder.preview.components.MaterialCardViewPreview;
import com.nobi.appbuilder.preview.components.ProgressBarPreview;
import com.nobi.appbuilder.preview.components.ScrollViewPreview;
import com.nobi.appbuilder.preview.components.SwitchPreview;
import com.nobi.appbuilder.preview.components.TextViewPreview;
import com.nobi.appbuilder.utils.DimensionUtils;

/**
 * Builds one component's real Android View, recursively. Each
 * component is created, given its stable view id, styled by its
 * *Preview class, filled with its children (containers only) and
 * wired to its events. One bad component is swapped for a visible
 * error placeholder instead of killing the whole preview.
 */
public class PreviewComponentFactory {

    private final PreviewViewFactory viewFactory = new PreviewViewFactory();
    private final PreviewLayoutFactory layoutFactory = new PreviewLayoutFactory();

    public View create(Context context, ComponentModel model, PreviewContext ctx) {
        if (model == null) {
            return errorView(context, "?", "missing component");
        }
        String type = model.getType();
        if (type == null || !ComponentRegistry.contains(type)) {
            return errorView(context, model.getId(),
                    "unknown component type '" + type + "'");
        }
        View view;
        try {
            view = viewFactory.create(context, model);
            if (view == null) {
                return errorView(context, model.getId(),
                        "no preview view for type '" + type + "'");
            }
            view.setId(ctx.assignViewId(model.getId()));
            view.setTag(model.getId());
            ctx.registerView(model.getId(), view);
            applyComponent(context, model, view, ctx);
            if (view instanceof ViewGroup
                    && PreviewViewFactory.isContainer(type)) {
                ViewGroup group = (ViewGroup) view;
                int limit = "ScrollView".equals(type) ? 1
                        : model.getChildren().size();
                for (int i = 0; i < limit; i++) {
                    ComponentModel child = model.getChildren().get(i);
                    View childView = create(context, child, ctx);
                    ViewGroup.LayoutParams params = layoutFactory.create(
                            child, group, ctx);
                    group.addView(childView, params);
                }
            }
            com.nobi.appbuilder.preview.theme.PreviewStyleResolver.applyCommon(
                    view, model);
            if (ctx.getDispatcher() != null) {
                ctx.getDispatcher().attach(view, model, ctx);
            }
            return view;
        } catch (Exception e) {
            return errorView(context, model.getId(),
                    e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private void applyComponent(Context context, ComponentModel model, View view,
                                PreviewContext ctx) {
        switch (model.getType()) {
            case "TextView":
                TextViewPreview.apply(context, model, (TextView) view, ctx);
                break;
            case "Button":
                ButtonPreview.apply(context, model,
                        (com.google.android.material.button.MaterialButton) view, ctx);
                break;
            case "ImageView":
                ImageViewPreview.apply(context, model,
                        (android.widget.ImageView) view, ctx);
                break;
            case "EditText":
                EditTextPreview.apply(context, model,
                        (android.widget.EditText) view, ctx);
                break;
            case "LinearLayout":
                LinearLayoutPreview.apply(context, model,
                        (android.widget.LinearLayout) view, ctx);
                break;
            case "FrameLayout":
                FrameLayoutPreview.apply(context, model,
                        (android.widget.FrameLayout) view, ctx);
                break;
            case "ConstraintLayout":
                ConstraintLayoutPreview.apply(context, model,
                        (androidx.constraintlayout.widget.ConstraintLayout) view, ctx);
                break;
            case "ScrollView":
                ScrollViewPreview.apply(context, model,
                        (android.widget.ScrollView) view, ctx);
                break;
            case "CheckBox":
                CheckBoxPreview.apply(context, model,
                        (android.widget.CheckBox) view, ctx);
                break;
            case "Switch":
                SwitchPreview.apply(context, model,
                        (android.widget.Switch) view, ctx);
                break;
            case "ProgressBar":
                ProgressBarPreview.apply(context, model,
                        (android.widget.ProgressBar) view, ctx);
                break;
            case "MaterialCardView":
                MaterialCardViewPreview.apply(context, model,
                        (com.google.android.material.card.MaterialCardView) view, ctx);
                break;
            default:
                break;
        }
    }

    /** Red-tinted placeholder naming the component and the problem. */
    public static View errorView(Context context, String componentId, String message) {
        TextView error = new TextView(context);
        error.setText("[!] " + componentId + ": " + message);
        error.setTextColor(Color.parseColor("#B3261E"));
        error.setBackgroundColor(Color.parseColor("#FDECEA"));
        int pad = DimensionUtils.dpToPx(context, 8);
        error.setPadding(pad, pad, pad, pad);
        return error;
    }
}
