package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** CheckBox: a labelled on/off box. */
public final class CheckBoxComponent {

    public static final String TYPE = "CheckBox";

    private CheckBoxComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "CheckBox", ComponentCategory.INPUT)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "CheckBox")
                .defaultProperty("checked", "false")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .minSize(48, 24)
                .canvasRenderer(CheckBoxComponent::drawOnCanvas)
                .previewBuilder(CheckBoxComponent::buildPreviewView)
                .xmlProvider(CheckBoxComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        float box = 18 * density;
        float top = bounds.centerY() - box / 2;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * density);
        paint.setColor(Color.parseColor("#6B7280"));
        canvas.drawRect(bounds.left + 2 * density, top,
                bounds.left + 2 * density + box, top + box, paint);
        if ("true".equals(model.getProperty("checked"))) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(Color.parseColor("#0B4499"));
            paint.setStrokeWidth(2.5f * density);
            float x0 = bounds.left + 2 * density;
            canvas.drawLine(x0 + 4 * density, top + box / 2,
                    x0 + 8 * density, top + box - 4 * density, paint);
            canvas.drawLine(x0 + 8 * density, top + box - 4 * density,
                    x0 + box - 2 * density, top + 4 * density, paint);
        }
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        paint.setTextSize(ComponentDefinition.parseTextSizeSp(model, 16) * density);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(model.getProperty("text", "CheckBox"),
                bounds.left + 26 * density,
                bounds.centerY() + 5 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        CheckBox view = new CheckBox(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setText(model.getProperty("text", "CheckBox"));
        view.setChecked("true".equals(model.getProperty("checked")));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                ComponentDefinition.parseTextSizeSp(model, 16));
        view.setTextColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:text", model.getProperty("text", ""));
        attrs.put("android:checked", model.getProperty("checked", "false"));
        return attrs;
    }
}
