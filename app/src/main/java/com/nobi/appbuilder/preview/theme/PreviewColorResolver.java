package com.nobi.appbuilder.preview.theme;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.model.ThemeModel;

/**
 * Colour parsing for preview: plain hex values plus the theme tokens
 * a design can reference instead of a literal colour. Anything
 * unparseable falls back instead of crashing the render.
 */
public final class PreviewColorResolver {

    private PreviewColorResolver() {
    }

    /** Parses "#RRGGBB" / "#AARRGGBB", falling back on bad input. */
    public static int parse(String value, int fallback) {
        if (value == null) {
            return fallback;
        }
        return ComponentDefinition.parseColor(value, fallback);
    }

    /**
     * Resolves a colour reference: hex, a theme token
     * ("primary", "accent", "background", "surface", "text" or the
     * matching ThemeModel field names), or "@color/&lt;token&gt;".
     */
    public static int resolveToken(String value, ThemeModel theme, int fallback) {
        if (value == null || theme == null) {
            return parse(value, fallback);
        }
        String token = value.trim();
        if (token.startsWith("@color/")) {
            token = token.substring("@color/".length());
        }
        switch (token) {
            case "primary":
            case "primaryColor":
                return parse(theme.getPrimaryColor(), fallback);
            case "primaryDark":
            case "primaryDarkColor":
                return parse(theme.getPrimaryDarkColor(), fallback);
            case "accent":
            case "accentColor":
                return parse(theme.getAccentColor(), fallback);
            case "background":
            case "backgroundColor":
                return parse(theme.getBackgroundColor(), fallback);
            case "surface":
            case "surfaceColor":
                return parse(theme.getSurfaceColor(), fallback);
            case "text":
            case "textColor":
                return parse(theme.getTextColor(), fallback);
            default:
                return parse(token, fallback);
        }
    }

    /** Relative luminance test, used to pick readable defaults. */
    public static boolean isLight(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        double luminance = 0.2126 * r + 0.7152 * g + 0.0722 * b;
        return luminance > 140;
    }
}
