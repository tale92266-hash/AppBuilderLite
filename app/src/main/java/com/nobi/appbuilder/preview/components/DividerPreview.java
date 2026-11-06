package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.view.View;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** A plain View with the design background color. */
public final class DividerPreview {

    private DividerPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             View view, PreviewContext previewContext) {
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
