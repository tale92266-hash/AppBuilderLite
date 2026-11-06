package com.nobi.appbuilder.generator.templates;

/**
 * Small fixed templates for the generated resource files that are
 * not built element-by-element (themes and the default shapes).
 */
public final class ResourceTemplate {

    private ResourceTemplate() {
    }

    /** AppTheme: MaterialComponents DayNight with the project colours. */
    public static String themesXml(String primary, String primaryDark,
                                   String accent) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<resources>\n"
                + "    <style name=\"AppTheme\" "
                + "parent=\"Theme.MaterialComponents.DayNight.DarkActionBar\">\n"
                + "        <item name=\"colorPrimary\">" + primary + "</item>\n"
                + "        <item name=\"colorPrimaryDark\">" + primaryDark + "</item>\n"
                + "        <item name=\"colorAccent\">" + accent + "</item>\n"
                + "    </style>\n"
                + "</resources>\n";
    }

    /** Rounded rectangle shape used by bg_button.xml / bg_card.xml. */
    public static String shapeXml(String color, String radiusDp) {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                + "    android:shape=\"rectangle\">\n"
                + "    <solid android:color=\"" + color + "\" />\n"
                + "    <corners android:radius=\"" + radiusDp + "\" />\n"
                + "</shape>\n";
    }

    /** Per-component shape: solid colour, radius and optional stroke. */
    public static String componentShapeXml(String color, String radiusDp,
                                           String strokeWidthDp,
                                           String strokeColor) {
        StringBuilder out = new StringBuilder();
        out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        out.append("<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        out.append("    android:shape=\"rectangle\">\n");
        out.append("    <solid android:color=\"").append(color).append("\" />\n");
        if (radiusDp != null) {
            out.append("    <corners android:radius=\"").append(radiusDp)
                    .append("\" />\n");
        }
        if (strokeWidthDp != null && strokeColor != null) {
            out.append("    <stroke android:width=\"").append(strokeWidthDp)
                    .append("\" android:color=\"").append(strokeColor)
                    .append("\" />\n");
        }
        out.append("</shape>\n");
        return out.toString();
    }
}
