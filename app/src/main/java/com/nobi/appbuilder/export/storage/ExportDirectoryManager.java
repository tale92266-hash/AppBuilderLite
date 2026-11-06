package com.nobi.appbuilder.export.storage;

import android.content.Context;

import com.nobi.appbuilder.export.ProjectCleanupManager;

import java.io.File;
import java.util.UUID;

/**
 * Temporary export workspaces under the app cache. Each export gets
 * its own directory so two exports can never overwrite each other.
 */
public class ExportDirectoryManager {

    private final Context appContext;

    public ExportDirectoryManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public File getExportCacheDir() {
        File dir = new File(appContext.getCacheDir(), "export");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** A fresh empty workspace for one export run. */
    public File createWorkspace() {
        File workspace = new File(getExportCacheDir(),
                "project-" + UUID.randomUUID().toString());
        workspace.mkdirs();
        return workspace;
    }

    public boolean deleteWorkspace(File workspace) {
        return ProjectCleanupManager.deleteWorkspace(appContext.getCacheDir(),
                workspace);
    }

    /** Deletes workspaces older than a day. Returns how many. */
    public int cleanupOldWorkspaces() {
        return ProjectCleanupManager.deleteOldWorkspaces(getExportCacheDir(),
                24L * 60 * 60 * 1000);
    }
}
