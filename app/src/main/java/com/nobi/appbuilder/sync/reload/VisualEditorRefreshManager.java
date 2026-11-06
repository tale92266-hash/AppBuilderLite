package com.nobi.appbuilder.sync.reload;

import com.nobi.appbuilder.sync.ChangeOrigin;

/** Requests a visual-editor reload after a code-side change. */
public class VisualEditorRefreshManager {

    public static void register(
            LiveReloadManager.VisualRefreshListener listener) {
        LiveReloadManager.registerVisual(listener);
    }

    public static void unregister() {
        LiveReloadManager.unregisterVisual();
    }

    public static void requestRefresh(String projectId, String screenId,
                                      ChangeOrigin origin) {
        LiveReloadManager.requestReload(
                new ReloadRequest(projectId, screenId, origin));
    }
}
