package com.nobi.appbuilder.model;

/**
 * Theme configuration of a project document. Colours are hex strings
 * ("#RRGGBB"). The preview renderer (Phase 6) and the XML generator
 * (Phase 7) both read from this one model.
 */
public class ThemeModel {

    private String name;
    private String primaryColor;
    private String primaryDarkColor;
    private String accentColor;
    private String backgroundColor;
    private String surfaceColor;
    private String textColor;
    private boolean darkMode;
    private String fontFamily;

    public ThemeModel() {
        name = "AppBuilder Default";
        primaryColor = "#0B4499";
        primaryDarkColor = "#08306E";
        accentColor = "#7C5CFF";
        backgroundColor = "#FFFFFF";
        surfaceColor = "#FFFFFF";
        textColor = "#111827";
        darkMode = false;
        fontFamily = "sans-serif";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "AppBuilder Default" : name;
    }

    public String getPrimaryColor() {
        return primaryColor;
    }

    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    public String getPrimaryDarkColor() {
        return primaryDarkColor;
    }

    public void setPrimaryDarkColor(String primaryDarkColor) {
        this.primaryDarkColor = primaryDarkColor;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getSurfaceColor() {
        return surfaceColor;
    }

    public void setSurfaceColor(String surfaceColor) {
        this.surfaceColor = surfaceColor;
    }

    public String getTextColor() {
        return textColor;
    }

    public void setTextColor(String textColor) {
        this.textColor = textColor;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily == null ? "sans-serif" : fontFamily;
    }

    public ThemeModel copy() {
        ThemeModel copy = new ThemeModel();
        copy.name = name;
        copy.primaryColor = primaryColor;
        copy.primaryDarkColor = primaryDarkColor;
        copy.accentColor = accentColor;
        copy.backgroundColor = backgroundColor;
        copy.surfaceColor = surfaceColor;
        copy.textColor = textColor;
        copy.darkMode = darkMode;
        copy.fontFamily = fontFamily;
        return copy;
    }
}
