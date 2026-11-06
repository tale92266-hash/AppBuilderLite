package com.nobi.appbuilder.dataflow;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-screen typed arguments, stored as JSON in project_settings
 * (key "screen_args_<screenId>"). Validates supplied values and
 * builds Intent extras for navigation — strings only.
 */
public class ScreenDataManager {

    public static final String ARGS_KEY_PREFIX = "screen_args_";

    private final ProjectSettingsManager settings;
    private final Gson gson = new Gson();

    public ScreenDataManager(DatabaseHelper dbHelper) {
        this.settings = new ProjectSettingsManager(dbHelper,
                new ProjectRepository(dbHelper));
    }

    public static String argsKey(String screenId) {
        return ARGS_KEY_PREFIX + screenId;
    }

    public List<ScreenArgument> getArguments(String projectId, String screenId) {
        String json = settings.getSetting(projectId, argsKey(screenId), "");
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<ScreenArgument>>() {
            }.getType();
            List<ScreenArgument> args = gson.fromJson(json, type);
            return args != null ? args : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void saveArguments(String projectId, String screenId,
                              List<ScreenArgument> args) {
        settings.setSetting(projectId, argsKey(screenId),
                gson.toJson(args == null ? new ArrayList<>() : args));
    }

    public ScreenArgument getArgument(String projectId, String screenId,
                                      String key) {
        for (ScreenArgument arg : getArguments(projectId, screenId)) {
            if (key != null && key.equals(arg.getKey())) {
                return arg;
            }
        }
        return null;
    }

    /** Validates supplied values; returns problems (empty = valid). */
    public List<String> validateValues(String projectId, String screenId,
                                       Map<String, String> values) {
        List<String> errors = new ArrayList<>();
        for (ScreenArgument arg : getArguments(projectId, screenId)) {
            String value = values == null ? null : values.get(arg.getKey());
            if (value == null || value.isEmpty()) {
                value = arg.getDefaultValue();
            }
            String problem = arg.validateValue(value);
            if (problem != null) {
                errors.add(problem);
            }
        }
        return errors;
    }

    /** Builds Intent extras: supplied values, then defaults. */
    public Intent buildExtras(String projectId, String screenId,
                              Map<String, String> values) {
        Intent intent = new Intent();
        for (ScreenArgument arg : getArguments(projectId, screenId)) {
            String value = values == null ? null : values.get(arg.getKey());
            if (value == null || value.isEmpty()) {
                value = arg.getDefaultValue();
            }
            if (value != null) {
                intent.putExtra("arg_" + arg.getKey(), value);
            }
        }
        return intent;
    }

    /** Reads "arg_<key>" extras back into a plain map. */
    public Map<String, String> readExtras(Intent intent) {
        Map<String, String> values = new LinkedHashMap<>();
        if (intent == null || intent.getExtras() == null) {
            return values;
        }
        for (String key : intent.getExtras().keySet()) {
            if (key != null && key.startsWith("arg_")) {
                Object value = intent.getExtras().get(key);
                if (value != null) {
                    values.put(key.substring("arg_".length()),
                            String.valueOf(value));
                }
            }
        }
        return values;
    }
}
