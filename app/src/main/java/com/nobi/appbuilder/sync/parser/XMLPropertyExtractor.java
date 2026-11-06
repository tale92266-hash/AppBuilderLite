package com.nobi.appbuilder.sync.parser;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Extracts the supported attributes of one parsed element into two
 * maps: component properties (text, colors, …) and layout parameters
 * (width, height, margins, padding, gravity, weight). Anything not
 * in the supported set is reported as unknown so the caller can
 * preserve it or warn — never silently drop it.
 */
public class XMLPropertyExtractor {

    /** Result of one extraction. */
    public static class Extracted {
        public final Map<String, String> componentProps =
                new LinkedHashMap<>();
        public final Map<String, String> layoutProps =
                new LinkedHashMap<>();
        public final Map<String, String> unknownAttrs =
                new LinkedHashMap<>();
    }

    private static final Map<String, String> ATTR_TO_COMPONENT =
            new LinkedHashMap<>();
    private static final Map<String, String> ATTR_TO_LAYOUT =
            new LinkedHashMap<>();

    static {
        ATTR_TO_COMPONENT.put("android:text", "text");
        ATTR_TO_COMPONENT.put("android:textSize", "textSize");
        ATTR_TO_COMPONENT.put("android:textColor", "textColor");
        ATTR_TO_COMPONENT.put("android:background", "backgroundColor");
        ATTR_TO_COMPONENT.put("android:hint", "hint");
        ATTR_TO_COMPONENT.put("android:inputType", "inputType");
        ATTR_TO_COMPONENT.put("android:scaleType", "scaleType");
        ATTR_TO_COMPONENT.put("android:orientation", "orientation");
        ATTR_TO_COMPONENT.put("android:checked", "checked");
        ATTR_TO_COMPONENT.put("android:visibility", "visibility");
        ATTR_TO_COMPONENT.put("android:enabled", "enabled");
        ATTR_TO_COMPONENT.put("android:gravity", "gravity");
        ATTR_TO_COMPONENT.put("android:max", "max");
        ATTR_TO_COMPONENT.put("android:progress", "progress");
        ATTR_TO_COMPONENT.put("android:indeterminate", "indeterminate");
        ATTR_TO_COMPONENT.put("android:src", "src");
        ATTR_TO_COMPONENT.put("android:elevation", "elevation");
        ATTR_TO_COMPONENT.put("app:cardCornerRadius", "cornerRadius");
        ATTR_TO_COMPONENT.put("app:cardBackgroundColor", "backgroundColor");

        ATTR_TO_LAYOUT.put("android:layout_width", "width");
        ATTR_TO_LAYOUT.put("android:layout_height", "height");
        ATTR_TO_LAYOUT.put("android:padding", "padding");
        ATTR_TO_LAYOUT.put("android:paddingLeft", "paddingLeft");
        ATTR_TO_LAYOUT.put("android:paddingTop", "paddingTop");
        ATTR_TO_LAYOUT.put("android:paddingRight", "paddingRight");
        ATTR_TO_LAYOUT.put("android:paddingBottom", "paddingBottom");
        ATTR_TO_LAYOUT.put("android:layout_margin", "margin");
        ATTR_TO_LAYOUT.put("android:layout_marginLeft", "marginLeft");
        ATTR_TO_LAYOUT.put("android:layout_marginTop", "marginTop");
        ATTR_TO_LAYOUT.put("android:layout_marginRight", "marginRight");
        ATTR_TO_LAYOUT.put("android:layout_marginBottom", "marginBottom");
        ATTR_TO_LAYOUT.put("android:layout_gravity", "layoutGravity");
        ATTR_TO_LAYOUT.put("android:layout_weight", "weight");
    }

    public static boolean isSupportedAttribute(String attrName) {
        return ATTR_TO_COMPONENT.containsKey(attrName)
                || ATTR_TO_LAYOUT.containsKey(attrName)
                || "android:id".equals(attrName)
                || attrName.startsWith("app:layout_constraint");
    }

    public Extracted extract(XMLParseResult.ParsedElement element) {
        Extracted out = new Extracted();
        for (Map.Entry<String, String> attr
                : element.attributes.entrySet()) {
            String name = attr.getKey();
            String value = attr.getValue();
            if ("android:id".equals(name)
                    || "xmlns:android".equals(name)
                    || "xmlns:app".equals(name)) {
                continue;
            }
            if (ATTR_TO_COMPONENT.containsKey(name)) {
                out.componentProps.put(ATTR_TO_COMPONENT.get(name),
                        convertValue(name, value));
            } else if (ATTR_TO_LAYOUT.containsKey(name)) {
                out.layoutProps.put(ATTR_TO_LAYOUT.get(name),
                        convertValue(name, value));
            } else if (name.startsWith("app:layout_constraint")) {
                out.componentProps.put(constraintKey(name), value);
            } else {
                out.unknownAttrs.put(name, value);
            }
        }
        return out;
    }

    private String convertValue(String attrName, String value) {
        if (value == null) {
            return "";
        }
        if ("android:textSize".equals(attrName)
                || "app:cardCornerRadius".equals(attrName)) {
            return value.replace("sp", "").replace("dp", "").trim();
        }
        if ("android:inputType".equals(attrName)) {
            if (value.contains("textPassword")) {
                return "password";
            }
            if (value.contains("number")) {
                return "number";
            }
            if (value.contains("textEmailAddress")) {
                return "email";
            }
            if (value.contains("phone")) {
                return "phone";
            }
            if (value.contains("textMultiLine")) {
                return "multiline";
            }
            return "text";
        }
        return value;
    }

    private String constraintKey(String attrName) {
        switch (attrName) {
            case "app:layout_constraintTop_toTopOf":
                return "constraintTop";
            case "app:layout_constraintBottom_toBottomOf":
                return "constraintBottom";
            case "app:layout_constraintStart_toStartOf":
                return "constraintStart";
            case "app:layout_constraintEnd_toEndOf":
                return "constraintEnd";
            case "app:layout_constraintHorizontal_bias":
                return "biasHorizontal";
            case "app:layout_constraintVertical_bias":
                return "biasVertical";
            default:
                return attrName;
        }
    }
}
