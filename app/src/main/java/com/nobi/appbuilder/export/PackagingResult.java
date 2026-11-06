package com.nobi.appbuilder.export;

import java.io.File;

/** Everything one export run produced, plus its report. */
public class PackagingResult {

    private final ProjectPackagingConfig config;
    private final PackagingReport report;
    private final File zipFile;
    private final String outputUri;
    private long exportHistoryId = -1;

    public PackagingResult(ProjectPackagingConfig config,
                           PackagingReport report, File zipFile,
                           String outputUri) {
        this.config = config;
        this.report = report;
        this.zipFile = zipFile;
        this.outputUri = outputUri;
    }

    public boolean isSuccess() {
        return report.isSuccess() && zipFile != null && zipFile.exists()
                && zipFile.length() > 0;
    }

    public ProjectPackagingConfig getConfig() {
        return config;
    }

    public PackagingReport getReport() {
        return report;
    }

    public File getZipFile() {
        return zipFile;
    }

    /** Content or file URI of the ZIP (for sharing / history). */
    public String getOutputUri() {
        return outputUri;
    }

    public long getExportHistoryId() {
        return exportHistoryId;
    }

    public void setExportHistoryId(long exportHistoryId) {
        this.exportHistoryId = exportHistoryId;
    }
}
