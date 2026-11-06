package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.LinkedHashMap;
import java.util.Map;

/** TextView: a read-only text label. */
public final class TextViewComponent {

    public static final String TYPE = "TextView";

    private TextViewComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "TextView", ComponentCategory.BASIC)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "TextView")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .defaultProperty("gravity", "left")
                .minSize(16, 16)
                .canvasRenderer(TextViewComponent::drawOnCanvas)
                .previewBuilder(TextViewComponent::buildPreviewView)
                .xmlProvider(TextViewComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            paint.setColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
            canvas.drawRect(bounds, paint);
        }
        float textSizePx = ComponentDefinition.parseTextSizeSp(model, 16) * density;
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        paint.setTextSize(textSizePx);
        String gravity = model.getProperty("gravity", "left");
        float x = bounds.left + 4 * density;
        if ("center".equals(gravity)) {
            paint.setTextAlign(Paint.Align.CENTER);
            x = bounds.centerX();
        } else if ("right".equals(gravity) || "end".equals(gravity)) {
            paint.setTextAlign(Paint.Align.RIGHT);
            x = bounds.right - 4 * density;
        } else {
            paint.setTextAlign(Paint.Align.LEFT);
        }
        float y = bounds.centerY() + textSizePx * 0.35f;
        canvas.drawText(model.getProperty("text", "TextView"), x, y, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        TextView view = new TextView(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setText(model.getProperty("text", "TextView"));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                ComponentDefinition.parseTextSizeSp(model, 16));
        view.setTextColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        view.setGravity(ComponentDefinition.parseGravity(model.getProperty("gravity")));
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            view.setBackgroundColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
        }
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:text", model.getProperty("text", ""));
        attrs.put("android:textSize",
                ((int) ComponentDefinition.parseTextSizeSp(model, 16)) + "sp");
        String textColor = model.getProperty("textColor");
        if (textColor != null) {
            attrs.put("android:textColor", textColor);
        }
        String gravity = model.getProperty("gravity");
        if (gravity != null && !gravity.isEmpty()) {
            attrs.put("android:gravity", gravity);
        }
        return attrs;
    }
}
