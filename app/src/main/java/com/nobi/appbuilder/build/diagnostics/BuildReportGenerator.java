package com.nobi.appbuilder.build.diagnostics;

import com.nobi.appbuilder.build.BuildEnvironmentInfo;
import com.nobi.appbuilder.build.BuildResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/** Per-build TXT report: project, config, environment, timings,
 * result, APK metadata, errors and warnings. Never includes signing
 * credentials or passwords. */
public class BuildReportGenerator {

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.US);

    public static String generate(BuildResult result,
                                  BuildEnvironmentInfo environment,
                                  String projectName) {
        StringBuilder out = new StringBuilder();
        out.append("AppBuilder Lite — Build Report\n");
        out.append("================================\n\n");
        out.append("Project: ").append(projectName).append("\n");
        out.append("Build ID: ").append(result.getBuildId()).append("\n");
        out.append("Build type: ")
                .append(result.getConfiguration().getBuildType())
                .append("\n");
        out.append("Application ID: ")
                .append(result.getConfiguration().getApplicationId())
                .append("\n");
        out.append("Version: ")
                .append(result.getConfiguration().getVersionName())
                .append(" (").append(result.getConfiguration().getVersionCode())
                .append(")\n");
        out.append("Min SDK: ").append(result.getConfiguration().getMinSdk())
                .append("  Target SDK: ")
                .append(result.getConfiguration().getTargetSdk()).append("\n");
        out.append("Gradle task: ")
                .append(result.getConfiguration().getGradleTask())
                .append("\n\n");
        if (environment != null) {
            out.append("Environment: ").append(environment.getSummary())
                    .append("\n");
            for (String message : environment.getMessages()) {
                out.append("  - ").append(message).append("\n");
            }
            out.append("\n");
        }
        out.append("Started: ")
                .append(DATE_FORMAT.format(new Date(result.getStartTimeMs())))
                .append("\n");
        if (result.getEndTimeMs() > 0) {
            out.append("Finished: ")
                    .append(DATE_FORMAT.format(
                            new Date(result.getEndTimeMs()))).append("\n");
        }
        out.append("Duration: ").append(result.getDurationMs())
                .append(" ms\n");
        out.append("Result: ").append(result.getStatus()).append("\n\n");
        if (result.getApkFile() != null) {
            out.append("APK: ").append(result.getApkFile().getAbsolutePath())
                    .append("\n");
            out.append("APK size: ").append(result.getApkFile().length())
                    .append(" bytes\n");
            out.append("APK package: ").append(result.getApkPackageName())
                    .append("\n");
            out.append("APK version: ").append(result.getApkVersionName())
                    .append(" (").append(result.getApkVersionCode())
                    .append(")\n");
            out.append("APK min SDK: ").append(result.getApkMinSdk())
                    .append("\n\n");
        }
        if (result.getExternalZipPath() != null) {
            out.append("Exported project ZIP: ")
                    .append(result.getExternalZipPath()).append("\n");
            out.append("Build the APK in Android Studio with:\n");
            out.append("  ./gradlew assemble")
                    .append(result.getConfiguration().isRelease()
                            ? "Release" : "Debug").append("\n\n");
        }
        out.append("Errors (").append(result.getErrorCount()).append("):\n");
        for (BuildError error : result.getErrors()) {
            out.append("  - ").append(error.toString()).append("\n");
            out.append("    Suggestion: ").append(error.getSuggestion())
                    .append("\n");
        }
        out.append("\nWarnings (").append(result.getWarningCount())
                .append("):\n");
        for (BuildWarning warning : result.getWarnings()) {
            out.append("  - ").append(warning.toString()).append("\n");
        }
        return out.toString();
    }
}
