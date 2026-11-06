package com.nobi.appbuilder.sync;

import android.content.Context;
import android.content.SharedPreferences;

/** User preferences for synchronization, in SharedPreferences. */
public class SynchronizationPolicy {

    public static final String POLICY_ASK = "ask";
    public static final String POLICY_KEEP_VISUAL = "keep_visual";
    public static final String POLICY_KEEP_CODE = "keep_code";

    private static final String PREFS = "sync_prefs";
    private static final String KEY_AUTO_SYNC = "auto_sync";
    private static final String KEY_DEBOUNCE_MS = "debounce_ms";
    private static final String KEY_CONFLICT_POLICY = "conflict_policy";
    private static final String KEY_PRESERVE_UNKNOWN = "preserve_unknown";

    private final SharedPreferences prefs;

    public SynchronizationPolicy(Context context) {
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isAutoSync() {
        return prefs.getBoolean(KEY_AUTO_SYNC, true);
    }

    public void setAutoSync(boolean autoSync) {
        prefs.edit().putBoolean(KEY_AUTO_SYNC, autoSync).apply();
    }

    public int getDebounceMs() {
        return Math.max(200, Math.min(2000,
                prefs.getInt(KEY_DEBOUNCE_MS, 500)));
    }

    public void setDebounceMs(int debounceMs) {
        prefs.edit().putInt(KEY_DEBOUNCE_MS, debounceMs).apply();
    }

    public String getConflictPolicy() {
        return prefs.getString(KEY_CONFLICT_POLICY, POLICY_ASK);
    }

    public void setConflictPolicy(String policy) {
        prefs.edit().putString(KEY_CONFLICT_POLICY, policy).apply();
    }

    public boolean isPreserveUnknownAttributes() {
        return prefs.getBoolean(KEY_PRESERVE_UNKNOWN, true);
    }

    public void setPreserveUnknownAttributes(boolean preserve) {
        prefs.edit().putBoolean(KEY_PRESERVE_UNKNOWN, preserve).apply();
    }
}
