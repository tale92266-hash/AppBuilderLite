package com.nobi.appbuilder.sync.reload;

import android.os.Handler;
import android.os.Looper;

/**
 * Central live-reload hub. Screens register one listener each while
 * they are visible; a sync run posts one debounced request and the
 * hub fans it out. Reloads only re-render content — selection,
 * canvas zoom/scroll, code cursor and preview navigation state stay
 * with the owning screen, never reset here.
 */
public class LiveReloadManager {

    public interface VisualRefreshListener {
        void onVisualRefreshRequested(ReloadRequest request);
    }

    public interface CodeRefreshListener {
        void onCodeRefreshRequested(ReloadRequest request);
    }

    public interface PreviewRefreshListener {
        void onPreviewRefreshRequested(ReloadRequest request);
    }

    private static VisualRefreshListener visualListener;
    private static CodeRefreshListener codeListener;
    private static PreviewRefreshListener previewListener;
    private static final Handler HANDLER =
            new Handler(Looper.getMainLooper());
    private static Runnable pending;

    public static void registerVisual(VisualRefreshListener listener) {
        visualListener = listener;
    }

    public static void unregisterVisual() {
        visualListener = null;
    }

    public static void registerCode(CodeRefreshListener listener) {
        codeListener = listener;
    }

    public static void unregisterCode() {
        codeListener = null;
    }

    public static void registerPreview(PreviewRefreshListener listener) {
        previewListener = listener;
    }

    public static void unregisterPreview() {
        previewListener = null;
    }

    /** Debounced fan-out (300 ms); a burst of changes reloads once. */
    public static void requestReload(final ReloadRequest request) {
        HANDLER.removeCallbacks(pending);
        pending = () -> {
            if (visualListener != null) {
                visualListener.onVisualRefreshRequested(request);
            }
            if (codeListener != null) {
                codeListener.onCodeRefreshRequested(request);
            }
            if (previewListener != null) {
                previewListener.onPreviewRefreshRequested(request);
            }
        };
        HANDLER.postDelayed(pending, 300);
    }
}
