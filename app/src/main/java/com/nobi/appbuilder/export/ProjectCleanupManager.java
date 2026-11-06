package com.nobi.appbuilder.export;

import java.io.File;

/**
 * Safe cleanup of temporary export workspaces. Only deletes inside
 * the app's own cache directory, never user files, never the original
 * project, and never outside the designated workspace root.
 */
public class ProjectCleanupManager {

    /** Recursively deletes a workspace that lives under cacheDir. */
    public static boolean deleteWorkspace(File cacheDir, File workspace) {
        if (cacheDir == null || workspace == null) {
            return false;
        }
        try {
            String cachePath = cacheDir.getCanonicalPath();
            String workspacePath = workspace.getCanonicalPath();
            if (!workspacePath.startsWith(cachePath + File.separator)) {
                return false;
            }
            return deleteRecursive(workspace);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean deleteRecursive(File file) {
        boolean ok = true;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    ok = deleteRecursive(child) && ok;
                }
            }
        }
        return file.delete() && ok;
    }

    /** Deletes export workspaces older than the given age. */
    public static int deleteOldWorkspaces(File exportCacheDir, long maxAgeMs) {
        if (exportCacheDir == null || !exportCacheDir.isDirectory()) {
            return 0;
        }
        int deleted = 0;
        long now = System.currentTimeMillis();
        File[] children = exportCacheDir.listFiles();
        if (children == null) {
            return 0;
        }
        for (File child : children) {
            if (now - child.lastModified() > maxAgeMs
                    && deleteRecursive(child)) {
                deleted++;
            }
        }
        return deleted;
    }
}
