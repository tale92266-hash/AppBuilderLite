package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/**
 * ScrollView: a vertically scrolling container with exactly one child.
 * Its preview holds an inner vertical LinearLayout; the preview renderer
 * (Phase 6) adds the component's children into that inner layout.
 */
public final class ScrollViewComponent {

    public static final String TYPE = "ScrollView";

    private ScrollViewComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "ScrollView", ComponentCategory.LAYOUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.MATCH_PARENT)
                .container(true)
                .canvasRenderer(ScrollViewComponent::drawOnCanvas)
                .previewBuilder(ScrollViewComponent::buildPreviewView)
                .xmlProvider(ScrollViewComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        LinearLayoutComponent.drawContainer(canvas, bounds, "ScrollView", density);
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#C7CFDD"));
        float barWidth = 4 * density;
        canvas.drawRect(bounds.right - barWidth - 2 * density, bounds.top + 16 * density,
                bounds.right - 2 * density, bounds.bottom - 4 * density, paint);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        ScrollView view = new ScrollView(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        view.setFillViewport(true);
        LinearLayout inner = new LinearLayout(context);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setLayoutParams(new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT));
        view.addView(inner);
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        return ComponentDefinition.baseXmlAttributes(model);
    }
}
