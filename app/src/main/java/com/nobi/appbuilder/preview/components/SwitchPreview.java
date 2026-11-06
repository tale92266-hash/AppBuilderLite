package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.Switch;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.Switch: label, size, colour, checked. */
public final class SwitchPreview {

    private SwitchPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             Switch view, PreviewContext previewContext) {
        PreviewStyleResolver.applyText(view, model,
                previewContext.getThemeManager());
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
        view.setChecked(PreviewStyleResolver.parseBoolean(model, "checked", false));
    }
}
