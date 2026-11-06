package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.ProgressBar;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** ProgressBar: a horizontal determinate (or indeterminate) progress bar. */
public final class ProgressBarComponent {

    public static final String TYPE = "ProgressBar";

    private ProgressBarComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "ProgressBar",
                ComponentCategory.DISPLAY)
                .defaultSize(ComponentModel.MATCH_PARENT, 24)
                .defaultProperty("max", "100")
                .defaultProperty("progress", "50")
                .defaultProperty("indeterminate", "false")
                .minSize(48, 8)
                .canvasRenderer(ProgressBarComponent::drawOnCanvas)
                .previewBuilder(ProgressBarComponent::buildPreviewView)
                .xmlProvider(ProgressBarComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    static int intProperty(ComponentModel model, String key, int fallback) {
        try {
            return Integer.parseInt(model.getProperty(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float barH = 8 * density;
        float top = bounds.centerY() - barH / 2;
        RectF track = new RectF(bounds.left, top, bounds.right, top + barH);
        paint.setColor(Color.parseColor("#E5E7EB"));
        canvas.drawRoundRect(track, barH / 2, barH / 2, paint);
        if (!"true".equals(model.getProperty("indeterminate"))) {
            int max = Math.max(1, intProperty(model, "max", 100));
            int progress = Math.max(0, Math.min(max, intProperty(model, "progress", 50)));
            float fillW = track.width() * progress / (float) max;
            paint.setColor(Color.parseColor("#0B4499"));
            canvas.drawRoundRect(new RectF(track.left, top,
                    track.left + fillW, top + barH), barH / 2, barH / 2, paint);
        } else {
            paint.setColor(Color.parseColor("#0B4499"));
            float w = track.width() * 0.4f;
            canvas.drawRoundRect(new RectF(track.left + track.width() * 0.3f, top,
                    track.left + track.width() * 0.3f + w, top + barH),
                    barH / 2, barH / 2, paint);
        }
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        ProgressBar view;
        if ("true".equals(model.getProperty("indeterminate"))) {
            view = new ProgressBar(context);
            view.setIndeterminate(true);
        } else {
            view = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
            view.setIndeterminate(false);
            view.setMax(Math.max(1, intProperty(model, "max", 100)));
            view.setProgress(intProperty(model, "progress", 50));
        }
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:max", model.getProperty("max", "100"));
        attrs.put("android:progress", model.getProperty("progress", "50"));
        attrs.put("android:indeterminate", model.getProperty("indeterminate", "false"));
        return attrs;
    }
}
