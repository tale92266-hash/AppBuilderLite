package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** ConstraintLayout: a flat container positioning children by constraints. */
public final class ConstraintLayoutComponent {

    public static final String TYPE = "ConstraintLayout";

    private ConstraintLayoutComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "ConstraintLayout",
                ComponentCategory.LAYOUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.MATCH_PARENT)
                .container(true)
                .canvasRenderer(ConstraintLayoutComponent::drawOnCanvas)
                .previewBuilder(ConstraintLayoutComponent::buildPreviewView)
                .xmlProvider(ConstraintLayoutComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            Paint fill = new Paint();
            fill.setColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
            canvas.drawRect(bounds, fill);
        }
        LinearLayoutComponent.drawContainer(canvas, bounds, "ConstraintLayout", density);
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setColor(Color.parseColor("#7C5CFF"));
        float r = 3 * density;
        canvas.drawCircle(bounds.left, bounds.top, r, dot);
        canvas.drawCircle(bounds.right, bounds.top, r, dot);
        canvas.drawCircle(bounds.left, bounds.bottom, r, dot);
        canvas.drawCircle(bounds.right, bounds.bottom, r, dot);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        ConstraintLayout view = new ConstraintLayout(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            view.setBackgroundColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
        }
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        return ComponentDefinition.baseXmlAttributes(model);
    }
}
