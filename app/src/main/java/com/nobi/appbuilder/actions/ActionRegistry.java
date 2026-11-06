package com.nobi.appbuilder.actions;

import com.nobi.appbuilder.actions.builtins.ChangeTextAction;
import com.nobi.appbuilder.actions.builtins.ConditionalAction;
import com.nobi.appbuilder.actions.builtins.DelayAction;
import com.nobi.appbuilder.actions.builtins.FinishScreenAction;
import com.nobi.appbuilder.actions.builtins.NavigateAction;
import com.nobi.appbuilder.actions.builtins.OpenUrlAction;
import com.nobi.appbuilder.actions.builtins.SetEnabledAction;
import com.nobi.appbuilder.actions.builtins.SetVariableAction;
import com.nobi.appbuilder.actions.builtins.SetVisibilityAction;
import com.nobi.appbuilder.actions.builtins.ShowDialogAction;
import com.nobi.appbuilder.actions.builtins.ShowToastAction;
import com.nobi.appbuilder.actions.builtins.ValidateFormAction;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The single registry of built-in actions. Every surface — workflow
 * editor, preview executor, validator — looks an action up here, so
 * one action id always means the same thing everywhere.
 */
public final class ActionRegistry {

    private static final Map<String, ActionDefinition> ALL =
            new LinkedHashMap<>();

    static {
        register(NavigateAction.definition());
        register(ShowToastAction.definition());
        register(ChangeTextAction.definition());
        register(SetVisibilityAction.definition());
        register(SetEnabledAction.definition());
        register(OpenUrlAction.definition());
        register(FinishScreenAction.definition());
        register(ValidateFormAction.definition());
        register(SetVariableAction.definition());
        register(ShowDialogAction.definition());
        register(DelayAction.definition());
        register(ConditionalAction.definition());
    }

    private ActionRegistry() {
    }

    private static void register(ActionDefinition definition) {
        ALL.put(definition.getId(), definition);
    }

    public static ActionDefinition get(String actionId) {
        return actionId == null ? null : ALL.get(actionId);
    }

    public static boolean contains(String actionId) {
        return actionId != null && ALL.containsKey(actionId);
    }

    public static List<ActionDefinition> getAll() {
        return new ArrayList<>(ALL.values());
    }

    public static List<ActionDefinition> getByCategory(String category) {
        List<ActionDefinition> out = new ArrayList<>();
        for (ActionDefinition def : ALL.values()) {
            if (category != null && category.equals(def.getCategory())) {
                out.add(def);
            }
        }
        return out;
    }
}
