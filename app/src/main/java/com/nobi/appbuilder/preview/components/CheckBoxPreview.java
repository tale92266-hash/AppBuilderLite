package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.CheckBox;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.CheckBox: label, size, colour, checked. */
public final class CheckBoxPreview {

    private CheckBoxPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             CheckBox view, PreviewContext previewContext) {
        PreviewStyleResolver.applyText(view, model,
                previewContext.getThemeManager());
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
        view.setChecked(PreviewStyleResolver.parseBoolean(model, "checked", false));
    }
}
