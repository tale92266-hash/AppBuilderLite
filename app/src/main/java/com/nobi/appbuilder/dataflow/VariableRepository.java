package com.nobi.appbuilder.dataflow;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.project.ProjectSettingsManager;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Variable definitions, stored as JSON in project_settings:
 * "variables_global_json" for global variables and
 * "variables_<screenId>_json" for screen variables.
 */
public class VariableRepository {

    public static final String GLOBAL_KEY = "variables_global_json";
    public static final String SCREEN_KEY_PREFIX = "variables_";

    private final ProjectSettingsManager settings;
    private final Gson gson = new Gson();

    public VariableRepository(DatabaseHelper dbHelper) {
        this.settings = new ProjectSettingsManager(dbHelper,
                new ProjectRepository(dbHelper));
    }

    public static String screenKey(String screenId) {
        return SCREEN_KEY_PREFIX + screenId + "_json";
    }

    public List<VariableDefinition> getGlobalVariables(String projectId) {
        return readList(settings.getSetting(projectId, GLOBAL_KEY, ""));
    }

    public List<VariableDefinition> getScreenVariables(String projectId,
                                                       String screenId) {
        return readList(settings.getSetting(projectId, screenKey(screenId), ""));
    }

    public void saveGlobalVariables(String projectId,
                                    List<VariableDefinition> variables) {
        settings.setSetting(projectId, GLOBAL_KEY,
                gson.toJson(variables == null ? new ArrayList<>() : variables));
    }

    public void saveScreenVariables(String projectId, String screenId,
                                    List<VariableDefinition> variables) {
        settings.setSetting(projectId, screenKey(screenId),
                gson.toJson(variables == null ? new ArrayList<>() : variables));
    }

    public VariableDefinition find(String projectId, String screenId,
                                   String name) {
        for (VariableDefinition def : getScreenVariables(projectId, screenId)) {
            if (name != null && name.equals(def.getName())) {
                return def;
            }
        }
        for (VariableDefinition def : getGlobalVariables(projectId)) {
            if (name != null && name.equals(def.getName())) {
                return def;
            }
        }
        return null;
    }

    private List<VariableDefinition> readList(String json) {
        if (json == null || json.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            Type type = new TypeToken<List<VariableDefinition>>() {
            }.getType();
            List<VariableDefinition> list = gson.fromJson(json, type);
            return list != null ? list : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
