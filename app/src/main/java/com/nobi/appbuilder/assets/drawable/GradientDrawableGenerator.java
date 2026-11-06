package com.nobi.appbuilder.assets.drawable;

/** Generates Android gradient-drawable XML (linear/radial/sweep). */
public final class GradientDrawableGenerator {

    private GradientDrawableGenerator() {
    }

    public static String gradientXml(String type, int angle,
                                     String startColor, String centerColor,
                                     String endColor) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        xml.append("    android:shape=\"rectangle\">\n");
        xml.append("    <gradient android:type=\"")
                .append(type == null ? "linear" : type).append("\"\n");
        if ("linear".equals(type) || type == null) {
            xml.append("        android:angle=\"").append(angle).append("\"\n");
        }
        xml.append("        android:startColor=\"").append(startColor)
                .append("\"\n");
        if (centerColor != null && !centerColor.isEmpty()) {
            xml.append("        android:centerColor=\"").append(centerColor)
                    .append("\"\n");
        }
        xml.append("        android:endColor=\"").append(endColor)
                .append("\" />\n");
        xml.append("</shape>\n");
        return xml.toString();
    }
}
