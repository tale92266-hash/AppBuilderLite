package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Arrays;
import java.util.Map;

/** Spinner: a dropdown selector. Preview shows a placeholder entry. */
public final class SpinnerComponent {

    public static final String TYPE = "Spinner";

    private SpinnerComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "Spinner", ComponentCategory.INPUT)
                .defaultSize(160, ComponentModel.WRAP_CONTENT)
                .defaultProperty("text", "Select an option")
                .defaultProperty("textSize", "16")
                .defaultProperty("textColor", "#111827")
                .minSize(120, 40)
                .canvasRenderer(SpinnerComponent::drawOnCanvas)
                .previewBuilder(SpinnerComponent::buildPreviewView)
                .xmlProvider(SpinnerComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(Color.parseColor("#9CA3AF"));
        canvas.drawRect(bounds.left + 2 * density, bounds.top + 2 * density,
                bounds.right - 2 * density, bounds.bottom - 2 * density, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("textColor"), Color.BLACK));
        paint.setTextSize(ComponentDefinition.parseTextSizeSp(model, 16) * density);
        canvas.drawText(model.getProperty("text", "Select an option"),
                bounds.left + 10 * density,
                bounds.centerY() + ComponentDefinition.parseTextSizeSp(model, 16) * density * 0.35f,
                paint);
        float cx = bounds.right - 16 * density;
        float cy = bounds.centerY();
        paint.setColor(Color.parseColor("#6B7280"));
        canvas.drawCircle(cx, cy, 3 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        Spinner view = new Spinner(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item,
                Arrays.asList(model.getProperty("text", "Select an option")));
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        view.setAdapter(adapter);
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:spinnerMode", "dropdown");
        return attrs;
    }
}
