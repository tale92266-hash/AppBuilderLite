package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.ProgressBar;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;

/** Real android.widget.ProgressBar: indeterminate, max, progress. */
public final class ProgressBarPreview {

    private ProgressBarPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             ProgressBar view, PreviewContext previewContext) {
        boolean indeterminate = PreviewStyleResolver.parseBoolean(
                model, "indeterminate", false);
        view.setIndeterminate(indeterminate);
        if (!indeterminate) {
            int max = PreviewStyleResolver.parseInt(model, "max", 100);
            if (max <= 0) {
                max = 100;
            }
            view.setMax(max);
            int progress = PreviewStyleResolver.parseInt(model, "progress", 0);
            if (progress < 0) {
                progress = 0;
            }
            if (progress > max) {
                progress = max;
            }
            view.setProgress(progress);
        }
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
    }
}
