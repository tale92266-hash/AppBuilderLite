package com.nobi.appbuilder.preview;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;

/**
 * In-preview screen navigation by stable screen id (never by display
 * name). Keeps its own history stack for Back, knows the starting
 * screen for Restart, and refuses unknown destinations instead of
 * crashing — the caller shows "Screen not found".
 */
public class PreviewNavigator {

    public interface ScreenChangeListener {
        void onScreenChanged(ScreenModel screen);
    }

    private final IRDocument document;
    private final PreviewSession session;
    private final ScreenChangeListener listener;

    public PreviewNavigator(IRDocument document, PreviewSession session,
                            ScreenChangeListener listener) {
        this.document = document;
        this.session = session;
        this.listener = listener;
    }

    /** Opens a screen, pushing the current one onto the history. */
    public boolean openScreen(String screenId) {
        if (document == null || screenId == null) {
            return false;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            return false;
        }
        String current = session.getCurrentScreenId();
        if (current != null && !current.equals(screenId)) {
            session.pushHistory(current);
        }
        session.setCurrentScreenId(screenId);
        listener.onScreenChanged(screen);
        return true;
    }

    /** Returns to the previous screen, or false when at the start. */
    public boolean goBack() {
        if (!session.canGoBack()) {
            return false;
        }
        String previous = session.popHistory();
        ScreenModel screen = document.getScreen(previous);
        if (screen == null) {
            return false;
        }
        session.setCurrentScreenId(previous);
        listener.onScreenChanged(screen);
        return true;
    }

    public boolean canGoBack() {
        return session.canGoBack();
    }

    /** Clears the history and shows the starting screen again. */
    public void restart() {
        session.clearHistory();
        String initial = session.getInitialScreenId();
        ScreenModel screen = document == null ? null : document.getScreen(initial);
        if (screen == null && document != null && !document.getScreens().isEmpty()) {
            screen = document.getScreens().get(0);
        }
        if (screen != null) {
            session.setCurrentScreenId(screen.getId());
            listener.onScreenChanged(screen);
        }
    }

    public String getCurrentScreenId() {
        return session.getCurrentScreenId();
    }

    public ScreenModel getCurrentScreen() {
        return document == null ? null
                : document.getScreen(session.getCurrentScreenId());
    }

    /** The launcher screen, or the first screen when none is marked. */
    public ScreenModel getStartingScreen() {
        if (document == null) {
            return null;
        }
        for (ScreenModel screen : document.getScreens()) {
            if (screen.isLauncher()) {
                return screen;
            }
        }
        return document.getScreens().isEmpty() ? null : document.getScreens().get(0);
    }
}
