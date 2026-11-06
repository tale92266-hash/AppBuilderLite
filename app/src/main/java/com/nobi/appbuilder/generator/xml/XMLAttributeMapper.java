package com.nobi.appbuilder.generator.xml;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.mapping.PropertyMappingRegistry;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.LayoutModel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps one component's properties to its generated XML attributes.
 * Sources, in order: the component's own XmlProvider (Phase 2),
 * the PropertyMappingRegistry conversions, and the layout parameters
 * for its parent type. Colours and strings go through the shared
 * resource collector so identical values share one resource.
 * Unsupported properties are collected as warnings by the caller —
 * nothing is dropped silently.
 */
public class XMLAttributeMapper {

    private final GenerationContext context;

    public XMLAttributeMapper(GenerationContext context) {
        this.context = context;
    }

    /**
     * All attributes for a component except android:id and the
     * parent-specific layout params (added by LayoutXMLGenerator).
     * Unsupported property keys are appended to {@code unsupported}.
     */
    public Map<String, String> map(ComponentModel model,
                                   java.util.List<String> unsupported) {
        Map<String, String> attrs = new LinkedHashMap<>();
        String type = model.getType();

        // Phase 2 provider attributes (textSize, textColor, ...).
        Map<String, String> provided =
                PropertyMappingRegistry.providerAttributes(model);
        for (Map.Entry<String, String> entry : provided.entrySet()) {
            attrs.put(entry.getKey(), convertProviderValue(model, entry));
        }

        for (Map.Entry<String, String> entry : model.getProperties().entrySet()) {
            String key = entry.getKey();
            String raw = entry.getValue();
            if (raw == null) {
                continue;
            }
            if (isHandledElsewhere(key, type)) {
                continue;
            }
            String attribute = PropertyMappingRegistry.attributeFor(key);
            if (attribute == null) {
                unsupported.add(key);
                continue;
            }
            String converted = convert(key, raw, model, attribute, attrs);
            if (converted != null) {
                attrs.put(attribute, converted);
            } else {
                unsupported.add(key + " (bad value '" + raw + "')");
            }
        }

        // Background: shape drawable when radius/border exist, else a
        // colour (resource when the collector already knows it).
        applyBackground(model, attrs);

        // Padding from the layout model.
        LayoutModel layout = model.getLayout();
        if (layout != null) {
            putIfPositive(attrs, "android:paddingLeft", layout.getPaddingLeft());
            putIfPositive(attrs, "android:paddingTop", layout.getPaddingTop());
            putIfPositive(attrs, "android:paddingRight", layout.getPaddingRight());
            putIfPositive(attrs, "android:paddingBottom", layout.getPaddingBottom());
        }
        return attrs;
    }

    /** Keys whose XML comes from another place (layout params, id,
     * constraints, text extraction, image source). */
    private boolean isHandledElsewhere(String key, String type) {
        switch (key) {
            case "id":
            case "width":
            case "height":
            case "x":
            case "y":
            case "gravity":
            case "layoutGravity":
            case "layoutWeight":
            case "marginLeft":
            case "marginTop":
            case "marginRight":
            case "marginBottom":
            case "constraintTop":
            case "constraintBottom":
            case "constraintStart":
            case "constraintEnd":
            case "biasHorizontal":
            case "biasVertical":
            case "cornerRadius":
            case "borderWidth":
            case "borderColor":
            case "src":
            case "backgroundColor":
                return true;
            case "text":
            case "hint":
                // Extracted to @string resources by convert().
                return false;
            default:
                return false;
        }
    }

