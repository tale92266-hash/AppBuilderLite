package com.nobi.appbuilder.library.design;

import com.nobi.appbuilder.model.ThemeModel;

/** A light/dark/custom theme: a named set of colours. */
public class ThemeDefinition {

    private String id;
    private String projectId;
    private String name;
    private boolean darkMode;
    private String primaryColor;
    private String primaryDarkColor;
    private String accentColor;
    private String backgroundColor;
    private String surfaceColor;
    private String textColor;
    private long createdAt;

    public ThemeDefinition() {
    }

    public static ThemeDefinition light(String id, String projectId,
                                        String name) {
        ThemeDefinition theme = new ThemeDefinition();
        theme.id = id;
        theme.projectId = projectId;
        theme.name = name;
        theme.darkMode = false;
        theme.primaryColor = "#0B4499";
        theme.primaryDarkColor = "#082F6B";
        theme.accentColor = "#0B4499";
        theme.backgroundColor = "#FFFFFF";
        theme.surfaceColor = "#F5F5F5";
        theme.textColor = "#111827";
        theme.createdAt = System.currentTimeMillis();
        return theme;
    }

    public static ThemeDefinition dark(String id, String projectId,
                                       String name) {
        ThemeDefinition theme = light(id, projectId, name);
        theme.darkMode = true;
        theme.primaryColor = "#90CAF9";
        theme.primaryDarkColor = "#64B5F6";
        theme.accentColor = "#90CAF9";
        theme.backgroundColor = "#121212";
        theme.surfaceColor = "#1E1E1E";
        theme.textColor = "#F5F5F5";
        return theme;
    }

    public ThemeModel toThemeModel() {
        ThemeModel model = new ThemeModel();
        model.setName(name);
        model.setPrimaryColor(primaryColor);
        model.setPrimaryDarkColor(primaryDarkColor);
        model.setAccentColor(accentColor);
        model.setBackgroundColor(backgroundColor);
        model.setSurfaceColor(surfaceColor);
        model.setTextColor(textColor);
        model.setDarkMode(darkMode);
        return model;
    }

    public static ThemeDefinition fromThemeModel(String id, String projectId,
                                                 ThemeModel model) {
        ThemeDefinition theme = new ThemeDefinition();
        theme.id = id;
        theme.projectId = projectId;
        theme.name = model.getName();
        theme.darkMode = model.isDarkMode();
        theme.primaryColor = model.getPrimaryColor();
        theme.primaryDarkColor = model.getPrimaryDarkColor();
        theme.accentColor = model.getAccentColor();
        theme.backgroundColor = model.getBackgroundColor();
        theme.surfaceColor = model.getSurfaceColor();
        theme.textColor = model.getTextColor();
        theme.createdAt = System.currentTimeMillis();
        return theme;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
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

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
