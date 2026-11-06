package com.nobi.appbuilder.build;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StatFs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Detects whether this Android device can genuinely compile Android
 * projects. It never assumes Termux, Android Studio, or any fixed
 * path: it scans PATH for executables, honours JAVA_HOME /
 * ANDROID_HOME / ANDROID_SDK_ROOT, and accepts a user-configured SDK
 * path from preferences. On a normal phone every check fails and the
 * answer is honestly EXTERNAL_BUILD_REQUIRED — never a fake local
 * build.
 */
public class BuildEnvironmentDetector {

    public static final String PREFS_BUILD = "build_prefs";
    public static final String KEY_SDK_PATH = "sdk_path";

    private final Context appContext;

    public BuildEnvironmentDetector(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public BuildEnvironmentInfo detect(int targetSdk) {
        BuildEnvironmentInfo info = new BuildEnvironmentInfo();
        boolean anyTool = false;

        File java = findOnPath("java");
        if (java != null) {
            info.setJavaFound(true);
            anyTool = true;
            info.setJavaVersion(versionOf(java, "--version"));
        } else {
            info.addMessage("No Java runtime found on PATH");
        }
        File javac = findOnPath("javac");
        if (javac != null) {
            info.setJavacFound(true);
        } else {
            info.addMessage("No JDK compiler (javac) found");
        }
        File gradle = findOnPath("gradle");
        if (gradle != null) {
            info.setGradleFound(true);
            anyTool = true;
            info.setGradleVersion(versionOf(gradle, "--version"));
        } else {
            info.addMessage("No Gradle found on PATH");
        }

        String sdkPath = configuredSdkPath();
        if (sdkPath != null) {
            info.setSdkPath(sdkPath);
            File sdk = new File(sdkPath);
            File platform = new File(sdk,
                    "platforms/android-" + targetSdk);
            info.setPlatformFound(platform.isDirectory());
            if (!info.isPlatformFound()) {
                info.addMessage("SDK platform android-" + targetSdk
                        + " is not installed");
            }
            File buildTools = new File(sdk, "build-tools");
            info.setBuildToolsFound(buildTools.isDirectory()
                    && buildTools.list() != null
                    && buildTools.list().length > 0);
            if (info.isBuildToolsFound()) {
                String latest = latestBuildTools(buildTools);
                File aapt2 = new File(buildTools, latest + "/aapt2");
                info.setAapt2Found(aapt2.isFile() && aapt2.canExecute());
                if (!info.isAapt2Found()) {
                    info.addMessage("AAPT2 not found in build-tools");
                }
            } else {
                info.addMessage("Android build-tools are not installed");
            }
            File licenses = new File(sdk, "licenses");
            info.setLicensesOk(licenses.isDirectory()
                    && licenses.list() != null
                    && licenses.list().length > 0);
            if (!info.isLicensesOk()) {
                info.addMessage("Android SDK licenses are not accepted");
            }
        } else {
            info.addMessage("No Android SDK found "
                    + "(ANDROID_HOME / ANDROID_SDK_ROOT / configured path)");
        }

        try {
            StatFs stat = new StatFs(
                    appContext.getCacheDir().getAbsolutePath());
            info.setFreeSpaceBytes(stat.getAvailableBytes());
            if (info.getFreeSpaceBytes() < 500L * 1024 * 1024) {
                info.addMessage("Less than 500 MB free storage");
            }
        } catch (Exception e) {
            info.addMessage("Could not check free storage");
        }

        if (info.isJavaFound() && info.isJavacFound()
                && info.isGradleFound() && info.getSdkPath() != null
                && info.isPlatformFound() && info.isBuildToolsFound()
                && info.isAapt2Found() && info.isLicensesOk()
                && info.getFreeSpaceBytes() >= 500L * 1024 * 1024) {
            info.setCapability(BuildCapability.LOCAL_BUILD_SUPPORTED);
        } else if (!anyTool && info.getSdkPath() == null) {
            info.setCapability(BuildCapability.EXTERNAL_BUILD_REQUIRED);
        } else if (anyTool || info.getSdkPath() != null) {
            info.setCapability(BuildCapability.ENVIRONMENT_INCOMPLETE);
        } else {
            info.setCapability(BuildCapability.ENVIRONMENT_UNSUPPORTED);
        }
        return info;
    }

    private String configuredSdkPath() {
        SharedPreferences prefs = appContext.getSharedPreferences(
                PREFS_BUILD, Context.MODE_PRIVATE);
        String configured = prefs.getString(KEY_SDK_PATH, null);
        if (configured != null && !configured.trim().isEmpty()
                && new File(configured).isDirectory()) {
            return configured;
        }
        String home = System.getenv("ANDROID_HOME");
        if (home != null && new File(home).isDirectory()) {
            return home;
        }
        String root = System.getenv("ANDROID_SDK_ROOT");
        if (root != null && new File(root).isDirectory()) {
            return root;
        }
        return null;
    }

    private String latestBuildTools(File buildToolsDir) {
        String[] versions = buildToolsDir.list();
        if (versions == null || versions.length == 0) {
            return "";
        }
        java.util.Arrays.sort(versions);
        return versions[versions.length - 1];
    }

    /** First executable with this name on PATH, or null. */
    private File findOnPath(String name) {
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        for (String dir : path.split(":")) {
            File candidate = new File(dir, name);
            if (candidate.isFile() && candidate.canExecute()) {
                return candidate;
            }
        }
        return null;
    }

    /** Runs "<tool> <arg>" briefly and returns the first output line. */
    private String versionOf(File tool, String arg) {
        try {
            Process process = new ProcessBuilder(
                    tool.getAbsolutePath(), arg)
                    .redirectErrorStream(true).start();
            byte[] buffer = new byte[512];
            int read = process.getInputStream().read(buffer);
            process.destroy();
            if (read > 0) {
                String line = new String(buffer, 0, read,
                        java.nio.charset.StandardCharsets.UTF_8);
                int newline = line.indexOf('\n');
                return (newline >= 0 ? line.substring(0, newline) : line)
                        .trim();
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public static List<String> pathEntries() {
        List<String> out = new ArrayList<>();
        String path = System.getenv("PATH");
        if (path != null) {
            for (String dir : path.split(":")) {
                out.add(dir);
            }
        }
        return out;
    }
}
