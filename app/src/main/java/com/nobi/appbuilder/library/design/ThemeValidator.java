package com.nobi.appbuilder.library.design;

import java.util.ArrayList;
import java.util.List;

/** Validates a theme definition before it is saved or applied. */
public final class ThemeValidator {

    private ThemeValidator() {
    }

    public static List<String> validate(ThemeDefinition theme) {
        List<String> errors = new ArrayList<>();
        if (theme == null) {
            errors.add("Missing theme");
            return errors;
        }
        if (theme.getName() == null || theme.getName().trim().isEmpty()) {
            errors.add("Theme needs a name");
        }
        checkColor(errors, "Primary", theme.getPrimaryColor());
        checkColor(errors, "Primary dark", theme.getPrimaryDarkColor());
        checkColor(errors, "Accent", theme.getAccentColor());
        checkColor(errors, "Background", theme.getBackgroundColor());
        checkColor(errors, "Surface", theme.getSurfaceColor());
        checkColor(errors, "Text", theme.getTextColor());
        return errors;
    }

    private static void checkColor(List<String> errors, String label,
                                   String value) {
        if (value == null || value.isEmpty()) {
            errors.add(label + " colour is missing");
            return;
        }
        try {
            android.graphics.Color.parseColor(value);
        } catch (IllegalArgumentException e) {
            errors.add(label + " colour is not a valid hex colour: " + value);
        }
    }
}
