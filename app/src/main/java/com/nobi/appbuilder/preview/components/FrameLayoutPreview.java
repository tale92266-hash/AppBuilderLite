package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.FrameLayout;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.FrameLayout: background only; children are
 * positioned by their own layout gravity (see PreviewLayoutFactory). */
public final class FrameLayoutPreview {

    private FrameLayoutPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             FrameLayout view, PreviewContext previewContext) {
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
