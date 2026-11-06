package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.TypedValue;
import android.view.View;
import android.widget.Switch;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** Switch: a labelled on/off toggle. */
public final class SwitchComponent {

    public static final String TYPE = "Switch";

    private SwitchComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "Switch", ComponentCategory.INPUT)
                .defaultSize(ComponentModel.WRAP_CONTENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "Switch")
                .defaultProperty("checked", "false")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .minSize(48, 24)
                .canvasRenderer(SwitchComponent::drawOnCanvas)
                .previewBuilder(SwitchComponent::buildPreviewView)
                .xmlProvider(SwitchComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        boolean checked = "true".equals(model.getProperty("checked"));
        float padLeft = model.getLayout().getPaddingLeft() * density;
        float padShiftY = (model.getLayout().getPaddingTop()
                - model.getLayout().getPaddingBottom()) / 2f * density;
        float trackW = 34 * density;
        float trackH = 20 * density;
        float top = bounds.centerY() - trackH / 2 + padShiftY;
        float left = bounds.left + 2 * density + padLeft;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(checked ? Color.parseColor("#0B4499")
                : Color.parseColor("#C7CFDD"));
        canvas.drawRoundRect(new RectF(left, top, left + trackW, top + trackH),
                trackH / 2, trackH / 2, paint);
        float thumbR = 8 * density;
        float thumbCx = checked ? left + trackW - thumbR - 2 * density
                : left + thumbR + 2 * density;
        paint.setColor(Color.WHITE);
        canvas.drawCircle(thumbCx, bounds.centerY() + padShiftY, thumbR, paint);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        paint.setTextSize(ComponentDefinition.parseTextSizeSp(model, 16) * density);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(model.getProperty("text", "Switch"),
                left + trackW + 8 * density,
                bounds.centerY() + 5 * density + padShiftY, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        Switch view = new Switch(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setText(model.getProperty("text", "Switch"));
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
