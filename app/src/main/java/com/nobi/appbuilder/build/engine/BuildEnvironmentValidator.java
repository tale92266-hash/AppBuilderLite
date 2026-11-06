package com.nobi.appbuilder.build.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Validates a user-configured Android SDK directory. */
public class BuildEnvironmentValidator {

    /** Problems; empty means the SDK directory looks usable. */
    public static List<String> validateSdkPath(String sdkPath,
                                               int targetSdk) {
        List<String> problems = new ArrayList<>();
        if (sdkPath == null || sdkPath.trim().isEmpty()) {
            problems.add("No SDK path configured");
            return problems;
        }
        File sdk = new File(sdkPath);
        if (!sdk.isDirectory()) {
            problems.add("SDK path is not a directory: " + sdkPath);
            return problems;
        }
        File platforms = new File(sdk, "platforms");
        if (!platforms.isDirectory()
                || platforms.list() == null
                || platforms.list().length == 0) {
            problems.add("No SDK platforms installed under " + sdkPath);
        } else if (!new File(platforms, "android-" + targetSdk)
                .isDirectory()) {
            problems.add("SDK platform android-" + targetSdk
                    + " is not installed");
        }
        File buildTools = new File(sdk, "build-tools");
        if (!buildTools.isDirectory()
                || buildTools.list() == null
                || buildTools.list().length == 0) {
            problems.add("No Android build-tools installed under " + sdkPath);
        }
        File licenses = new File(sdk, "licenses");
        if (!licenses.isDirectory()
                || licenses.list() == null
                || licenses.list().length == 0) {
            problems.add("Android SDK licenses are not accepted");
        }
        return problems;
    }
}
