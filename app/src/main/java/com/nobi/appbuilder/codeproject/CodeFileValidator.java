package com.nobi.appbuilder.codeproject;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Guards file operations: canonical-path validation against the
 * workspace, Android resource-name rules, and protection for
 * essential project files.
 */
public class CodeFileValidator {

    /** Files that must never be deleted (editing is allowed where
     * safe; the wrapper jar is binary and read-only). */
    public static final Set<String> PROTECTED_FILES = new HashSet<>(
            Arrays.asList("settings.gradle", "build.gradle",
                    "gradle.properties", "AndroidManifest.xml",
                    "gradle-wrapper.properties", "gradle-wrapper.jar",
                    "gradlew", "gradlew.bat"));

    public static boolean isPathInside(File root, File file) {
        try {
            String rootPath = root.getCanonicalPath();
            return file.getCanonicalPath().startsWith(rootPath);
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isProtected(File file) {
        return PROTECTED_FILES.contains(file.getName());
    }

    public static boolean isDeletable(File file) {
        return !isProtected(file);
    }

    /** Android resource-name rules: lowercase, digits, underscore. */
    public static boolean isValidResourceName(String name) {
        return name != null && name.matches("[a-z][a-z0-9_]*");
    }

    public static boolean isValidFileName(String name) {
        return name != null && !name.isEmpty()
                && !name.contains("/") && !name.contains("\\")
                && !name.contains("..") && !name.contains("\0");
    }

    public static boolean isTextFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".java") || name.endsWith(".xml")
                || name.endsWith(".gradle") || name.endsWith(".properties")
                || name.endsWith(".txt") || name.endsWith(".md")
                || name.endsWith(".pro");
    }
}
