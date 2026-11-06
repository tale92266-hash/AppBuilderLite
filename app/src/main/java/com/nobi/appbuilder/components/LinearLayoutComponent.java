package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.View;
import android.widget.LinearLayout;

import com.nobi.appbuilder.model.ComponentModel;

import java.util.Map;

/** LinearLayout: a container that stacks children vertically or horizontally. */
public final class LinearLayoutComponent {

    public static final String TYPE = "LinearLayout";

    private LinearLayoutComponent() {
    }

    public static ComponentDefinition definition() {
        return new ComponentDefinition.Builder(TYPE, "LinearLayout", ComponentCategory.LAYOUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.MATCH_PARENT)
                .container(true)
                .defaultProperty("orientation", "vertical")
                .canvasRenderer(LinearLayoutComponent::drawOnCanvas)
                .previewBuilder(LinearLayoutComponent::buildPreviewView)
                .xmlProvider(LinearLayoutComponent::toXmlAttributes)
                .build();
    }

    public static ComponentModel createDefault() {
        return ComponentFactory.create(TYPE);
    }

    /** Shared container look: dashed border plus a small type label. */
    public static void drawContainer(Canvas canvas, RectF bounds, String label,
                                     float density) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.parseColor("#7C5CFF"));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2 * density);
        paint.setPathEffect(new DashPathEffect(new float[]{8 * density, 6 * density}, 0));
        canvas.drawRect(bounds, paint);
        paint.setPathEffect(null);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.parseColor("#7C5CFF"));
        paint.setTextSize(11 * density);
        paint.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, bounds.left + 4 * density, bounds.top + 12 * density, paint);
    }

    public static void drawOnCanvas(Canvas canvas, RectF bounds,
                                    ComponentModel model, float density) {
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            Paint fill = new Paint();
            fill.setColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
            canvas.drawRect(bounds, fill);
        }
        drawContainer(canvas, bounds, "LinearLayout", density);
    }

    public static View buildPreviewView(Context context, ComponentModel model) {
        LinearLayout view = new LinearLayout(context);
        view.setLayoutParams(ComponentDefinition.layoutParams(context, model));
        String orientation = model.getProperty("orientation", "vertical");
        view.setOrientation("horizontal".equals(orientation)
                ? LinearLayout.HORIZONTAL : LinearLayout.VERTICAL);
        String bg = model.getProperty("backgroundColor");
        if (bg != null) {
            view.setBackgroundColor(ComponentDefinition.parseColor(bg, Color.TRANSPARENT));
        }
        return view;
    }

    public static Map<String, String> toXmlAttributes(ComponentModel model) {
        Map<String, String> attrs = ComponentDefinition.baseXmlAttributes(model);
        attrs.put("android:orientation", model.getProperty("orientation", "vertical"));
        return attrs;
    }
}
