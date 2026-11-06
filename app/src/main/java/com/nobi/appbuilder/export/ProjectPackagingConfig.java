package com.nobi.appbuilder.export;

import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Export configuration: everything the packaging engine needs that
 * the user can change, without touching the original project unless
 * they explicitly save it. Defaults come from the project and its
 * settings; validate() blocks invalid configurations from starting.
 */
public class ProjectPackagingConfig {

    private String projectName;
    private String appName;
    private String packageName;
    private int minSdk;
    private int targetSdk;
    private String versionName;
    private int versionCode;
    private String description;
    /** SAF tree URI string for a custom output directory, or null. */
    private String outputTreeUri;
    /** Include the genuine Gradle wrapper (jar + scripts). */
    private boolean includeWrapper = true;

    public static ProjectPackagingConfig defaultsFor(ProjectModel project,
                                                     ProjectSettingsManager settings) {
        ProjectPackagingConfig config = new ProjectPackagingConfig();
        String projectId = project.getId();
        config.projectName = project.getAppName();
        config.appName = project.getAppName();
        config.packageName = project.getPackageName();
        config.minSdk = project.getMinSdk();
        config.targetSdk = parseInt(settings.getSetting(projectId,
                ProjectSettingsManager.KEY_TARGET_SDK, "35"), 35);
        config.versionName = settings.getSetting(projectId,
                ProjectSettingsManager.KEY_VERSION_NAME, "1.0");
        config.versionCode = parseInt(settings.getSetting(projectId,
                ProjectSettingsManager.KEY_VERSION_CODE, "1"), 1);
        config.description = "";
        return config;
    }

    private static int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    /** Returns blocking problems; empty means the config can proceed. */
    public List<String> validate() {
        List<String> problems = new ArrayList<>();
        if (projectName == null || projectName.trim().isEmpty()) {
            problems.add("Project name is required");
        } else if (projectName.trim().length() > 100) {
            problems.add("Project name must be 100 characters or fewer");
        }
        if (appName == null || appName.trim().isEmpty()) {
            problems.add("Application name is required");
        }
        if (!isValidPackageName(packageName)) {
            problems.add("Invalid package name: " + packageName);
        }
        if (minSdk < 21 || minSdk > 35) {
            problems.add("Minimum SDK must be between 21 and 35");
        }
        if (targetSdk < minSdk || targetSdk > 35) {
            problems.add("Target SDK must be between minimum SDK and 35");
        }
        if (versionName == null || versionName.trim().isEmpty()
                || !versionName.trim().matches("^\\d+\\.\\d+(\\.\\d+)?$")) {
            problems.add("Version name must look like 1.0 or 1.0.0");
        }
        if (versionCode < 1) {
            problems.add("Version code must be 1 or greater");
        }
        return problems;
    }

    /** Android package rules: dot-separated Java identifiers. */
    public static boolean isValidPackageName(String packageName) {
        if (packageName == null || packageName.isEmpty()) {
            return false;
        }
        String[] parts = packageName.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        for (String part : parts) {
            if (!part.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                return false;
            }
            if ("class".equals(part) || "int".equals(part)
                    || "void".equals(part)) {
                return false;
            }
        }
        return true;
    }

    /** File-safe form of the project name for the ZIP file. */
    public String zipBaseName() {
        String base = projectName == null ? "project" : projectName.trim();
        base = base.replaceAll("[^A-Za-z0-9._-]+", "_");
        if (base.isEmpty()) {
            base = "project";
        }
        return base;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public int getTargetSdk() {
        return targetSdk;
    }

    public void setTargetSdk(int targetSdk) {
        this.targetSdk = targetSdk;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOutputTreeUri() {
        return outputTreeUri;
    }

    public void setOutputTreeUri(String outputTreeUri) {
        this.outputTreeUri = outputTreeUri;
    }

    public boolean isIncludeWrapper() {
        return includeWrapper;
    }

    public void setIncludeWrapper(boolean includeWrapper) {
        this.includeWrapper = includeWrapper;
    }
}
