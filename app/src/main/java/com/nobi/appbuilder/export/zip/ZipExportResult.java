package com.nobi.appbuilder.export.zip;

import java.io.File;

/** The outcome of one ZIP creation run. */
public class ZipExportResult {

    private final File zipFile;
    private final int entryCount;
    private final long bytesWritten;
    private final long durationMs;
    private final boolean verified;

    public ZipExportResult(File zipFile, int entryCount, long bytesWritten,
                           long durationMs, boolean verified) {
        this.zipFile = zipFile;
        this.entryCount = entryCount;
        this.bytesWritten = bytesWritten;
        this.durationMs = durationMs;
        this.verified = verified;
    }

    public File getZipFile() {
        return zipFile;
    }

    public int getEntryCount() {
        return entryCount;
    }

    public long getBytesWritten() {
        return bytesWritten;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public boolean isVerified() {
        return verified;
    }
}