    private String convert(String key, String raw, ComponentModel model,
                           String attribute, Map<String, String> attrs) {
        switch (key) {
            case "text":
            case "hint": {
                if (raw.isEmpty()) {
                    return "";
                }
                if (raw.startsWith("@string/")) {
                    return raw;
                }
                String name = context.getResources().stringFor(raw,
                        XMLIdResolver.sanitizeId(model.getId())
                                + ("text".equals(key) ? "_text" : "_hint"));
                return "@string/" + name;
            }
            case "textColor": {
                String name = context.getResources().colorFor(raw,
                        "color_" + XMLIdResolver.sanitizeId(model.getId())
                                + "_text");
                return "@color/" + name;
            }
            case "textSize": {
                String converted = PropertyMappingRegistry.convertValue(
                        key, raw, model);
                if (converted == null) {
                    return null;
                }
                String name = context.getResources().dimenFor(converted,
                        "text_size_" + converted.replace("sp", ""));
                return "@dimen/" + name;
            }
            default:
                return PropertyMappingRegistry.convertValue(key, raw, model);
        }
    }

    /** Provider values still need resource extraction for colours. */
    private String convertProviderValue(ComponentModel model,
                                        Map.Entry<String, String> entry) {
        String key = entry.getKey();
        String value = entry.getValue();
        if ("android:textColor".equals(key) && value != null
                && value.startsWith("#")) {
            String name = context.getResources().colorFor(value,
                    "color_" + XMLIdResolver.sanitizeId(model.getId()) + "_text");
            return "@color/" + name;
        }
        if ("android:textSize".equals(key) && value != null) {
            String name = context.getResources().dimenFor(value,
                    "text_size_" + value.replace("sp", ""));
            return "@dimen/" + name;
        }
        return value;
    }

    private void applyBackground(ComponentModel model, Map<String, String> attrs) {
        String bg = model.getProperty("backgroundColor");
        if (bg == null) {
            return;
        }
        String type = model.getType();
        float radius = 0f;
        String radiusRaw = model.getProperty("cornerRadius");
        if (radiusRaw != null) {
            try {
                radius = Float.parseFloat(radiusRaw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        String borderRaw = model.getProperty("borderWidth");
        float border = 0f;
        if (borderRaw != null) {
            try {
                border = Float.parseFloat(borderRaw.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        if ("Button".equals(type)) {
            if (radius > 0 || border > 0) {
                String shape = shapeFor(model, bg, radius, border);
                attrs.put("android:background", "@drawable/" + shape);
            } else {
                attrs.put("android:backgroundTint", colorRefOrHex(bg, model));
            }
            return;
        }
        if ("MaterialCardView".equals(type)) {
            attrs.put("app:cardBackgroundColor", colorRefOrHex(bg, model));
            if (radius > 0) {
                attrs.put("app:cardCornerRadius",
                        PropertyMappingRegistry.formatNumber(radius) + "dp");
            }
            return;
        }
        if (radius > 0 || border > 0) {
            String shape = shapeFor(model, bg, radius, border);
            attrs.put("android:background", "@drawable/" + shape);
        } else {
            attrs.put("android:background", colorRefOrHex(bg, model));
        }
    }

    private String colorRefOrHex(String hex, ComponentModel model) {
        if (hex.startsWith("#")) {
            String name = context.getResources().colorFor(hex,
                    "color_" + XMLIdResolver.sanitizeId(model.getId()) + "_bg");
            return "@color/" + name;
        }
        return hex;
    }

    /** Registers (or reuses) a per-component shape drawable. */
    private String shapeFor(ComponentModel model, String bg,
                            float radius, float border) {
        String id = XMLIdResolver.sanitizeId(model.getId());
        String existing = context.getResources().shapes.get(id);
        if (existing != null) {
            return existing;
        }
        String fileName = "bg_" + id;
        context.getResources().shapes.put(id, fileName);
        context.getResources().shapeSpecs.put(id,
                new GenerationContext.ResourceCollector.ShapeSpec(bg, radius,
                        border, model.getProperty("borderColor")));
        return fileName;
    }

    private void putIfPositive(Map<String, String> attrs, String key, int dp) {
        if (dp > 0) {
            attrs.put(key, dp + "dp");
        }
    }
}
