package com.nobi.appbuilder.build;

import java.util.ArrayList;
import java.util.List;

/** Structured result of environment detection. */
public class BuildEnvironmentInfo {

    private BuildCapability capability = BuildCapability.ENVIRONMENT_UNSUPPORTED;
    private boolean javaFound;
    private boolean javacFound;
    private boolean gradleFound;
    private String javaVersion;
    private String gradleVersion;
    private String sdkPath;
    private boolean platformFound;
    private boolean buildToolsFound;
    private boolean aapt2Found;
    private boolean licensesOk;
    private long freeSpaceBytes;
    private final List<String> messages = new ArrayList<>();

    public BuildCapability getCapability() {
        return capability;
    }

    public void setCapability(BuildCapability capability) {
        this.capability = capability;
    }

    public boolean isJavaFound() {
        return javaFound;
    }

    public void setJavaFound(boolean javaFound) {
        this.javaFound = javaFound;
    }

    public boolean isJavacFound() {
        return javacFound;
    }

    public void setJavacFound(boolean javacFound) {
        this.javacFound = javacFound;
    }

    public boolean isGradleFound() {
        return gradleFound;
    }

    public void setGradleFound(boolean gradleFound) {
        this.gradleFound = gradleFound;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getGradleVersion() {
        return gradleVersion;
    }

    public void setGradleVersion(String gradleVersion) {
        this.gradleVersion = gradleVersion;
    }

    public String getSdkPath() {
        return sdkPath;
    }

    public void setSdkPath(String sdkPath) {
        this.sdkPath = sdkPath;
    }

    public boolean isPlatformFound() {
        return platformFound;
    }

    public void setPlatformFound(boolean platformFound) {
        this.platformFound = platformFound;
    }

    public boolean isBuildToolsFound() {
        return buildToolsFound;
    }

    public void setBuildToolsFound(boolean buildToolsFound) {
        this.buildToolsFound = buildToolsFound;
    }

    public boolean isAapt2Found() {
        return aapt2Found;
    }

    public void setAapt2Found(boolean aapt2Found) {
        this.aapt2Found = aapt2Found;
    }

    public boolean isLicensesOk() {
        return licensesOk;
    }

    public void setLicensesOk(boolean licensesOk) {
        this.licensesOk = licensesOk;
    }

    public long getFreeSpaceBytes() {
        return freeSpaceBytes;
    }

    public void setFreeSpaceBytes(long freeSpaceBytes) {
        this.freeSpaceBytes = freeSpaceBytes;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        messages.add(message);
    }

    /** One-line summary for the UI. */
    public String getSummary() {
        switch (capability) {
            case LOCAL_BUILD_SUPPORTED:
                return "Local build supported (Gradle "
                        + (gradleVersion == null ? "?" : gradleVersion)
                        + ", SDK at " + sdkPath + ")";
            case EXTERNAL_BUILD_REQUIRED:
                return "No local build tools found — export the project "
                        + "ZIP and build it in Android Studio";
            case ENVIRONMENT_INCOMPLETE:
                return "Build environment incomplete — "
                        + String.join("; ", messages);
            default:
                return "Local builds are not supported on this device";
        }
    }
}
