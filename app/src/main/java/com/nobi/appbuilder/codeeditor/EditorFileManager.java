package com.nobi.appbuilder.codeeditor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * File access for the editor. Every path is validated against the
 * workspace root (canonical-path guard against traversal), and saves
 * are atomic: write to a temp file, then rename over the original.
 * A backup (.bak) is kept before a destructive overwrite.
 */
public class EditorFileManager {

    private final File workspaceRoot;

    public EditorFileManager(File workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public File getWorkspaceRoot() {
        return workspaceRoot;
    }

    /** Resolves a relative path inside the workspace; null when it
     * escapes the workspace or is absolute. */
    public File resolve(String relativePath) {
        if (relativePath == null || relativePath.isEmpty()
                || relativePath.contains("..")
                || new File(relativePath).isAbsolute()) {
            return null;
        }
        try {
            File candidate = new File(workspaceRoot, relativePath);
            String root = workspaceRoot.getCanonicalPath();
            String path = candidate.getCanonicalPath();
            return path.startsWith(root) ? candidate : null;
        } catch (IOException e) {
            return null;
        }
    }

    public boolean isInsideWorkspace(File file) {
        try {
            String root = workspaceRoot.getCanonicalPath();
            return file.getCanonicalPath().startsWith(root);
        } catch (IOException e) {
            return false;
        }
    }

    public String read(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream in = new FileInputStream(file)) {
            int offset = 0;
            int read;
            while (offset < bytes.length
                    && (read = in.read(bytes, offset,
                    bytes.length - offset)) != -1) {
                offset += read;
            }
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    /** Atomic save: temp file + rename. Returns false on failure
     * without touching the original. */
    public boolean save(File file, String text) {
        if (!isInsideWorkspace(file)) {
            return false;
        }
        File temp = new File(file.getParentFile(),
                file.getName() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        } catch (IOException e) {
            temp.delete();
            return false;
        }
        if (file.exists()) {
            File backup = new File(file.getParentFile(),
                    file.getName() + ".bak");
            backup.delete();
            file.renameTo(backup);
        }
        if (!temp.renameTo(file)) {
            temp.delete();
            return false;
        }
        return true;
    }

    public boolean delete(File file) {
        if (!isInsideWorkspace(file) || !file.exists()) {
            return false;
        }
        return file.delete();
    }
}
