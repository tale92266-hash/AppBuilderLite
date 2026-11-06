package com.nobi.appbuilder.library.presets;

import android.content.Context;

import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.properties.PropertyRegistry;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Facade for component presets: eight built-ins plus user presets.
 * Applying a preset only updates supported properties — the
 * component keeps its id and its other properties.
 */
public class ComponentPresetManager {

    private final DatabaseHelper dbHelper;
    private final PresetRepository repository;

    public ComponentPresetManager(Context context, DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.repository = new PresetRepository(dbHelper);
    }

    public PresetRepository getRepository() {
        return repository;
    }

    public static List<ComponentPreset> builtInPresets(String projectId) {
        List<ComponentPreset> presets = new ArrayList<>();
        presets.add(preset("builtin-primary-button", projectId,
                "Primary Button", "Button",
                "backgroundColor", "#0B4499",
                "textColor", "#FFFFFF",
                "cornerRadius", "8"));
        presets.add(preset("builtin-secondary-button", projectId,
                "Secondary Button", "Button",
                "backgroundColor", "#E8EEF7",
                "textColor", "#0B4499",
                "cornerRadius", "8"));
        presets.add(preset("builtin-outlined-button", projectId,
                "Outlined Button", "Button",
                "backgroundColor", "#00000000",
                "textColor", "#0B4499",
                "cornerRadius", "8"));
        presets.add(preset("builtin-filled-field", projectId,
                "Filled Text Field", "EditText",
                "backgroundColor", "#F5F5F5",
                "hint", "Filled field"));
        presets.add(preset("builtin-search-field", projectId,
                "Search Field", "EditText",
                "hint", "Search",
                "inputType", "text"));
        presets.add(preset("builtin-profile-image", projectId,
                "Profile Image", "ImageView",
                "cornerRadius", "999",
                "backgroundColor", "#E8EEF7"));
        presets.add(preset("builtin-heading-text", projectId,
                "Heading Text", "TextView",
                "textSize", "24",
                "textColor", "#111827"));
        presets.add(preset("builtin-card-container", projectId,
                "Card Container", "MaterialCardView",
                "backgroundColor", "#FFFFFF",
                "cornerRadius", "12",
                "elevation", "2"));
        return presets;
    }

    private static ComponentPreset preset(String id, String projectId,
                                          String name, String type,
                                          String... keyValues) {
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            props.put(keyValues[i], keyValues[i + 1]);
        }
        return new ComponentPreset(id, projectId, name, type, props, true);
    }

    public List<ComponentPreset> getAll(String projectId) {
        List<ComponentPreset> out = new ArrayList<>(builtInPresets(projectId));
        out.addAll(repository.getByProject(projectId));
        return out;
    }

    public List<ComponentPreset> getForType(String projectId,
                                            String componentType) {
        List<ComponentPreset> out = new ArrayList<>();
        for (ComponentPreset preset : getAll(projectId)) {
            if (componentType.equals(preset.getComponentType())) {
                out.add(preset);
            }
        }
        return out;
    }

    /** Applies a preset to a model. Returns the applied property keys. */
    public List<String> applyPreset(ComponentModel model,
                                    ComponentPreset preset) {
        List<String> applied = new ArrayList<>();
        if (model == null || preset == null) {
            return applied;
        }
        if (!model.getType().equals(preset.getComponentType())) {
            return applied;
        }
        for (Map.Entry<String, String> entry
                : preset.getProperties().entrySet()) {
            if (PropertyRegistry.get(entry.getKey()) == null
                    && !isKnownProperty(entry.getKey())) {
                continue;
            }
            model.setProperty(entry.getKey(), entry.getValue());
            applied.add(entry.getKey());
        }
        return applied;
    }

    private boolean isKnownProperty(String key) {
        return "backgroundColor".equals(key) || "cornerRadius".equals(key)
                || "elevation".equals(key) || "text".equals(key)
                || "textColor".equals(key) || "textSize".equals(key)
                || "hint".equals(key) || "inputType".equals(key);
    }

    public ComponentPreset createCustom(String projectId, String name,
                                        String componentType,
                                        Map<String, String> properties) {
        if (!ComponentRegistry.contains(componentType)) {
            return null;
        }
        ComponentPreset preset = new ComponentPreset(
                IDGenerator.newId(), projectId, name, componentType,
                properties, false);
        return repository.save(preset) ? preset : null;
    }

    public boolean delete(String presetId) {
        return repository.delete(presetId);
    }
}
