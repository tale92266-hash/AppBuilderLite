package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.LinearLayout;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.LinearLayout: orientation + gravity. */
public final class LinearLayoutPreview {

    private LinearLayoutPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             LinearLayout view, PreviewContext previewContext) {
        String orientation = model.getProperty("orientation");
        view.setOrientation("horizontal".equalsIgnoreCase(orientation)
                ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        String gravity = model.getLayout() == null ? null
                : model.getLayout().getGravity();
        if (gravity != null) {
            view.setGravity(PreviewStyleResolver.gravityFor(gravity));
        }
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
