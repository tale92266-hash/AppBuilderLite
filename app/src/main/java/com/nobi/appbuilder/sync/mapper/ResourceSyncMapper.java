package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.model.ThemeModel;
import com.nobi.appbuilder.sync.parser.XMLResourceResolver;

/**
 * Maps resource references between XML (@color/primary) and the IR
 * theme. Theme tokens resolve to the project's theme colors;
 * component-level explicit colors are never overwritten by a theme
 * change.
 */
public class ResourceSyncMapper {

    /** Resolves a @color/@string/@dimen reference, or null. */
    public static String resolveReference(String reference,
                                          XMLResourceResolver resolver) {
        if (resolver == null) {
            return null;
        }
        return resolver.resolve(reference);
    }

    /** Theme token name for a color value, or null when not a token. */
    public static String tokenForColor(String color, ThemeModel theme) {
        if (color == null || theme == null) {
            return null;
        }
        if (color.equalsIgnoreCase(theme.getPrimaryColor())) {
            return "primary";
        }
        if (color.equalsIgnoreCase(theme.getPrimaryDarkColor())) {
            return "primary_dark";
        }
        if (color.equalsIgnoreCase(theme.getAccentColor())) {
            return "accent";
        }
        if (color.equalsIgnoreCase(theme.getBackgroundColor())) {
            return "background";
        }
        if (color.equalsIgnoreCase(theme.getSurfaceColor())) {
            return "surface";
        }
        if (color.equalsIgnoreCase(theme.getTextColor())) {
            return "text";
        }
        return null;
    }

    /** Theme color for a token name, or null when not a token. */
    public static String colorForToken(String token, ThemeModel theme) {
        if (token == null || theme == null) {
            return null;
        }
        switch (token) {
            case "primary":
                return theme.getPrimaryColor();
            case "primary_dark":
                return theme.getPrimaryDarkColor();
            case "accent":
                return theme.getAccentColor();
            case "background":
                return theme.getBackgroundColor();
            case "surface":
                return theme.getSurfaceColor();
            case "text":
                return theme.getTextColor();
            default:
                return null;
        }
    }
}
