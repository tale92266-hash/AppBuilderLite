package com.nobi.appbuilder.codeproject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Create / rename / delete for workspace files. Deletion is refused
 * for protected files and always keeps a .bak copy first.
 */
public class CodeFileOperations {

    private final File root;

    public CodeFileOperations(File root) {
        this.root = root;
    }

    public File createFile(File parent, String name, String content) {
        if (parent == null || !CodeFileValidator.isPathInside(root, parent)
                || !CodeFileValidator.isValidFileName(name)) {
            return null;
        }
        File file = new File(parent, name);
        if (file.exists()) {
            return null;
        }
        try (FileOutputStream out = new FileOutputStream(file)) {
            out.write((content == null ? "" : content)
                    .getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public File createFolder(File parent, String name) {
        if (parent == null || !CodeFileValidator.isPathInside(root, parent)
                || !CodeFileValidator.isValidFileName(name)) {
            return null;
        }
        File dir = new File(parent, name);
        return dir.mkdirs() ? dir : null;
    }

    public boolean rename(File file, String newName) {
        if (file == null || !CodeFileValidator.isPathInside(root, file)
                || !CodeFileValidator.isValidFileName(newName)) {
            return false;
        }
        File target = new File(file.getParentFile(), newName);
        return !target.exists() && file.renameTo(target);
    }

    /** Deletes with a .bak backup kept beside the file. Protected
     * files are never deleted. Returns false when refused. */
    public boolean delete(File file) {
        if (file == null || !CodeFileValidator.isPathInside(root, file)
                || !CodeFileValidator.isDeletable(file)
                || !file.exists()) {
            return false;
        }
        if (file.isFile()) {
            File backup = new File(file.getParentFile(),
                    file.getName() + ".bak");
            backup.delete();
            file.renameTo(backup);
            return true;
        }
        return deleteRecursive(file);
    }

    private boolean deleteRecursive(File dir) {
        File[] children = dir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (!CodeFileValidator.isDeletable(child)) {
                    return false;
                }
                if (child.isDirectory()) {
                    if (!deleteRecursive(child)) {
                        return false;
                    }
                } else if (!child.delete()) {
                    return false;
                }
            }
        }
        return dir.delete();
    }
}
