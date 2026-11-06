package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.properties.PropertyRegistry;

/**
 * Two-way mapping between IR property keys and XML attributes.
 * The XML attribute comes from the Phase 4 PropertyRegistry so the
 * visual editor, the inspector and the sync engine never disagree
 * about a mapping.
 */
public class PropertySyncMapper {

    /** IR property -> XML attribute value (e.g. textSize 16 -> "16sp"). */
    public static String toXmlValue(String propertyKey, String value) {
        if (value == null) {
            return "";
        }
        if ("textSize".equals(propertyKey)
                || "cornerRadius".equals(propertyKey)) {
            return value + "sp";
        }
        if ("inputType".equals(propertyKey)) {
            switch (value) {
                case "password":
                    return "textPassword";
                case "number":
                    return "number";
                case "email":
                    return "textEmailAddress";
                case "phone":
                    return "phone";
                case "multiline":
                    return "textMultiLine";
                default:
                    return "text";
            }
        }
        return value;
    }

    /** XML attribute -> IR property key, or null when unsupported. */
    public static String toPropertyKey(String xmlAttribute) {
        if (xmlAttribute == null) {
            return null;
        }
        switch (xmlAttribute) {
            case "android:text":
                return "text";
            case "android:textSize":
                return "textSize";
            case "android:textColor":
                return "textColor";
            case "android:background":
                return "backgroundColor";
            case "android:hint":
                return "hint";
            case "android:inputType":
                return "inputType";
            case "android:scaleType":
                return "scaleType";
            case "android:orientation":
                return "orientation";
            case "android:checked":
                return "checked";
            case "android:visibility":
                return "visibility";
            case "android:enabled":
                return "enabled";
            case "android:gravity":
                return "gravity";
            case "android:max":
                return "max";
            case "android:progress":
                return "progress";
            case "android:indeterminate":
                return "indeterminate";
            case "android:src":
                return "src";
            case "android:elevation":
                return "elevation";
            case "app:cardCornerRadius":
                return "cornerRadius";
            case "app:cardBackgroundColor":
                return "backgroundColor";
            default:
                return null;
        }
    }

    /** The XML attribute for an IR property key, or null. */
    public static String toXmlAttribute(String propertyKey) {
        return PropertyRegistry.xmlAttributeFor(propertyKey);
    }
}
