package com.nobi.appbuilder.sync.conflict;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Records and persists conflicts until the user resolves them. */
public class ConflictManager {

    private static final String CONFLICTS_FILE = "conflicts.json";

    private final File stateDir;
    private final List<ConflictRecord> records = new ArrayList<>();

    public ConflictManager(Context context, String projectId) {
        stateDir = new File(new File(context.getApplicationContext()
                .getFilesDir(), "sync-state"), projectId);
        stateDir.mkdirs();
        load();
    }

    public ConflictRecord record(String screenId, String componentId,
                                 ConflictType type, String visualValue,
                                 String codeValue) {
        ConflictRecord record = new ConflictRecord(
                stateDir.getName(), screenId, componentId, type,
                visualValue, codeValue);
        records.add(record);
        save();
        return record;
    }

    public List<ConflictRecord> getUnresolved() {
        List<ConflictRecord> out = new ArrayList<>();
        for (ConflictRecord record : records) {
            if (!record.isResolved()) {
                out.add(record);
            }
        }
        return out;
    }

    public List<ConflictRecord> getAll() {
        return new ArrayList<>(records);
    }

    public int unresolvedCount() {
        return getUnresolved().size();
    }

    public ConflictRecord findById(String id) {
        for (ConflictRecord record : records) {
            if (record.getId().equals(id)) {
                return record;
            }
        }
        return null;
    }

    public void markResolved(ConflictRecord record, String resolution) {
        record.setResolved(true);
        record.setResolution(resolution);
        save();
    }

    public void removeResolved() {
        Iterator<ConflictRecord> it = records.iterator();
        while (it.hasNext()) {
            if (it.next().isResolved()) {
                it.remove();
            }
        }
        save();
    }

    private void load() {
        try {
            File file = new File(stateDir, CONFLICTS_FILE);
            if (!file.exists()) {
                return;
            }
            String json = new String(java.nio.file.Files.readAllBytes(
                    file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            Type type = new TypeToken<List<ConflictRecord>>() {
            }.getType();
            List<ConflictRecord> loaded = new Gson().fromJson(json, type);
            if (loaded != null) {
                records.addAll(loaded);
            }
        } catch (Exception ignored) {
        }
    }

    private void save() {
        try {
            java.nio.file.Files.write(
                    new File(stateDir, CONFLICTS_FILE).toPath(),
                    new Gson().toJson(records).getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }
}
