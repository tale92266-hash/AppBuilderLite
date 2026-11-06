package com.nobi.appbuilder.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Writes files into the export workspace. Every path is validated
 * (no absolute paths, no "..", no backslashes) and duplicates are
 * rejected — a duplicate means the generator produced the same file
 * twice, which must be reported, not silently overwritten.
 */
public class ProjectFileManager {

    private final File workspaceRoot;
    private final Set<String> writtenPaths = new HashSet<>();

    public ProjectFileManager(File workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    /** Normalizes a relative path; null when the path is unsafe. */
    public static String safeRelativePath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String normalized = path.replace('\\', '/');
        if (normalized.startsWith("/") || normalized.contains("..")
                || normalized.contains("\0")) {
            return null;
        }
        // Collapse duplicate slashes.
        while (normalized.contains("//")) {
            normalized = normalized.replace("//", "/");
        }
        return normalized;
    }

    public boolean writeText(String relativePath, String content)
            throws IOException {
        String safe = safeRelativePath(relativePath);
        if (safe == null) {
            throw new IOException("Unsafe file path: " + relativePath);
        }
        if (!writtenPaths.add(safe)) {
            throw new IOException("Duplicate file: " + safe);
        }
        File file = new File(workspaceRoot, safe);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory: " + parent);
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
        return true;
    }

    public boolean writeBytes(String relativePath, byte[] content)
            throws IOException {
        String safe = safeRelativePath(relativePath);
        if (safe == null) {
            throw new IOException("Unsafe file path: " + relativePath);
        }
        if (!writtenPaths.add(safe)) {
            throw new IOException("Duplicate file: " + safe);
        }
        File file = new File(workspaceRoot, safe);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            throw new IOException("Cannot create directory: " + parent);
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(content);
        }
        return true;
    }

    public boolean hasWritten(String relativePath) {
        return writtenPaths.contains(safeRelativePath(relativePath));
    }

    public Set<String> getWrittenPaths() {
        return new HashSet<>(writtenPaths);
    }

    public File getWorkspaceRoot() {
        return workspaceRoot;
    }
}
