package com.nobi.appbuilder.navigation;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory Back stack of visited screen ids (max 50). Preview-only
 * state — it is never persisted.
 */
public class NavigationHistoryManager {

    public static final int MAX_HISTORY = 50;

    private final List<String> history = new ArrayList<>();

    public void push(String screenId) {
        if (screenId == null) {
            return;
        }
        history.add(screenId);
        while (history.size() > MAX_HISTORY) {
            history.remove(0);
        }
    }

    /** Pops the current screen and returns the previous one, or null. */
    public String goBack() {
        if (history.size() <= 1) {
            return null;
        }
        history.remove(history.size() - 1);
        return history.get(history.size() - 1);
    }

    public String peek() {
        if (history.isEmpty()) {
            return null;
        }
        return history.get(history.size() - 1);
    }

    public boolean canGoBack() {
        return history.size() > 1;
    }

    public List<String> getHistory() {
        return new ArrayList<>(history);
    }

    public void clear() {
        history.clear();
    }
}
