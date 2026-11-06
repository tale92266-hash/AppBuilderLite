package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.TextView;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.TextView: text, size, colour, gravity, lines. */
public final class TextViewPreview {

    private TextViewPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             TextView view, PreviewContext previewContext) {
        PreviewStyleResolver.applyText(view, model,
                previewContext.getThemeManager());
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
