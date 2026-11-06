package com.nobi.appbuilder.build;

import com.nobi.appbuilder.build.diagnostics.BuildError;
import com.nobi.appbuilder.build.diagnostics.BuildWarning;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** The verified outcome of one build run. A SUCCESS result always
 * carries a real, verified APK file; anything else is not success. */
public class BuildResult {

    private final String buildId;
    private final String projectId;
    private final BuildConfiguration configuration;
    private BuildStatus status;
    private long startTimeMs;
    private long endTimeMs;
    private File apkFile;
    private String apkPackageName;
    private String apkVersionName;
    private int apkVersionCode;
    private int apkMinSdk;
    private String externalZipPath;
    private String reportText;
    private String logPath;
    private final List<BuildError> errors = new ArrayList<>();
    private final List<BuildWarning> warnings = new ArrayList<>();

    public BuildResult(String buildId, String projectId,
                       BuildConfiguration configuration) {
        this.buildId = buildId;
        this.projectId = projectId;
        this.configuration = configuration;
        this.status = BuildStatus.PENDING;
        this.startTimeMs = System.currentTimeMillis();
    }

    public String getBuildId() {
        return buildId;
    }

    public String getProjectId() {
        return projectId;
    }

    public BuildConfiguration getConfiguration() {
        return configuration;
    }

    public BuildStatus getStatus() {
        return status;
    }

    public void setStatus(BuildStatus status) {
        this.status = status;
        if (status.isTerminal()) {
            endTimeMs = System.currentTimeMillis();
        }
    }

    public long getStartTimeMs() {
        return startTimeMs;
    }

    public long getEndTimeMs() {
        return endTimeMs;
    }

    public long getDurationMs() {
        return (endTimeMs > 0 ? endTimeMs : System.currentTimeMillis())
                - startTimeMs;
    }

    public File getApkFile() {
        return apkFile;
    }

    public void setApkFile(File apkFile) {
        this.apkFile = apkFile;
    }

    public String getApkPackageName() {
        return apkPackageName;
    }

    public void setApkPackageName(String apkPackageName) {
        this.apkPackageName = apkPackageName;
    }

    public String getApkVersionName() {
        return apkVersionName;
    }

    public void setApkVersionName(String apkVersionName) {
        this.apkVersionName = apkVersionName;
    }

    public int getApkVersionCode() {
        return apkVersionCode;
    }

    public void setApkVersionCode(int apkVersionCode) {
        this.apkVersionCode = apkVersionCode;
    }

    public int getApkMinSdk() {
        return apkMinSdk;
    }

    public void setApkMinSdk(int apkMinSdk) {
        this.apkMinSdk = apkMinSdk;
    }

    public String getExternalZipPath() {
        return externalZipPath;
    }

    public void setExternalZipPath(String externalZipPath) {
        this.externalZipPath = externalZipPath;
    }

    public String getReportText() {
        return reportText;
    }

    public void setReportText(String reportText) {
        this.reportText = reportText;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public List<BuildError> getErrors() {
        return errors;
    }

    public List<BuildWarning> getWarnings() {
        return warnings;
    }

    public int getErrorCount() {
        return errors.size();
    }

    public int getWarningCount() {
        return warnings.size();
    }
}
