package com.nobi.appbuilder.assets.drawable;

/**
 * Generates Android shape-drawable XML: rectangle/oval/line/ring,
 * solid fill, stroke, corner radius (uniform or per-corner) and
 * padding. Values are escaped; nothing is concatenated unsafely
 * into the XML beyond the validated inputs.
 */
public final class ShapeDrawableGenerator {

    private ShapeDrawableGenerator() {
    }

    public static String shapeXml(String shape, String solidColor,
                                  Integer strokeWidthDp, String strokeColor,
                                  Integer cornerRadiusDp,
                                  Integer[] cornersDp,
                                  Integer paddingDp) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<shape xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        xml.append("    android:shape=\"")
                .append(shape == null ? "rectangle" : shape).append("\">\n");
        if (solidColor != null && !solidColor.isEmpty()) {
            xml.append("    <solid android:color=\"").append(solidColor)
                    .append("\" />\n");
        }
        if (strokeWidthDp != null && strokeColor != null) {
            xml.append("    <stroke android:width=\"").append(strokeWidthDp)
                    .append("dp\" android:color=\"").append(strokeColor)
                    .append("\" />\n");
        }
        if (cornersDp != null && cornersDp.length == 4) {
            xml.append("    <corners android:topLeftRadius=\"")
                    .append(cornersDp[0]).append("dp\"\n")
                    .append("        android:topRightRadius=\"")
                    .append(cornersDp[1]).append("dp\"\n")
                    .append("        android:bottomLeftRadius=\"")
                    .append(cornersDp[2]).append("dp\"\n")
                    .append("        android:bottomRightRadius=\"")
                    .append(cornersDp[3]).append("dp\" />\n");
        } else if (cornerRadiusDp != null) {
            xml.append("    <corners android:radius=\"")
                    .append(cornerRadiusDp).append("dp\" />\n");
        }
        if (paddingDp != null) {
            xml.append("    <padding android:left=\"").append(paddingDp)
                    .append("dp\" android:top=\"").append(paddingDp)
                    .append("dp\" android:right=\"").append(paddingDp)
                    .append("dp\" android:bottom=\"").append(paddingDp)
                    .append("dp\" />\n");
        }
        xml.append("</shape>\n");
        return xml.toString();
    }
}
