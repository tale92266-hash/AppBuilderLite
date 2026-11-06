package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.RadioButton;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** RadioButton: a single-choice option with a label. */
public final class RadioButtonComponent {

    public static final String TYPE = "RadioButton";

    private RadioButtonComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "RadioButton", ComponentCategory.INPUT)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "Option")
                .defaultProperty("checked", "false")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .minSize(48, 24)
                .canvasRenderer(RadioButtonComponent::drawOnCanvas)
                .previewBuilder(RadioButtonComponent::buildPreviewView)
                .xmlProvider(RadioButtonComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        float radius = 9 * density;
        float cx = bounds.left + radius + 4 * density;
        float cy = bounds.centerY();
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * density);
        paint.setColor(Color.parseColor("#6B7280"));
        canvas.drawCircle(cx, cy, radius, paint);
        if ("true".equals(model.getProperty("checked"))) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.parseColor("#0B4499"));
            canvas.drawCircle(cx, cy, radius / 2, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        paint.setTextSize(ComponentDefinition.parseTextSizeSp(model, 16) * density);
        canvas.drawText(model.getProperty("text", "Option"),
                cx + radius + 8 * density,
                cy + ComponentDefinition.parseTextSizeSp(model, 16) * density * 0.35f,
                paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        RadioButton view = new RadioButton(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setText(model.getProperty("text", "Option"));
        view.setTextSize(ComponentDefinition.parseTextSizeSp(model, 16));
        view.setTextColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        view.setChecked("true".equals(model.getProperty("checked")));
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
        attrs.put("android:checked", model.getProperty("checked", "false"));
        return attrs;
    }
}
