package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.FrameLayout;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** FrameLayout: a container that stacks children on top of each other. */
public final class FrameLayoutComponent {

    public static final String TYPE = "FrameLayout";

    private FrameLayoutComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "FrameLayout", ComponentCategory.LAYOUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.MATCH_PARENT)
                .container(true)
                .canvasRenderer(FrameLayoutComponent::drawOnCanvas)
                .previewBuilder(FrameLayoutComponent::buildPreviewView)
                .xmlProvider(FrameLayoutComponent::toXmlAttributes)
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
        LinearLayoutComponent.drawContainer(canvas, bounds, "FrameLayout", density);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        FrameLayout view = new FrameLayout(context);
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
