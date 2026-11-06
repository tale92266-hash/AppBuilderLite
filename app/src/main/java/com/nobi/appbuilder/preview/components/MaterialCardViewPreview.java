package com.nobi.appbuilder.preview.components;

import android.content.Context;

import com.google.android.material.card.MaterialCardView;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewColorResolver;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;
import com.nobi.appbuilder.utils.DimensionUtils;

/** Real MaterialCardView: background colour, corner radius, elevation. */
public final class MaterialCardViewPreview {

    private MaterialCardViewPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             MaterialCardView view, PreviewContext previewContext) {
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            view.setCardBackgroundColor(PreviewColorResolver.resolveToken(bg,
                    previewContext.getThemeManager().getEffectiveTheme(),
                    previewContext.getThemeManager().getSurfaceColor()));
        }
        float radius = PreviewStyleResolver.parseFloat(model, "cornerRadius", 0f);
        if (radius > 0) {
            view.setRadius(DimensionUtils.dpToPx(context, radius));
        }
        float elevation = PreviewStyleResolver.parseFloat(model, "elevation", 0f);
        if (elevation > 0) {
            view.setCardElevation(DimensionUtils.dpToPx(context, elevation));
        }
    }
}
