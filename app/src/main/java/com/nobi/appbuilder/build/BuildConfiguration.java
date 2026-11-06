package com.nobi.appbuilder.build;

import java.util.ArrayList;
import java.util.List;

/** User-chosen build options, validated before any build starts. */
public class BuildConfiguration {

    public static final String TYPE_DEBUG = "debug";
    public static final String TYPE_RELEASE = "release";

    private String buildType = TYPE_DEBUG;
    private String applicationId;
    private String versionName = "1.0";
    private int versionCode = 1;
    private int minSdk = 26;
    private int targetSdk = 35;
    private boolean shrinkEnabled;
    private boolean resourceShrinkEnabled;
    private boolean includeReport = true;
    private boolean preserveLogs = true;
    /** Optional user-configured Android SDK path; null means auto-detect. */
    private String sdkPath;

    public String getBuildType() {
        return buildType;
    }

    public void setBuildType(String buildType) {
        this.buildType = buildType;
    }

    public boolean isRelease() {
        return TYPE_RELEASE.equals(buildType);
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
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

    public boolean isShrinkEnabled() {
        return shrinkEnabled;
    }

    public void setShrinkEnabled(boolean shrinkEnabled) {
        this.shrinkEnabled = shrinkEnabled;
    }

    public boolean isResourceShrinkEnabled() {
        return resourceShrinkEnabled;
    }

    public void setResourceShrinkEnabled(boolean resourceShrinkEnabled) {
        this.resourceShrinkEnabled = resourceShrinkEnabled;
    }

    public boolean isIncludeReport() {
        return includeReport;
    }

    public void setIncludeReport(boolean includeReport) {
        this.includeReport = includeReport;
    }

    public boolean isPreserveLogs() {
        return preserveLogs;
    }

    public void setPreserveLogs(boolean preserveLogs) {
        this.preserveLogs = preserveLogs;
    }

    public String getSdkPath() {
        return sdkPath;
    }

    public void setSdkPath(String sdkPath) {
        this.sdkPath = sdkPath;
    }

    /** The Gradle task for this configuration. */
    public String getGradleTask() {
        return isRelease() ? "assembleRelease" : "assembleDebug";
    }

    /** Problems; empty means the configuration is valid. */
    public List<String> validate() {
        List<String> problems = new ArrayList<>();
        if (!TYPE_DEBUG.equals(buildType) && !TYPE_RELEASE.equals(buildType)) {
            problems.add("Build type must be debug or release");
        }
        if (applicationId == null || applicationId.isEmpty()) {
            problems.add("Application ID is required");
        } else if (!applicationId.matches(
                "[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)+")) {
            problems.add("Application ID is not a valid package name");
        }
        if (versionName == null
                || !versionName.matches("^\\d+\\.\\d+(\\.\\d+)?$")) {
            problems.add("Version name must look like 1.0 or 1.0.0");
        }
        if (versionCode < 1) {
            problems.add("Version code must be at least 1");
        }
        if (minSdk < 21 || minSdk > 35) {
            problems.add("Minimum SDK must be between 21 and 35");
        }
        if (targetSdk < minSdk || targetSdk > 35) {
            problems.add("Target SDK must be between minimum SDK and 35");
        }
        if (resourceShrinkEnabled && !shrinkEnabled && isRelease()) {
            problems.add("Resource shrinking requires code shrinking");
        }
        return problems;
    }
}
