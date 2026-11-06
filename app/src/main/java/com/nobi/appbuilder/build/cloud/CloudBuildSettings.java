package com.nobi.appbuilder.build.cloud;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Cloud build connection: the user's GitHub account for the build
 * repo. The token is the user's own personal access token (classic)
 * with repo + workflow scopes; it lives in this app's private
 * preferences, never in a file the user can share by accident, and
 * it can be revoked on github.com at any time. A future hardening
 * step is EncryptedSharedPreferences; the storage is app-private
 * either way.
 */
public class CloudBuildSettings {

    public static final String PREFS_CLOUD = "cloud_build_prefs";
    public static final String KEY_TOKEN = "github_token";
    public static final String KEY_OWNER = "github_owner";
    public static final String KEY_REPO = "github_repo";

    /** Private repo the app creates for cloud builds. */
    public static final String DEFAULT_REPO = "appbuilder-cloud-builds";

    private final SharedPreferences prefs;

    public CloudBuildSettings(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(
                PREFS_CLOUD, Context.MODE_PRIVATE);
    }

    public boolean isConnected() {
        String token = getToken();
        return token != null && !token.trim().isEmpty()
                && getOwner() != null;
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getOwner() {
        return prefs.getString(KEY_OWNER, null);
    }

    public String getRepo() {
        return prefs.getString(KEY_REPO, DEFAULT_REPO);
    }

    public void setRepo(String repo) {
        prefs.edit().putString(KEY_REPO,
                repo == null || repo.trim().isEmpty()
                        ? DEFAULT_REPO : repo.trim()).apply();
    }

    /** Saves a verified token + owner (verify with GitHub first). */
    public void connect(String token, String owner) {
        prefs.edit()
                .putString(KEY_TOKEN, token.trim())
                .putString(KEY_OWNER, owner.trim())
                .apply();
    }

    public void disconnect() {
        prefs.edit()
                .remove(KEY_TOKEN)
                .remove(KEY_OWNER)
                .apply();
    }
}
