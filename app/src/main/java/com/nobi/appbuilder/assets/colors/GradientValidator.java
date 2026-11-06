package com.nobi.appbuilder.assets.colors;

import java.util.ArrayList;
import java.util.List;

/** Validates a gradient model before it is saved. */
public final class GradientValidator {

    private GradientValidator() {
    }

    public static List<String> validate(GradientModel gradient) {
        List<String> errors = new ArrayList<>();
        if (gradient == null) {
            errors.add("Missing gradient");
            return errors;
        }
        if (gradient.getName() == null || gradient.getName().trim().isEmpty()) {
            errors.add("Gradient needs a name");
        }
        String type = gradient.getType();
        if (type == null || (!"linear".equals(type) && !"radial".equals(type)
                && !"sweep".equals(type))) {
            errors.add("Gradient type must be linear, radial or sweep");
        }
        checkColor(errors, "Start", gradient.getStartColor());
        checkColor(errors, "End", gradient.getEndColor());
        if (gradient.getCenterColor() != null
                && !gradient.getCenterColor().isEmpty()) {
            checkColor(errors, "Center", gradient.getCenterColor());
        }
        if ("linear".equals(type)
                && (gradient.getAngle() < 0 || gradient.getAngle() > 315
                || gradient.getAngle() % 45 != 0)) {
            errors.add("Linear gradient angle must be a multiple of 45");
        }
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
