package com.nobi.appbuilder.export;

import java.util.ArrayList;
import java.util.List;

/**
 * The outcome of one packaging run: errors, warnings, file counts,
 * duration and the ZIP that was produced. A run with errors is never
 * reported as successful, even when a ZIP file exists.
 */
public class PackagingReport {

    private final List<PackagingError> errors = new ArrayList<>();
    private final List<PackagingError> warnings = new ArrayList<>();
    private int javaFiles;
    private int xmlFiles;
    private int resources;
    private int activities;
    private int totalFiles;
    private long durationMs;
    private long zipSizeBytes;
    private String zipFileName;

    public void addError(PackagingError error) {
        if (error.isError()) {
            errors.add(error);
        } else {
            warnings.add(error);
        }
    }

    public void addAll(List<PackagingError> problems) {
        for (PackagingError problem : problems) {
            addError(problem);
        }
    }

    public List<PackagingError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<PackagingError> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<PackagingError> getAllProblems() {
        List<PackagingError> all = new ArrayList<>(errors);
        all.addAll(warnings);
        return all;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public int getJavaFiles() {
        return javaFiles;
    }

    public void setJavaFiles(int javaFiles) {
        this.javaFiles = javaFiles;
    }

    public int getXmlFiles() {
        return xmlFiles;
    }

    public void setXmlFiles(int xmlFiles) {
        this.xmlFiles = xmlFiles;
    }

    public int getResources() {
        return resources;
    }

    public void setResources(int resources) {
        this.resources = resources;
    }

    public int getActivities() {
        return activities;
    }

    public void setActivities(int activities) {
        this.activities = activities;
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public void setTotalFiles(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(long durationMs) {
        this.durationMs = durationMs;
    }

    public long getZipSizeBytes() {
        return zipSizeBytes;
    }

    public void setZipSizeBytes(long zipSizeBytes) {
        this.zipSizeBytes = zipSizeBytes;
    }

    public String getZipFileName() {
        return zipFileName;
    }

    public void setZipFileName(String zipFileName) {
        this.zipFileName = zipFileName;
    }

    public String toText() {
        StringBuilder out = new StringBuilder();
        out.append(isSuccess() ? "Export succeeded" : "Export failed");
        out.append(" — ").append(totalFiles).append(" files (")
                .append(javaFiles).append(" Java, ")
                .append(xmlFiles).append(" XML, ")
                .append(resources).append(" resources, ")
                .append(activities).append(" activities), ")
                .append(durationMs).append(" ms.\n");
        if (zipFileName != null) {
            out.append("ZIP: ").append(zipFileName)
                    .append(" (").append(zipSizeBytes).append(" bytes).\n");
        }
        for (PackagingError problem : getAllProblems()) {
            out.append(problem.toString()).append("\n");
        }
        return out.toString().trim();
    }
}
