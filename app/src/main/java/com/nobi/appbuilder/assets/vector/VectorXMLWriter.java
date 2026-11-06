package com.nobi.appbuilder.assets.vector;

/** Writes a VectorAssetModel as valid VectorDrawable XML. */
public final class VectorXMLWriter {

    private VectorXMLWriter() {
    }

    public static String toXml(VectorAssetModel model) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        xml.append("<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n");
        xml.append("    android:width=\"").append(format(model.getWidthDp()))
                .append("dp\"\n");
        xml.append("    android:height=\"").append(format(model.getHeightDp()))
                .append("dp\"\n");
        xml.append("    android:viewportWidth=\"")
                .append(format(model.getViewportWidth())).append("\"\n");
        xml.append("    android:viewportHeight=\"")
                .append(format(model.getViewportHeight())).append("\">\n");
        for (VectorPathModel path : model.getPaths()) {
            xml.append("    <path\n");
            if (path.getFillColor() != null) {
                xml.append("        android:fillColor=\"")
                        .append(path.getFillColor()).append("\"\n");
            }
            if (path.getStrokeColor() != null) {
                xml.append("        android:strokeColor=\"")
                        .append(path.getStrokeColor()).append("\"\n");
                xml.append("        android:strokeWidth=\"")
                        .append(format(path.getStrokeWidth())).append("\"\n");
            }
            if (path.getFillAlpha() < 1f) {
                xml.append("        android:fillAlpha=\"")
                        .append(format(path.getFillAlpha())).append("\"\n");
            }
            if (path.getStrokeAlpha() < 1f) {
                xml.append("        android:strokeAlpha=\"")
                        .append(format(path.getStrokeAlpha())).append("\"\n");
            }
            if (path.getTrimPathStart() > 0f) {
                xml.append("        android:trimPathStart=\"")
                        .append(format(path.getTrimPathStart())).append("\"\n");
            }
            if (path.getTrimPathEnd() < 1f) {
                xml.append("        android:trimPathEnd=\"")
                        .append(format(path.getTrimPathEnd())).append("\"\n");
            }
            xml.append("        android:pathData=\"")
                    .append(escape(path.getPathData())).append("\" />\n");
        }
        xml.append("</vector>\n");
        return xml.toString();
    }

    private static String format(float value) {
        if (value == Math.round(value)) {
            return String.valueOf(Math.round(value));
        }
        return String.valueOf(value);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace("\"", "&quot;");
    }
}
