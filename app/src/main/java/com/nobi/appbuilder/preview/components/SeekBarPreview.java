package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.SeekBar;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;

/** Real android.widget.SeekBar. */
public final class SeekBarPreview {

    private SeekBarPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             SeekBar view, PreviewContext previewContext) {
        int max = 100;
        int progress = 50;
        try {
            max = Math.max(1, Integer.parseInt(model.getProperty("max", "100")));
        } catch (NumberFormatException ignored) {
        }
        try {
            progress = Integer.parseInt(model.getProperty("progress", "50"));
        } catch (NumberFormatException ignored) {
        }
        view.setMax(max);
        view.setProgress(progress);
    }
}
