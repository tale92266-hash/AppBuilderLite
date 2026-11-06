package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** Button: a tappable label with a coloured background. */
public final class ButtonComponent {

    public static final String TYPE = "Button";

    private ButtonComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "Button", ComponentCategory.BASIC)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "Button")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#FFFFFF")
                .defaultProperty("backgroundColor", "#0B4499")
                .defaultProperty("cornerRadius", "8")
                .minSize(48, 32)
                .canvasRenderer(ButtonComponent::drawOnCanvas)
                .previewBuilder(ButtonComponent::buildPreviewView)
                .xmlProvider(ButtonComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        float radius = 8 * density;
        try {
            radius = Float.parseFloat(model.getProperty("cornerRadius", "8")) * density;
        } catch (NumberFormatException ignored) {
        }
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.parseColor("#0B4499")));
        canvas.drawRoundRect(bounds, radius, radius, paint);
        float textSizePx = ComponentDefinition.parseTextSizeSp(model, 16) * density;
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.WHITE));
        paint.setTextSize(textSizePx);
        paint.setTextAlign(Paint.Align.CENTER);
        float padShiftY = (model.getLayout().getPaddingTop()
                - model.getLayout().getPaddingBottom()) / 2f * density;
        float y = bounds.centerY() + textSizePx * 0.35f + padShiftY;
        canvas.drawText(model.getProperty("text", "Button"), bounds.centerX(), y, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        Button view = new Button(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setText(model.getProperty("text", "Button"));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                ComponentDefinition.parseTextSizeSp(model, 16));
        view.setTextColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.WHITE));
        view.setBackgroundColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.parseColor("#0B4499")));
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
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            attrs.put("android:background", bg);
        }
        return attrs;
    }
}
