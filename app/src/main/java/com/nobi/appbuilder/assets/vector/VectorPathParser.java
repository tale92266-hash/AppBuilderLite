package com.nobi.appbuilder.assets.vector;

import java.util.regex.Pattern;

/**
 * Validates VectorDrawable path data. Only the path commands
 * Android supports are accepted; arbitrary SVG is NOT treated as
 * a valid VectorDrawable.
 */
public final class VectorPathParser {

    private static final Pattern PATH_DATA = Pattern.compile(
            "^[MmLlHhVvCcSsQqTtAaZz0-9\\s.,\\-eE+]*$");

    private VectorPathParser() {
    }

    public static boolean isValidPathData(String pathData) {
        if (pathData == null || pathData.trim().isEmpty()) {
            return false;
        }
        if (!PATH_DATA.matcher(pathData.trim()).matches()) {
            return false;
        }
        return pathData.trim().matches(".*[Mm].*");
    }

    public static String describeError(String pathData) {
        if (pathData == null || pathData.trim().isEmpty()) {
            return "Path data is empty";
        }
        if (!PATH_DATA.matcher(pathData.trim()).matches()) {
            return "Path data contains unsupported characters or commands";
        }
        if (!pathData.trim().matches(".*[Mm].*")) {
            return "Path data must start with a move (M) command";
        }
        return null;
    }
}
