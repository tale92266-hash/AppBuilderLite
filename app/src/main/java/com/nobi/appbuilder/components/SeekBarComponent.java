package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.SeekBar;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** SeekBar: a draggable slider. */
public final class SeekBarComponent {

    public static final String TYPE = "SeekBar";

    private SeekBarComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "SeekBar", ComponentCategory.INPUT)
                .defaultSize(200, ComponentModel.WRAP_CONTENT)
                .defaultProperty("max", "100")
                .defaultProperty("progress", "50")
                .minSize(120, 24)
                .canvasRenderer(SeekBarComponent::drawOnCanvas)
                .previewBuilder(SeekBarComponent::buildPreviewView)
                .xmlProvider(SeekBarComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    private static int intOf(ComponentModel model, String key, int fallback) {
        try {
            return Integer.parseInt(model.getProperty(key,
                    String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        float cy = bounds.centerY();
        float left = bounds.left + 8 * density;
        float right = bounds.right - 8 * density;
        int max = Math.max(1, intOf(model, "max", 100));
        float fraction = Math.max(0f, Math.min(1f,
                intOf(model, "progress", 50) / (float) max));
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(4 * density);
        paint.setColor(Color.parseColor("#D1D5DB"));
        canvas.drawLine(left, cy, right, cy, paint);
        paint.setColor(Color.parseColor("#0B4499"));
        canvas.drawLine(left, cy, left + (right - left) * fraction, cy, paint);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(left + (right - left) * fraction, cy,
                8 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        SeekBar view = new SeekBar(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setMax(Math.max(1, intOf(model, "max", 100)));
        view.setProgress(intOf(model, "progress", 50));
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:max", model.getProperty("max", "100"));
        attrs.put("android:progress", model.getProperty("progress", "50"));
        return attrs;
    }
}
