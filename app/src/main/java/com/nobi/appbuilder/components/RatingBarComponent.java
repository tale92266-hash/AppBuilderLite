package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.RatingBar;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** RatingBar: star rating input/display. */
public final class RatingBarComponent {

    public static final String TYPE = "RatingBar";

    private RatingBarComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "RatingBar", ComponentCategory.INPUT)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("rating", "3")
                .defaultProperty("max", "5")
                .minSize(120, 32)
                .canvasRenderer(RatingBarComponent::drawOnCanvas)
                .previewBuilder(RatingBarComponent::buildPreviewView)
                .xmlProvider(RatingBarComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    private static float ratingOf(ComponentModel model) {
        try {
            return Float.parseFloat(model.getProperty("rating", "3"));
        } catch (NumberFormatException e) {
            return 3f;
        }
    }

    private static int maxOf(ComponentModel model) {
        try {
            return Integer.parseInt(model.getProperty("max", "5"));
        } catch (NumberFormatException e) {
            return 5;
        }
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        float rating = ratingOf(model);
        int max = Math.max(1, maxOf(model));
        float size = Math.min(20 * density,
                (bounds.width() - 8 * density) / max);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(size);
        paint.setColor(Color.parseColor("#F59E0B"));
        float x = bounds.left + 4 * density;
        float y = bounds.centerY() + size * 0.35f;
        for (int i = 0; i < max; i++) {
            canvas.drawText(i < rating ? "★" : "☆", x, y, paint);
            x += size + 2 * density;
        }
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        RatingBar view = new RatingBar(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setNumStars(Math.max(1, maxOf(model)));
        view.setRating(ratingOf(model));
        view.setStepSize(0.5f);
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:numStars", String.valueOf(maxOf(model)));
        attrs.put("android:rating", model.getProperty("rating", "3"));
        attrs.put("android:stepSize", "0.5");
        return attrs;
    }
}
