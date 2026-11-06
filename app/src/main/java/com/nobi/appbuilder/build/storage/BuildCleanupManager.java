package com.nobi.appbuilder.build.storage;

import java.io.File;

/** Safe cleanup of build workspaces. APKs and logs are never deleted
 * here — only temp workspaces, and only under the cache dir. */
public class BuildCleanupManager {

    /** Deletes a workspace only when it sits under the cache dir. */
    public static boolean deleteWorkspace(File cacheDir, File workspace) {
        if (workspace == null || cacheDir == null) {
            return false;
        }
        try {
            String cachePath = cacheDir.getCanonicalPath();
            String workspacePath = workspace.getCanonicalPath();
            if (!workspacePath.startsWith(cachePath)) {
                return false;
            }
            return deleteRecursive(workspace);
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        return file.delete();
    }

    /** Deletes workspaces older than maxAgeMs. Returns the count. */
    public static int deleteOldWorkspaces(File buildCacheDir, long maxAgeMs) {
        if (buildCacheDir == null || !buildCacheDir.isDirectory()) {
            return 0;
        }
        int count = 0;
        long now = System.currentTimeMillis();
        File[] children = buildCacheDir.listFiles();
        if (children != null) {
            for (File child : children) {
                if (now - child.lastModified() > maxAgeMs
                        && deleteRecursive(child)) {
                    count++;
                }
            }
        }
        return count;
    }
}
