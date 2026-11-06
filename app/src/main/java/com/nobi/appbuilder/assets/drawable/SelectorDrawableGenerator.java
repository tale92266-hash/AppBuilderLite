package com.nobi.appbuilder.assets.drawable;

/**
 * Generates a state-list drawable: pressed / selected / focused /
 * default, each pointing at a drawable resource name.
 */
public final class SelectorDrawableGenerator {

    private SelectorDrawableGenerator() {
    }

    public static String selectorXml(String pressedDrawable,
                                     String selectedDrawable,
                                     String focusedDrawable,
                                     String defaultDrawable) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<selector xmlns:android=\"http://schemas.android.com/apk/res/android\">\n");
        if (pressedDrawable != null) {
            xml.append("    <item android:state_pressed=\"true\" android:drawable=\"@drawable/")
                    .append(pressedDrawable).append("\" />\n");
        }
        if (selectedDrawable != null) {
            xml.append("    <item android:state_selected=\"true\" android:drawable=\"@drawable/")
                    .append(selectedDrawable).append("\" />\n");
        }
        if (focusedDrawable != null) {
            xml.append("    <item android:state_focused=\"true\" android:drawable=\"@drawable/")
                    .append(focusedDrawable).append("\" />\n");
        }
        xml.append("    <item android:drawable=\"@drawable/")
                .append(defaultDrawable == null ? "bg_button" : defaultDrawable)
                .append("\" />\n");
        xml.append("</selector>\n");
        return xml.toString();
    }
}
