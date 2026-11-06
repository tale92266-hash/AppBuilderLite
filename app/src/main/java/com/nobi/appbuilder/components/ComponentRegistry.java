package com.nobi.appbuilder.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.google.android.material.card.MaterialCardView;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single registry of component types. Everything in the app that needs
 * a component — factory, editor palette, preview, XML generator — asks
 * here instead of switching on type strings. MaterialCardView has no
 * dedicated component class file (Phase 2 list), so its definition is
 * built inline here with the same three strategies as the rest.
 */
public final class ComponentRegistry {

    private static final Map<String, ComponentDefinition> DEFINITIONS = new LinkedHashMap<>();

    static {
        register(TextViewComponent.definition());
        register(ButtonComponent.definition());
        register(ImageViewComponent.definition());
        register(EditTextComponent.definition());
        register(LinearLayoutComponent.definition());
        register(FrameLayoutComponent.definition());
        register(ConstraintLayoutComponent.definition());
        register(ScrollViewComponent.definition());
        register(CheckBoxComponent.definition());
        register(SwitchComponent.definition());
        register(ProgressBarComponent.definition());
        register(cardViewDefinition());
    }

    private ComponentRegistry() {
    }

    public static void register(ComponentDefinition definition) {
        if (definition != null && definition.getType() != null) {
            DEFINITIONS.put(definition.getType(), definition);
        }
    }

    public static void unregister(String type) {
        DEFINITIONS.remove(type);
    }

    public static ComponentDefinition get(String type) {
        return DEFINITIONS.get(type);
    }

    public static boolean contains(String type) {
        return type != null && DEFINITIONS.containsKey(type);
    }

    public static List<ComponentDefinition> getAll() {
        return new ArrayList<>(DEFINITIONS.values());
    }

    public static List<ComponentDefinition> getByCategory(ComponentCategory category) {
        List<ComponentDefinition> result = new ArrayList<>();
        for (ComponentDefinition definition : DEFINITIONS.values()) {
            if (definition.getCategory() == category) {
                result.add(definition);
            }
        }
        return result;
    }

    public static List<ComponentDefinition> getContainerTypes() {
        List<ComponentDefinition> result = new ArrayList<>();
        for (ComponentDefinition definition : DEFINITIONS.values()) {
            if (definition.isContainer()) {
                result.add(definition);
            }
        }
        return result;
    }

    public static List<ComponentDefinition> getLeafTypes() {
        List<ComponentDefinition> result = new ArrayList<>();
        for (ComponentDefinition definition : DEFINITIONS.values()) {
            if (!definition.isContainer()) {
                result.add(definition);
            }
        }
        return result;
    }

    private static ComponentDefinition cardViewDefinition() {
        return new ComponentDefinition.Builder("MaterialCardView", "CardView",
                ComponentCategory.LAYOUT)
                .defaultSize(ComponentModel.MATCH_PARENT, ComponentModel.WRAP_CONTENT)
                .container(true)
                .defaultProperty("backgroundColor", "#FFFFFF")
                .defaultProperty("cornerRadius", "12")
                .defaultProperty("elevation", "2")
                .canvasRenderer((Canvas canvas, RectF bounds, ComponentModel model,
                                 float density) -> {
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    float radius = 12 * density;
                    try {
                        radius = Float.parseFloat(
                                model.getProperty("cornerRadius", "12")) * density;
                    } catch (NumberFormatException ignored) {
                    }
                    paint.setColor(ComponentDefinition.parseColor(
                            model.getProperty("backgroundColor"), Color.WHITE));
                    canvas.drawRoundRect(bounds, radius, radius, paint);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(1.5f * density);
                    paint.setColor(Color.parseColor("#D1D5DB"));
                    canvas.drawRoundRect(bounds, radius, radius, paint);
                    LinearLayoutComponent.drawContainer(canvas, bounds, "CardView", density);
                })
                .previewBuilder((Context context, ComponentModel model) -> {
                    MaterialCardView view = new MaterialCardView(context);
                    view.setLayoutParams(
                            ComponentDefinition.layoutParams(context, model));
                    float density = context.getResources().getDisplayMetrics().density;
                    float radius = 12 * density;
                    float elevation = 2 * density;
                    try {
                        radius = Float.parseFloat(
                                model.getProperty("cornerRadius", "12")) * density;
                        elevation = Float.parseFloat(
                                model.getProperty("elevation", "2")) * density;
                    } catch (NumberFormatException ignored) {
                    }
                    view.setRadius(radius);
                    view.setCardElevation(elevation);
                    view.setCardBackgroundColor(ComponentDefinition.parseColor(
                            model.getProperty("backgroundColor"), Color.WHITE));
                    return view;
                })
                .xmlProvider((ComponentModel model) -> {
                    Map<String, String> attrs =
                            ComponentDefinition.baseXmlAttributes(model);
                    attrs.put("app:cardCornerRadius",
                            model.getProperty("cornerRadius", "12") + "dp");
                    attrs.put("app:cardElevation",
                            model.getProperty("elevation", "2") + "dp");
                    String bg = model.getProperty("backgroundColor");
                    if (bg != null) {
                        attrs.put("app:cardBackgroundColor", bg);
                    }
                    return attrs;
                })
                .build();
    }
}
