package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.ImageView;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/**
 * ImageView: shows an image. In Phase 2 the "src" property holds an asset
 * path; the canvas draws a placeholder glyph and the preview shows a
 * tinted box until assets land in the editor (Phase 3+).
 */
public final class ImageViewComponent {

    public static final String TYPE = "ImageView";

    private ImageViewComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "ImageView", ComponentCategory.BASIC)
                .defaultSize(120, 120)
                .defaultProperty("scaleType", "centerCrop")
                .defaultProperty("backgroundColor", "#E8EEF7")
                .minSize(24, 24)
                .canvasRenderer(ImageViewComponent::drawOnCanvas)
                .previewBuilder(ImageViewComponent::buildPreviewView)
                .xmlProvider(ImageViewComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.parseColor("#E8EEF7")));
        canvas.drawRect(bounds, paint);
        paint.setColor(Color.parseColor("#9AA7BD"));
        paint.setStrokeWidth(2 * density);
        canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
        canvas.drawLine(bounds.right, bounds.top, bounds.left, bounds.bottom, paint);
        paint.setColor(Color.parseColor("#5B6B84"));
        paint.setTextSize(12 * density);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("Image", bounds.centerX(),
                bounds.centerY() + 4 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        ImageView view = new ImageView(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        String scaleType = model.getProperty("scaleType", "centerCrop");
        if ("fitCenter".equals(scaleType)) {
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        } else if ("centerInside".equals(scaleType)) {
            view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        } else if ("fitXY".equals(scaleType)) {
            view.setScaleType(ImageView.ScaleType.FIT_XY);
        } else {
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        view.setBackgroundColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.parseColor("#E8EEF7")));
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        String scaleType = model.getProperty("scaleType");
        if (scaleType != null) {
            attrs.put("android:scaleType", scaleType);
        }
        String src = model.getProperty("src");
        if (src != null && !src.isEmpty()) {
            attrs.put("android:src", src);
        }
        return attrs;
    }
}
