package com.nobi.appbuilder.preview.components;

import android.content.Context;

import com.google.android.material.button.MaterialButton;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewColorResolver;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real MaterialButton: text, size, colours, corner radius. */
public final class ButtonPreview {

    private ButtonPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             MaterialButton view, PreviewContext previewContext) {
        PreviewStyleResolver.applyText(view, model,
                previewContext.getThemeManager());
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            view.setBackgroundColor(PreviewColorResolver.resolveToken(bg,
                    previewContext.getThemeManager().getEffectiveTheme(),
                    previewContext.getThemeManager().getPrimaryColor()));
        }
        float radius = PreviewStyleResolver.parseFloat(model, "cornerRadius", 0f);
        if (radius > 0) {
            view.setCornerRadius(Math.round(
                    com.nobi.appbuilder.utils.DimensionUtils.dpToPx(context, radius)));
        }
    }
}
