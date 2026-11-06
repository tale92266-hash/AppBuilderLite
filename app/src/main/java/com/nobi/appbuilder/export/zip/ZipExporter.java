package com.nobi.appbuilder.export.zip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * Creates ZIP archives with buffered streaming — the whole project
 * is never held in RAM twice. Supports cancellation between entries
 * and reports progress in bytes written. The output stream is
 * supplied by the caller so SAF destinations work the same as files.
 */
public class ZipExporter {

    public interface ProgressListener {
        void onProgress(long bytesWritten, long totalBytes);
    }

    private volatile boolean cancelled;

    public void cancel() {
        cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    /** Zips a workspace directory (relative paths preserved). */
    public ZipExportResult exportDirectory(File workspaceRoot, File zipFile,
                                           ProgressListener listener)
            throws IOException {
        long totalBytes = directorySize(workspaceRoot);
        long started = System.currentTimeMillis();
        long written = 0;
        int entries = 0;
        try (OutputStream fileOut = new BufferedOutputStream(
                new FileOutputStream(zipFile));
             ZipOutputStream zipOut = new ZipOutputStream(fileOut)) {
            ZipEntryWriter writer = new ZipEntryWriter(zipOut);
            List<File> files = listFilesRecursive(workspaceRoot);
            for (File file : files) {
                if (cancelled) {
                    throw new IOException("Export cancelled");
                }
                if (file.equals(workspaceRoot)) {
                    continue;
                }
                String relative = relativePath(workspaceRoot, file);
                if (file.isDirectory()) {
                    writer.addDirectory(relative);
                    entries++;
                    continue;
                }
                try (InputStream in = new FileInputStream(file)) {
                    writer.addStream(relative, in, file.length());
                }
                written += file.length();
                entries++;
                if (listener != null) {
                    listener.onProgress(written, totalBytes);
                }
            }
            // Empty standard directories, so the structure is visible
            // even when a project has no files in them yet.
            for (String dir : new String[]{
                    "app/src/main/res/xml/",
                    "app/src/main/res/mipmap-hdpi/",
                    "app/src/main/res/mipmap-mdpi/",
                    "app/src/main/res/mipmap-xhdpi/",
                    "app/src/main/res/mipmap-xxhdpi/",
                    "app/src/main/res/mipmap-xxxhdpi/"}) {
                if (!writer.hasWritten(dir)) {
                    writer.addDirectory(dir);
                    entries++;
                }
            }
            zipOut.finish();
            return new ZipExportResult(zipFile, entries, written,
                    System.currentTimeMillis() - started, false);
        }
    }

    /** Zips to an arbitrary output stream (e.g. a SAF document). */
    public ZipExportResult exportDirectoryToStream(File workspaceRoot,
                                                   OutputStream output,
                                                   ProgressListener listener)
            throws IOException {
        long totalBytes = directorySize(workspaceRoot);
        long started = System.currentTimeMillis();
        long written = 0;
        int entries = 0;
        try (ZipOutputStream zipOut = new ZipOutputStream(
                new BufferedOutputStream(output))) {
            ZipEntryWriter writer = new ZipEntryWriter(zipOut);
            for (File file : listFilesRecursive(workspaceRoot)) {
                if (cancelled) {
                    throw new IOException("Export cancelled");
                }
                if (file.equals(workspaceRoot)) {
                    continue;
                }
                String relative = relativePath(workspaceRoot, file);
                if (file.isDirectory()) {
                    writer.addDirectory(relative);
                    entries++;
                    continue;
                }
                try (InputStream in = new FileInputStream(file)) {
                    writer.addStream(relative, in, file.length());
                }
                written += file.length();
                entries++;
                if (listener != null) {
                    listener.onProgress(written, totalBytes);
                }
            }
            zipOut.finish();
            return new ZipExportResult(null, entries, written,
                    System.currentTimeMillis() - started, false);
        }
    }

    private List<File> listFilesRecursive(File root) {
        List<File> out = new ArrayList<>();
        collect(root, out);
        out.sort((a, b) -> a.getAbsolutePath().compareTo(b.getAbsolutePath()));
        return out;
    }

    private void collect(File file, List<File> out) {
        out.add(file);
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                java.util.Arrays.sort(children,
                        (a, b) -> a.getName().compareTo(b.getName()));
                for (File child : children) {
                    collect(child, out);
                }
            }
        }
    }

    private long directorySize(File root) {
        long size = 0;
        for (File file : listFilesRecursive(root)) {
            if (file.isFile()) {
                size += file.length();
            }
        }
        return size;
    }

    private String relativePath(File root, File file) {
        String rootPath = root.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath)) {
            String relative = filePath.substring(rootPath.length());
            if (relative.startsWith(File.separator)) {
                relative = relative.substring(1);
            }
            return relative.replace(File.separatorChar, '/');
        }
        return file.getName();
    }
}
