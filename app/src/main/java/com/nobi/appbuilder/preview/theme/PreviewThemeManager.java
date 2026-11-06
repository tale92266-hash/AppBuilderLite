package com.nobi.appbuilder.preview.theme;

import android.view.View;

import com.nobi.appbuilder.model.ThemeModel;

/**
 * Light / dark / project-theme switching for preview. The project
 * theme is never written to: switching only changes which ThemeModel
 * the resolvers read, so a component's own colours always win over
 * the theme and the original design is untouched.
 */
public class PreviewThemeManager {

    public static final int MODE_LIGHT = 0;
    public static final int MODE_DARK = 1;
    public static final int MODE_CUSTOM = 2;

    private static final String LIGHT_BACKGROUND = "#FFFFFF";
    private static final String LIGHT_SURFACE = "#F5F5F5";
    private static final String LIGHT_TEXT = "#111827";
    private static final String LIGHT_PRIMARY = "#0B4499";

    private static final String DARK_BACKGROUND = "#121212";
    private static final String DARK_SURFACE = "#1E1E1E";
    private static final String DARK_TEXT = "#F5F5F5";
    private static final String DARK_PRIMARY = "#90CAF9";

    private ThemeModel projectTheme;
    private int mode = MODE_CUSTOM;

    public PreviewThemeManager(ThemeModel projectTheme) {
        this.projectTheme = projectTheme;
    }

    public void setProjectTheme(ThemeModel projectTheme) {
        this.projectTheme = projectTheme;
    }

    public ThemeModel getProjectTheme() {
        return projectTheme;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        if (mode == MODE_LIGHT || mode == MODE_DARK || mode == MODE_CUSTOM) {
            this.mode = mode;
        }
    }

    public boolean isDarkMode() {
        if (mode == MODE_DARK) {
            return true;
        }
        if (mode == MODE_LIGHT) {
            return false;
        }
        return projectTheme != null && projectTheme.isDarkMode();
    }

    public String getModeName() {
        switch (mode) {
            case MODE_LIGHT:
                return "Light";
            case MODE_DARK:
                return "Dark";
            case MODE_CUSTOM:
            default:
                return "Project theme";
        }
    }

    /** The theme the resolvers actually read for the current mode. */
    public ThemeModel getEffectiveTheme() {
        if (mode == MODE_CUSTOM && projectTheme != null) {
            return projectTheme;
        }
        ThemeModel theme = new ThemeModel();
        if (mode == MODE_DARK) {
            theme.setName("Dark");
            theme.setDarkMode(true);
            theme.setBackgroundColor(DARK_BACKGROUND);
            theme.setSurfaceColor(DARK_SURFACE);
            theme.setTextColor(DARK_TEXT);
            theme.setPrimaryColor(DARK_PRIMARY);
            theme.setAccentColor(DARK_PRIMARY);
        } else {
            theme.setName("Light");
            theme.setDarkMode(false);
            theme.setBackgroundColor(LIGHT_BACKGROUND);
            theme.setSurfaceColor(LIGHT_SURFACE);
            theme.setTextColor(LIGHT_TEXT);
            theme.setPrimaryColor(LIGHT_PRIMARY);
            theme.setAccentColor(LIGHT_PRIMARY);
        }
        if (projectTheme != null) {
            theme.setFontFamily(projectTheme.getFontFamily());
        }
        return theme;
    }

    public int getBackgroundColor() {
        return PreviewColorResolver.parse(
                getEffectiveTheme().getBackgroundColor(), 0xFFFFFFFF);
    }

    public int getSurfaceColor() {
        return PreviewColorResolver.parse(
                getEffectiveTheme().getSurfaceColor(), 0xFFF5F5F5);
    }

    public int getTextColor() {
        return PreviewColorResolver.parse(
                getEffectiveTheme().getTextColor(), 0xFF111827);
    }

    public int getPrimaryColor() {
        return PreviewColorResolver.parse(
                getEffectiveTheme().getPrimaryColor(), 0xFF0B4499);
    }

    /** Paints the preview root with the theme background. */
    public void applyRootBackground(View root) {
        if (root != null) {
            root.setBackgroundColor(getBackgroundColor());
        }
    }
}
