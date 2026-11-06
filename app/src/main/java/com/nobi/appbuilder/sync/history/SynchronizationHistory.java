package com.nobi.appbuilder.sync.history;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/** Bounded (100) persisted history of sync snapshots. */
public class SynchronizationHistory {

    public static final int MAX_SNAPSHOTS = 100;

    private static final String HISTORY_FILE = "history.json";

    private final File stateDir;
    private final List<SynchronizationSnapshot> snapshots = new ArrayList<>();

    public SynchronizationHistory(Context context, String projectId) {
        stateDir = new File(new File(context.getApplicationContext()
                .getFilesDir(), "sync-state"), projectId);
        stateDir.mkdirs();
        load();
    }

    public void add(SynchronizationSnapshot snapshot) {
        snapshots.add(0, snapshot);
        while (snapshots.size() > MAX_SNAPSHOTS) {
            snapshots.remove(snapshots.size() - 1);
        }
        save();
    }

    public List<SynchronizationSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    public SynchronizationSnapshot getLatest() {
        return snapshots.isEmpty() ? null : snapshots.get(0);
    }

    public SynchronizationSnapshot findById(String id) {
        for (SynchronizationSnapshot snapshot : snapshots) {
            if (snapshot.getId().equals(id)) {
                return snapshot;
            }
        }
        return null;
    }

    public void clear() {
        snapshots.clear();
        save();
    }

    private void load() {
        try {
            File file = new File(stateDir, HISTORY_FILE);
            if (!file.exists()) {
                return;
            }
            String json = new String(java.nio.file.Files.readAllBytes(
                    file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            Type type = new TypeToken<List<SynchronizationSnapshot>>() {
            }.getType();
            List<SynchronizationSnapshot> loaded =
                    new Gson().fromJson(json, type);
            if (loaded != null) {
                snapshots.addAll(loaded);
            }
        } catch (Exception ignored) {
        }
    }

    private void save() {
        try {
            java.nio.file.Files.write(
                    new File(stateDir, HISTORY_FILE).toPath(),
                    new Gson().toJson(snapshots).getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
