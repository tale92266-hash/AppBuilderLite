package com.nobi.appbuilder.sync.reload;

import com.nobi.appbuilder.sync.ChangeOrigin;

/** Requests a code-editor reload after a visual-side change. */
public class CodeEditorRefreshManager {

    public static void register(
            LiveReloadManager.CodeRefreshListener listener) {
        LiveReloadManager.registerCode(listener);
    }

    public static void unregister() {
        LiveReloadManager.unregisterCode();
    }

    public static void requestRefresh(String projectId, String screenId,
                                      ChangeOrigin origin) {
        LiveReloadManager.requestReload(
                new ReloadRequest(projectId, screenId, origin));
    }
}
