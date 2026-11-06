package com.nobi.appbuilder.sync;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

/**
 * Content hashes for every synchronized file. A file is "changed by
 * the user" when its disk hash differs from the hash the sync engine
 * last recorded — that is how visual-to-code avoids overwriting
 * manual XML edits.
 */
public class ChangeTracker {

    private static final String HASHES_FILE = "hashes.json";

    private final File stateDir;
    private final Map<String, String> hashes = new HashMap<>();

    public ChangeTracker(Context context, String projectId) {
        stateDir = new File(new File(context.getApplicationContext()
                .getFilesDir(), "sync-state"), projectId);
        stateDir.mkdirs();
        load();
    }

    public static String hashOf(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(
                    java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder out = new StringBuilder();
            for (byte b : hash) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            return String.valueOf(text == null ? 0 : text.hashCode());
        }
    }

    public void record(String relativePath, String content) {
        hashes.put(relativePath, hashOf(content));
        save();
    }

    public String recordedHash(String relativePath) {
        return hashes.get(relativePath);
    }

    /** True when the file on disk differs from the recorded hash. */
    public boolean hasChanged(String relativePath, String currentContent) {
        String recorded = hashes.get(relativePath);
        return recorded == null
                || !recorded.equals(hashOf(currentContent));
    }

    public boolean isTracked(String relativePath) {
        return hashes.containsKey(relativePath);
    }

    private void load() {
        try {
            File file = new File(stateDir, HASHES_FILE);
            if (!file.exists()) {
                return;
            }
            String json = new String(java.nio.file.Files.readAllBytes(
                    file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> map = new Gson().fromJson(json, type);
            if (map != null) {
                hashes.putAll(map);
            }
        } catch (Exception ignored) {
        }
    }

    private void save() {
        try {
            java.nio.file.Files.write(
                    new File(stateDir, HASHES_FILE).toPath(),
                    new Gson().toJson(hashes).getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
