package com.nobi.appbuilder.sync.reload;

import com.nobi.appbuilder.sync.ChangeOrigin;

/** Requests a native-preview re-render after any sync change. */
public class PreviewRefreshManager {

    public static void register(
            LiveReloadManager.PreviewRefreshListener listener) {
        LiveReloadManager.registerPreview(listener);
    }

    public static void unregister() {
        LiveReloadManager.unregisterPreview();
    }

    public static void requestRefresh(String projectId, String screenId,
                                      ChangeOrigin origin) {
        LiveReloadManager.requestReload(
                new ReloadRequest(projectId, screenId, origin));
    }
}
