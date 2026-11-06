package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.InputType;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** EditText: a single-line text input with hint and input type. */
public final class EditTextComponent {

    public static final String TYPE = "EditText";

    private EditTextComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "EditText", ComponentCategory.INPUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.WRAP_CONTENT)
                .defaultProperty("hint", "Enter text")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .defaultProperty("inputType", "text")
                .minSize(48, 32)
                .canvasRenderer(EditTextComponent::drawOnCanvas)
                .previewBuilder(EditTextComponent::buildPreviewView)
                .xmlProvider(EditTextComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawRect(bounds, paint);
        float textSizePx = ComponentDefinition.parseTextSizeSp(model, 16) * density;
        paint.setTextSize(textSizePx);
        paint.setTextAlign(Paint.Align.LEFT);
        String text = model.getProperty("text", "");
        float padLeft = Math.max(4, model.getLayout().getPaddingLeft()) * density;
        float padShiftY = (model.getLayout().getPaddingTop()
                - model.getLayout().getPaddingBottom()) / 2f * density;
        float y = bounds.centerY() + textSizePx * 0.35f + padShiftY;
        if (text.isEmpty()) {
            paint.setColor(Color.parseColor("#9CA3AF"));
            canvas.drawText(model.getProperty("hint", "Enter text"),
                    bounds.left + padLeft, y, paint);
        } else {
            paint.setColor(ComponentDefinition.parseColor(
                    model.getProperty("textColor"), Color.BLACK));
            canvas.drawText(text, bounds.left + padLeft, y, paint);
        }
        paint.setColor(Color.parseColor("#0B4499"));
        paint.setStrokeWidth(2 * density);
        canvas.drawLine(bounds.left, bounds.bottom - 1 * density,
                bounds.right, bounds.bottom - 1 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        EditText view = new EditText(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setHint(model.getProperty("hint", "Enter text"));
        view.setText(model.getProperty("text", ""));
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP,
                ComponentDefinition.parseTextSizeSp(model, 16));
        view.setTextColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        view.setInputType(inputTypeFor(model.getProperty("inputType", "text")));
        return view;
    }

    static int inputTypeFor(String inputType) {
        if (inputType == null) {
            return InputType.TYPE_CLASS_TEXT;
        }
        switch (inputType) {
            case "number":
                return InputType.TYPE_CLASS_NUMBER;
            case "password":
                return InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD;
            case "email":
                return InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;
            case "phone":
                return InputType.TYPE_CLASS_PHONE;
            default:
                return InputType.TYPE_CLASS_TEXT;
        }
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:hint", model.getProperty("hint", ""));
        String text = model.getProperty("text");
        if (text != null && !text.isEmpty()) {
            attrs.put("android:text", text);
        }
        attrs.put("android:textSize",
                ((int) ComponentDefinition.parseTextSizeSp(model, 16)) + "sp");
        attrs.put("android:inputType", model.getProperty("inputType", "text"));
        return attrs;
    }
}
