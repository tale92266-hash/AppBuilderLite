package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** Divider: a thin horizontal rule (a plain View with a background). */
public final class DividerComponent {

    public static final String TYPE = "Divider";

    private DividerComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "Divider", ComponentCategory.BASIC)
                .defaultSize(ComponentModel.MATCH_PARENT, 1)
                .defaultProperty("backgroundColor", "#E5E7EB")
                .minSize(16, 1)
                .canvasRenderer(DividerComponent::drawOnCanvas)
                .previewBuilder(DividerComponent::buildPreviewView)
                .xmlProvider(DividerComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.LTGRAY));
        canvas.drawRect(bounds.left, bounds.centerY() - density,
                bounds.right, bounds.centerY() + density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        View view = new View(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setBackgroundColor(ComponentDefinition.parseColor(
                model.getProperty("backgroundColor"), Color.LTGRAY));
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            attrs.put("android:background", bg);
        }
        return attrs;
    }
}
