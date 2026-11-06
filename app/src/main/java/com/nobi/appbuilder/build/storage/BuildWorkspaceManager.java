package com.nobi.appbuilder.build.storage;

import android.content.Context;

import java.io.File;
import java.util.UUID;

/** Isolated per-build workspaces under the app cache. One build
 * never overwrites another; the original project is never touched. */
public class BuildWorkspaceManager {

    private final Context appContext;

    public BuildWorkspaceManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public File getBuildCacheDir() {
        File dir = new File(appContext.getCacheDir(), "builds");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File createWorkspace() {
        File workspace = new File(getBuildCacheDir(),
                "build-" + UUID.randomUUID().toString());
        workspace.mkdirs();
        return workspace;
    }

    public boolean deleteWorkspace(File workspace) {
        return BuildCleanupManager.deleteWorkspace(
                appContext.getCacheDir(), workspace);
    }
}
