package com.nobi.appbuilder.build.diagnostics;

/** Practical corrective guidance per failure category. Suggestions
 * are guidance only — nothing is auto-fixed or claimed fixed. */
public class BuildSuggestionEngine {

    public static String suggestionFor(String category) {
        switch (category) {
            case BuildIssueClassifier.GRADLE_VERSION_MISMATCH:
                return "Update the Gradle wrapper to a version compatible "
                        + "with the installed JDK (Gradle 8.11.1 needs JDK 17).";
            case BuildIssueClassifier.MISSING_SDK:
                return "Configure the Android SDK location (ANDROID_HOME) "
                        + "or set the SDK path in the build settings.";
            case BuildIssueClassifier.MISSING_PLATFORM:
                return "Install the required SDK platform (android-"
                        + "targetSdk) in Android Studio's SDK Manager.";
            case BuildIssueClassifier.AAPT2_FAILURE:
                return "Inspect the resource XML files and the installed "
                        + "build-tools version; a malformed layout or "
                        + "drawable usually causes this.";
            case BuildIssueClassifier.MISSING_RESOURCE:
                return "Verify the generated resource references — a "
                        + "missing @string/@color/@drawable or a wrong R.id "
                        + "in the generated Java.";
            case BuildIssueClassifier.JAVA_COMPILATION:
                return "Inspect the Java compiler output above; the file "
                        + "and line are shown with the error.";
            case BuildIssueClassifier.DEPENDENCY_FAILURE:
                return "Check the repository configuration and the network "
                        + "connection; Gradle needs to download dependencies "
                        + "on the first build.";
            case BuildIssueClassifier.DUPLICATE_RESOURCE:
                return "Remove the conflicting resource definitions — two "
                        + "resources share the same name or type.";
            case BuildIssueClassifier.MANIFEST_MERGER:
                return "Inspect the conflicting manifest declarations "
                        + "(application, activities, permissions) in the "
                        + "generated manifest.";
            case BuildIssueClassifier.INSUFFICIENT_STORAGE:
                return "Free storage on the device and retry the build.";
            default:
                return "Read the full build log around this error; the "
                        + "underlying cause is shown there.";
        }
    }
}
