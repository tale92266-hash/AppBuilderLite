package com.nobi.appbuilder.preview.components;

import android.content.Context;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real androidx ConstraintLayout: background only; children are
 * constrained by their own LayoutParams (see PreviewLayoutFactory). */
public final class ConstraintLayoutPreview {

    private ConstraintLayoutPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             ConstraintLayout view, PreviewContext previewContext) {
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
