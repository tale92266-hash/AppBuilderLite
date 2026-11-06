package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.widget.RatingBar;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;

/** Real android.widget.RatingBar. */
public final class RatingBarPreview {

    private RatingBarPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             RatingBar view, PreviewContext previewContext) {
        int max = 5;
        try {
            max = Math.max(1, Integer.parseInt(model.getProperty("max", "5")));
        } catch (NumberFormatException ignored) {
        }
        float rating = 3f;
        try {
            rating = Float.parseFloat(model.getProperty("rating", "3"));
        } catch (NumberFormatException ignored) {
        }
        view.setNumStars(max);
        view.setRating(rating);
        view.setStepSize(0.5f);
    }
}
