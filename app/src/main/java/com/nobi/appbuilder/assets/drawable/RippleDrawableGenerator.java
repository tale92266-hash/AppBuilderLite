package com.nobi.appbuilder.assets.drawable;

/** Generates an Android ripple drawable (API 21+). */
public final class RippleDrawableGenerator {

    private RippleDrawableGenerator() {
    }

    public static String rippleXml(String rippleColor,
                                   String contentDrawable) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<ripple xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        xml.append("    android:color=\"").append(rippleColor).append("\">\n");
        if (contentDrawable != null && !contentDrawable.isEmpty()) {
            xml.append("    <item android:drawable=\"@drawable/")
                    .append(contentDrawable).append("\" />\n");
        }
        xml.append("</ripple>\n");
        return xml.toString();
    }
}
