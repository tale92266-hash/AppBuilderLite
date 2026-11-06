package com.nobi.appbuilder.generator.mapping;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.properties.PropertyRegistry;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps component properties to generated XML attributes. The source
 * of truth for attribute names is the Phase 4 PropertyRegistry
 * (xmlAttributeFor); value conversions that need more than a rename
 * live here. Properties with no mapping are NOT dropped silently —
 * the caller reports them as warnings.
 */
public final class PropertyMappingRegistry {

    private PropertyMappingRegistry() {
    }

    /** Attribute name for a property key, or null when unmapped. */
    public static String attributeFor(String key) {
        String mapped = PropertyRegistry.xmlAttributeFor(key);
        if (mapped != null) {
            return mapped;
        }
        switch (key) {
            case "text":
                return "android:text";
            case "hint":
                return "android:hint";
            case "checked":
                return "android:checked";
            case "indeterminate":
                return "android:indeterminate";
            case "max":
                return "android:max";
            case "progress":
                return "android:progress";
            case "maxLines":
                return "android:maxLines";
            case "visibility":
                return "android:visibility";
            case "alpha":
                return "android:alpha";
            case "enabled":
                return "android:enabled";
            case "contentDescription":
                return "android:contentDescription";
            case "elevation":
                return "android:elevation";
            case "fontWeight":
                return "android:textStyle";
            case "typeface":
                return "android:typeface";
            default:
                return null;
        }
    }

    /**
     * Converts a raw property value to its XML form for the given
     * attribute. Returns null when the value cannot be represented
     * (the caller warns instead of writing garbage).
     */
    public static String convertValue(String key, String raw,
                                      ComponentModel model) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        switch (key) {
            case "textSize": {
                float sp = ComponentDefinition.parseTextSizeSp(model, -1f);
                if (sp <= 0) {
                    return null;
                }
                return formatNumber(sp) + "sp";
            }
            case "inputType":
                return mapInputType(value);
            case "scaleType":
                return mapScaleType(value);
            case "fontWeight":
                return "bold".equalsIgnoreCase(value) ? "bold" : "normal";
            case "orientation":
                return "horizontal".equalsIgnoreCase(value)
                        ? "horizontal" : "vertical";
            case "checked":
            case "indeterminate":
            case "enabled":
                return ("true".equalsIgnoreCase(value) || "1".equals(value))
                        ? "true" : "false";
            case "visibility": {
                if ("invisible".equalsIgnoreCase(value)) {
                    return "invisible";
                }
                if ("gone".equalsIgnoreCase(value)) {
                    return "gone";
                }
                return "visible";
            }
            case "elevation": {
                try {
                    return formatNumber(Float.parseFloat(value)) + "dp";
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            case "alpha": {
                try {
                    float alpha = Float.parseFloat(value);
                    if (alpha < 0f) {
                        alpha = 0f;
                    }
                    if (alpha > 1f) {
                        alpha = 1f;
                    }
                    return formatNumber(alpha);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            default:
                return value;
        }
    }

    /** Component-specific attributes from the Phase 2 XmlProvider,
     * with text/size/colour conversions applied on top. */
    public static Map<String, String> providerAttributes(ComponentModel model) {
        Map<String, String> out = new LinkedHashMap<>();
        ComponentDefinition definition =
                com.nobi.appbuilder.components.ComponentRegistry.get(model.getType());
        if (definition == null || definition.getXmlProvider() == null) {
            return out;
        }
        Map<String, String> provided = definition.getXmlProvider().attributes(model);
        if (provided == null) {
            return out;
        }
        for (Map.Entry<String, String> entry : provided.entrySet()) {
            out.put(entry.getKey(), entry.getValue());
        }
        return out;
    }

    public static String mapInputType(String value) {
        switch (value.toLowerCase()) {
            case "password":
                return "textPassword";
            case "number":
                return "number";
            case "phone":
                return "phone";
            case "email":
                return "textEmailAddress";
            case "multiline":
                return "textMultiLine";
            case "text":
            default:
                return "text";
        }
    }

    public static String mapScaleType(String value) {
        switch (value) {
            case "fitCenter":
                return "fitCenter";
            case "fitXY":
                return "fitXY";
            case "centerInside":
                return "centerInside";
            case "center":
                return "center";
            case "centerCrop":
            default:
                return "centerCrop";
        }
    }

    public static String formatNumber(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.valueOf(value);
    }
}
