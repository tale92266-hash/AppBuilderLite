package com.nobi.appbuilder.library.design;

import com.google.gson.Gson;

/** A named text style: size, weight, line height, spacing, family. */
public class TypographyToken {

    private final String name;
    private float fontSizeSp;
    private int fontWeight;
    private float lineHeightSp;
    private float letterSpacing;
    private String fontFamily;

    public TypographyToken(String name, float fontSizeSp, int fontWeight,
                           float lineHeightSp, float letterSpacing,
                           String fontFamily) {
        this.name = name;
        this.fontSizeSp = fontSizeSp;
        this.fontWeight = fontWeight;
        this.lineHeightSp = lineHeightSp;
        this.letterSpacing = letterSpacing;
        this.fontFamily = fontFamily;
    }

    public String getName() {
        return name;
    }

    public float getFontSizeSp() {
        return fontSizeSp;
    }

    public void setFontSizeSp(float fontSizeSp) {
        this.fontSizeSp = fontSizeSp;
    }

    public int getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(int fontWeight) {
        this.fontWeight = fontWeight;
    }

    public float getLineHeightSp() {
        return lineHeightSp;
    }

    public void setLineHeightSp(float lineHeightSp) {
        this.lineHeightSp = lineHeightSp;
    }

    public float getLetterSpacing() {
        return letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing) {
        this.letterSpacing = letterSpacing;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public DesignToken toToken(String id, String projectId) {
        return new DesignToken(id, projectId, DesignToken.KIND_TYPOGRAPHY,
                name, new Gson().toJson(this));
    }

    public static TypographyToken fromToken(DesignToken token) {
        try {
            TypographyToken parsed = new Gson().fromJson(
                    token.getValue(), TypographyToken.class);
            return parsed != null ? parsed
                    : new TypographyToken(token.getName(), 16, 400, 20, 0, "");
        } catch (Exception e) {
            return new TypographyToken(token.getName(), 16, 400, 20, 0, "");
        }
    }
}
