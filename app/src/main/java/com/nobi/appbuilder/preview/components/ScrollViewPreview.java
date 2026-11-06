package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.ScrollView;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.ScrollView: vertical scrolling, one child,
 * viewport filled so a short child still stretches full height. */
public final class ScrollViewPreview {

    private ScrollViewPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             ScrollView view, PreviewContext previewContext) {
        view.setFillViewport(true);
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
