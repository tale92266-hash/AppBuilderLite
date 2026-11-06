package com.nobi.appbuilder.assets.resources;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Android resource names: lowercase letters, numbers and
 * underscores only, starting with a letter. Imported file names
 * are auto-sanitized; collisions get a numeric suffix.
 */
public final class ResourceNamingManager {

    private static final Pattern VALID_NAME =
            Pattern.compile("^[a-z][a-z0-9_]*$");

    private ResourceNamingManager() {
    }

    public static boolean isValidResourceName(String name) {
        return name != null && VALID_NAME.matcher(name).matches();
    }

    /** Turns any file name into a valid resource name. */
    public static String sanitize(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "resource";
        }
        String base = fileName;
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        base = base.toLowerCase(Locale.US).replaceAll("[^a-z0-9_]", "_");
        base = base.replaceAll("_+", "_");
        base = base.replaceAll("^_+|_+$", "");
        if (base.isEmpty()) {
            return "resource";
        }
        if (!Character.isLetter(base.charAt(0))) {
            base = "r_" + base;
        }
        return base;
    }

    /** Appends _2, _3… until the name is unused. */
    public static String uniqueName(String base, Set<String> taken) {
        if (taken == null || !taken.contains(base)) {
            return base;
        }
        int suffix = 2;
        while (taken.contains(base + "_" + suffix)) {
            suffix++;
        }
        return base + "_" + suffix;
    }

    public static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.US);
    }
}
