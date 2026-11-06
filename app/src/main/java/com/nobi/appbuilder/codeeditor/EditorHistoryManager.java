package com.nobi.appbuilder.codeeditor;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Persists the editing session (open files, active tab, cursor
 * offsets) so the editor can restore it after an interruption. */
public class EditorHistoryManager {

    private static final String PREFS = "code_editor_history";
    private static final String KEY_SESSION = "session";

    /** One persisted tab. */
    public static class TabState {
        public String path;
        public int cursorOffset;
        public int scrollY;
    }

    private final SharedPreferences prefs;
    private final Gson gson = new Gson();

    public EditorHistoryManager(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void saveSession(String projectId, EditorSession session) {
        List<TabState> tabs = new ArrayList<>();
        for (EditorDocument document : session.getDocuments()) {
            TabState tab = new TabState();
            tab.path = document.getPath();
            tab.cursorOffset = document.getState().getCursor().getOffset();
            tab.scrollY = document.getState().getScrollY();
            tabs.add(tab);
        }
        SavedSession saved = new SavedSession();
        saved.projectId = projectId;
        saved.tabs = tabs;
        saved.activeIndex = session.getActiveIndex();
        prefs.edit().putString(KEY_SESSION + "_" + projectId,
                gson.toJson(saved)).apply();
    }

    public SavedSession loadSession(String projectId) {
        String json = prefs.getString(KEY_SESSION + "_" + projectId, null);
        if (json == null) {
            return null;
        }
        try {
            return gson.fromJson(json, SavedSession.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void clearSession(String projectId) {
        prefs.edit().remove(KEY_SESSION + "_" + projectId).apply();
    }

    /** Persisted session payload. */
    public static class SavedSession {
        public String projectId;
        public List<TabState> tabs;
        public int activeIndex;
    }

    /** Parses a JSON list of strings (used by the sync engine). */
    public static List<String> parseStringList(String json) {
        try {
            Type type = new TypeToken<List<String>>() {
            }.getType();
            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
