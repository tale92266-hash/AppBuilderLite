package com.nobi.appbuilder.build.diagnostics;

import java.util.regex.Pattern;

/** Classifies a log line into one of the known failure categories.
 * Unknown lines stay "unknown" — never guessed. */
public class BuildIssueClassifier {

    public static final String GRADLE_VERSION_MISMATCH = "Gradle version mismatch";
    public static final String MISSING_SDK = "Missing Android SDK";
    public static final String MISSING_PLATFORM = "Missing platform";
    public static final String AAPT2_FAILURE = "AAPT2 failure";
    public static final String MISSING_RESOURCE = "Missing resource ID";
    public static final String JAVA_COMPILATION = "Java compilation failure";
    public static final String DEPENDENCY_FAILURE = "Dependency resolution failure";
    public static final String DUPLICATE_RESOURCE = "Duplicate resource";
    public static final String MANIFEST_MERGER = "Manifest merger failure";
    public static final String INSUFFICIENT_STORAGE = "Insufficient storage";
    public static final String UNKNOWN = "Unknown build error";

    private static final Pattern GRADLE_VERSION =
            Pattern.compile("(?i)(Unsupported class file major version|"
                    + "Gradle version|Could not determine java version)");
    private static final Pattern SDK =
            Pattern.compile("(?i)(SDK location not found|ANDROID_HOME|"
                    + "Android SDK not found|sdk\\.dir)");
    private static final Pattern PLATFORM =
            Pattern.compile("(?i)(Failed to find target with hash string|"
                    + "platform.*not (found|installed)|android-\\d+.*not found)");
    private static final Pattern AAPT2 =
            Pattern.compile("(?i)(AAPT2|aapt2.*failed|Failed to execute aapt)");
    private static final Pattern RESOURCE =
            Pattern.compile("(?i)(error: cannot find symbol.*R\\.|"
                    + "resource .* not found|@.*not found|"
                    + "No resource found that matches)");
    private static final Pattern JAVA =
            Pattern.compile("(?i)(error: .*\\.java|; error:|cannot find symbol|"
                    + "package .* does not exist|incompatible types)");
    private static final Pattern DEPENDENCY =
            Pattern.compile("(?i)(Could not resolve|Could not download|"
                    + "Failed to resolve|offline mode|No cached version)");
    private static final Pattern DUPLICATE =
            Pattern.compile("(?i)(Duplicate (class|resource)|already defined)");
    private static final Pattern MANIFEST =
            Pattern.compile("(?i)(Manifest merger failed|manifest merger)");
    private static final Pattern STORAGE =
            Pattern.compile("(?i)(No space left on device|out of space|"
                    + "insufficient.*storage|ENOSPC)");

    public static String classify(String line) {
        if (line == null) {
            return UNKNOWN;
        }
        if (STORAGE.matcher(line).find()) {
            return INSUFFICIENT_STORAGE;
        }
        if (MANIFEST.matcher(line).find()) {
            return MANIFEST_MERGER;
        }
        if (DUPLICATE.matcher(line).find()) {
            return DUPLICATE_RESOURCE;
        }
        if (DEPENDENCY.matcher(line).find()) {
            return DEPENDENCY_FAILURE;
        }
        if (AAPT2.matcher(line).find()) {
            return AAPT2_FAILURE;
        }
        if (RESOURCE.matcher(line).find()) {
            return MISSING_RESOURCE;
        }
        if (PLATFORM.matcher(line).find()) {
            return MISSING_PLATFORM;
        }
        if (SDK.matcher(line).find()) {
            return MISSING_SDK;
        }
        if (GRADLE_VERSION.matcher(line).find()) {
            return GRADLE_VERSION_MISMATCH;
        }
        if (JAVA.matcher(line).find()) {
            return JAVA_COMPILATION;
        }
        return UNKNOWN;
    }
}
