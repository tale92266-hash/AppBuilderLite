package com.nobi.appbuilder.assets.vector;

import java.util.ArrayList;
import java.util.List;

/** Validates a vector asset model before it is saved. */
public final class VectorValidator {

    private VectorValidator() {
    }

    public static List<String> validate(VectorAssetModel model) {
        List<String> errors = new ArrayList<>();
        if (model == null) {
            errors.add("Missing vector asset");
            return errors;
        }
        if (model.getName() == null || model.getName().trim().isEmpty()) {
            errors.add("Vector needs a name");
        }
        if (model.getViewportWidth() <= 0 || model.getViewportHeight() <= 0) {
            errors.add("Viewport width and height must be greater than 0");
        }
        if (model.getWidthDp() <= 0 || model.getHeightDp() <= 0) {
            errors.add("Width and height must be greater than 0");
        }
        if (model.getPaths() == null || model.getPaths().isEmpty()) {
            errors.add("Vector needs at least one path");
            return errors;
        }
        for (int i = 0; i < model.getPaths().size(); i++) {
            VectorPathModel path = model.getPaths().get(i);
            String error = VectorPathParser.describeError(path.getPathData());
            if (error != null) {
                errors.add("Path " + (i + 1) + ": " + error);
            }
            if (path.getFillColor() == null && path.getStrokeColor() == null) {
                errors.add("Path " + (i + 1)
                        + " needs a fill colour or a stroke colour");
            }
        }
        return errors;
    }
}
