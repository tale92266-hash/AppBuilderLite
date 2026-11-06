package com.nobi.appbuilder.properties;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;

/**
 * Reads and writes one property on a ComponentModel, keeping the three
 * JSON sections separate: visual/functional values live in properties,
 * parent-relative layout in layout, editor-only state in editorMetadata.
 * Width/height are kept in sync between the model ints and the layout
 * dimension strings.
 */
public final class PropertyBinding {

    private PropertyBinding() {
    }

    public static PropertyValue getValue(ComponentModel model, PropertyDefinition definition) {
        String key = definition.getKey();
        LayoutModel layout = model.getLayout();
        switch (key) {
            case "id":
                return PropertyValue.ofString(model.getId() == null ? "" : model.getId());
            case "type":
                return PropertyValue.ofString(model.getType() == null ? "" : model.getType());
            case "width":
                return PropertyValue.ofDimension(layout.getWidth());
            case "height":
                return PropertyValue.ofDimension(layout.getHeight());
            case "x":
                return PropertyValue.ofInt(model.getX());
            case "y":
                return PropertyValue.ofInt(model.getY());
            case "gravity":
                return PropertyValue.ofString(model.getProperty("gravity", ""));
            case "layoutGravity":
                return PropertyValue.ofString(layout.getLayoutGravity());
            case "layoutWeight":
                return PropertyValue.ofFloat(layout.getWeight());
            case "marginLeft":
                return PropertyValue.ofInt(layout.getMarginLeft());
            case "marginTop":
                return PropertyValue.ofInt(layout.getMarginTop());
            case "marginRight":
                return PropertyValue.ofInt(layout.getMarginRight());
            case "marginBottom":
                return PropertyValue.ofInt(layout.getMarginBottom());
            case "paddingLeft":
                return PropertyValue.ofInt(layout.getPaddingLeft());
            case "paddingTop":
                return PropertyValue.ofInt(layout.getPaddingTop());
            case "paddingRight":
                return PropertyValue.ofInt(layout.getPaddingRight());
            case "paddingBottom":
                return PropertyValue.ofInt(layout.getPaddingBottom());
            case "locked":
                return PropertyValue.ofBoolean(model.isLocked());
            case "textSize":
                return PropertyValue.ofDimension(model.getProperty("textSize", "16sp"));
            default:
                return PropertyValue.of(definition.getType(),
                        model.getProperty(key, definition.getDefaultValue()));
        }
    }

    public static void setValue(ComponentModel model, PropertyDefinition definition,
                                PropertyValue value) {
        String key = definition.getKey();
        LayoutModel layout = model.getLayout();
        switch (key) {
            case "id":
                model.setId(value.asString().trim());
                return;
            case "width": {
                int dp = value.asDimensionDp(ComponentModel.WRAP_CONTENT);
                model.setWidth(dp);
                layout.setWidth(LayoutModel.dimensionToString(dp));
                return;
            }
            case "height": {
                int dp = value.asDimensionDp(ComponentModel.WRAP_CONTENT);
                model.setHeight(dp);
                layout.setHeight(LayoutModel.dimensionToString(dp));
                return;
            }
            case "x":
                model.setX(value.asInt(model.getX()));
                return;
            case "y":
                model.setY(value.asInt(model.getY()));
                return;
            case "gravity":
                model.setProperty("gravity", value.asString());
                return;
            case "layoutGravity":
                layout.setLayoutGravity(value.asString());
                return;
            case "layoutWeight":
                layout.setWeight(value.asFloat(layout.getWeight()));
                return;
            case "marginLeft":
                layout.setMarginLeft(value.asInt(layout.getMarginLeft()));
                return;
            case "marginTop":
                layout.setMarginTop(value.asInt(layout.getMarginTop()));
                return;
            case "marginRight":
                layout.setMarginRight(value.asInt(layout.getMarginRight()));
                return;
            case "marginBottom":
                layout.setMarginBottom(value.asInt(layout.getMarginBottom()));
                return;
            case "paddingLeft":
                layout.setPaddingLeft(value.asInt(layout.getPaddingLeft()));
                return;
            case "paddingTop":
                layout.setPaddingTop(value.asInt(layout.getPaddingTop()));
                return;
            case "paddingRight":
                layout.setPaddingRight(value.asInt(layout.getPaddingRight()));
                return;
            case "paddingBottom":
                layout.setPaddingBottom(value.asInt(layout.getPaddingBottom()));
                return;
            case "locked":
                model.setLocked(value.asBoolean(false));
                return;
            case "textSize":
                model.setProperty("textSize", normalizeTextSize(value.asString()));
                return;
            default:
                model.setProperty(key, value.asString());
                return;
        }
    }

    private static String normalizeTextSize(String raw) {
        String value = raw.trim().toLowerCase();
        if (value.endsWith("sp") || value.endsWith("dp")) {
            return value;
        }
        try {
            Float.parseFloat(value);
            return value + "sp";
        } catch (NumberFormatException e) {
            return "16sp";
        }
    }

    /** Writes every definition's default back (except id and type). */
    public static void resetToDefaults(ComponentModel model,
                                       java.util.List<PropertyDefinition> definitions) {
        for (PropertyDefinition definition : definitions) {
            String key = definition.getKey();
            if ("id".equals(key) || "type".equals(key)) {
                continue;
            }
            setValue(model, definition,
                    PropertyValue.of(definition.getType(), definition.getDefaultValue()));
        }
    }
}
