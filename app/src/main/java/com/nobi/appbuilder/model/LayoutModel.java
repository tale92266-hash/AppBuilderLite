package com.nobi.appbuilder.model;

/**
 * Layout parameters of one component, as they appear in the JSON document
 * ("layout": { "width": "match_parent", "height": "wrap_content", ... })
 * and later in generated XML. Dimensions are strings so the document can
 * hold "match_parent", "wrap_content" or an exact "120dp" value; margins
 * and padding are plain dp integers.
 */
public class LayoutModel {

    public static final String MATCH_PARENT = "match_parent";
    public static final String WRAP_CONTENT = "wrap_content";

    private String width;
    private String height;
    private int marginLeft;
    private int marginTop;
    private int marginRight;
    private int marginBottom;
    private int paddingLeft;
    private int paddingTop;
    private int paddingRight;
    private int paddingBottom;
    private String gravity;
    private String layoutGravity;
    private float weight;

    public LayoutModel() {
        width = MATCH_PARENT;
        height = WRAP_CONTENT;
        gravity = "";
        layoutGravity = "";
    }

    public LayoutModel(String width, String height) {
        this();
        this.width = width == null ? MATCH_PARENT : width;
        this.height = height == null ? WRAP_CONTENT : height;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width == null ? MATCH_PARENT : width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height == null ? WRAP_CONTENT : height;
    }

    public int getMarginLeft() {
        return marginLeft;
    }

    public void setMarginLeft(int marginLeft) {
        this.marginLeft = Math.max(0, marginLeft);
    }

    public int getMarginTop() {
        return marginTop;
    }

    public void setMarginTop(int marginTop) {
        this.marginTop = Math.max(0, marginTop);
    }

    public int getMarginRight() {
        return marginRight;
    }

    public void setMarginRight(int marginRight) {
        this.marginRight = Math.max(0, marginRight);
    }

    public int getMarginBottom() {
        return marginBottom;
    }

    public void setMarginBottom(int marginBottom) {
        this.marginBottom = Math.max(0, marginBottom);
    }

    public int getPaddingLeft() {
        return paddingLeft;
    }

    public void setPaddingLeft(int paddingLeft) {
        this.paddingLeft = Math.max(0, paddingLeft);
    }

    public int getPaddingTop() {
        return paddingTop;
    }

    public void setPaddingTop(int paddingTop) {
        this.paddingTop = Math.max(0, paddingTop);
    }

    public int getPaddingRight() {
        return paddingRight;
    }

    public void setPaddingRight(int paddingRight) {
        this.paddingRight = Math.max(0, paddingRight);
    }

    public int getPaddingBottom() {
        return paddingBottom;
    }

    public void setPaddingBottom(int paddingBottom) {
        this.paddingBottom = Math.max(0, paddingBottom);
    }

    public String getGravity() {
        return gravity;
    }

    public void setGravity(String gravity) {
        this.gravity = gravity == null ? "" : gravity;
    }

    public String getLayoutGravity() {
        return layoutGravity;
    }

    public void setLayoutGravity(String layoutGravity) {
        this.layoutGravity = layoutGravity == null ? "" : layoutGravity;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = Math.max(0f, weight);
    }

    public boolean isWidthMatchParent() {
        return MATCH_PARENT.equals(width);
    }

    public boolean isWidthWrapContent() {
        return WRAP_CONTENT.equals(width);
    }

    public boolean isHeightMatchParent() {
        return MATCH_PARENT.equals(height);
    }

    public boolean isHeightWrapContent() {
        return WRAP_CONTENT.equals(height);
    }

    public LayoutModel copy() {
        LayoutModel copy = new LayoutModel(width, height);
        copy.marginLeft = marginLeft;
        copy.marginTop = marginTop;
        copy.marginRight = marginRight;
        copy.marginBottom = marginBottom;
        copy.paddingLeft = paddingLeft;
        copy.paddingTop = paddingTop;
        copy.paddingRight = paddingRight;
        copy.paddingBottom = paddingBottom;
        copy.gravity = gravity;
        copy.layoutGravity = layoutGravity;
        copy.weight = weight;
        return copy;
    }

    /**
     * Parses a dimension string to the ComponentModel int convention:
     * "match_parent" -&gt; -1, "wrap_content" -&gt; -2, "120dp"/"120" -&gt; 120.
     * Returns fallback for anything unparseable.
     */
    public static int parseDimension(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        String v = value.trim().toLowerCase();
        if (MATCH_PARENT.equals(v)) {
            return ComponentModel.MATCH_PARENT;
        }
        if (WRAP_CONTENT.equals(v)) {
            return ComponentModel.WRAP_CONTENT;
        }
        if (v.endsWith("dp")) {
            v = v.substring(0, v.length() - 2);
        } else if (v.endsWith("px")) {
            v = v.substring(0, v.length() - 2);
        }
        try {
            return Math.max(0, Integer.parseInt(v.trim()));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /** Converts the ComponentModel int convention back to a string. */
    public static String dimensionToString(int dp) {
        if (dp == ComponentModel.MATCH_PARENT) {
            return MATCH_PARENT;
        }
        if (dp == ComponentModel.WRAP_CONTENT) {
            return WRAP_CONTENT;
        }
        return dp + "dp";
    }
}
