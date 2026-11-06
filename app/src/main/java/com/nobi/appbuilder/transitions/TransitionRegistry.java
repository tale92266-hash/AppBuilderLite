package com.nobi.appbuilder.transitions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Built-in transitions. Slide up/down have no framework slide anims,
 * so they fall back to fade — the registry never claims an anim that
 * does not exist.
 */
public final class TransitionRegistry {

    public static final String NONE = "none";
    public static final String FADE = "fade";
    public static final String SLIDE_LEFT = "slide_left";
    public static final String SLIDE_RIGHT = "slide_right";
    public static final String SLIDE_UP = "slide_up";
    public static final String SLIDE_DOWN = "slide_down";

    private static final Map<String, TransitionDefinition> ALL =
            new LinkedHashMap<>();

    static {
        register(new TransitionDefinition(NONE, "None",
                "none", "none", TransitionDefinition.DEFAULT_DURATION_MS));
        register(new TransitionDefinition(FADE, "Fade",
                "fade_in", "fade_out", TransitionDefinition.DEFAULT_DURATION_MS));
        register(new TransitionDefinition(SLIDE_LEFT, "Slide left",
                "slide_in_left", "slide_out_right",
                TransitionDefinition.DEFAULT_DURATION_MS));
        register(new TransitionDefinition(SLIDE_RIGHT, "Slide right",
                "slide_in_left", "slide_out_right",
                TransitionDefinition.DEFAULT_DURATION_MS));
        // No framework slide-up/slide-down anims: honest fade fallback.
        register(new TransitionDefinition(SLIDE_UP, "Slide up (fade)",
                "fade_in", "fade_out", TransitionDefinition.DEFAULT_DURATION_MS));
        register(new TransitionDefinition(SLIDE_DOWN, "Slide down (fade)",
                "fade_in", "fade_out", TransitionDefinition.DEFAULT_DURATION_MS));
    }

    private TransitionRegistry() {
    }

    private static void register(TransitionDefinition definition) {
        ALL.put(definition.getId(), definition);
    }

    public static TransitionDefinition get(String id) {
        if (id == null) {
            return ALL.get(NONE);
        }
        TransitionDefinition found = ALL.get(id);
        return found != null ? found : ALL.get(NONE);
    }

    /** Same transition with a custom (clamped) duration. */
    public static TransitionDefinition withDuration(String id,
                                                    int durationMs) {
        TransitionDefinition base = get(id);
        return new TransitionDefinition(base.getId(), base.getDisplayName(),
                base.getAnimIn(), base.getAnimOut(), durationMs);
    }

    public static boolean contains(String id) {
        return id != null && ALL.containsKey(id);
    }

    public static List<TransitionDefinition> getAll() {
        return new ArrayList<>(ALL.values());
    }

    public static List<String> getIds() {
        return new ArrayList<>(ALL.keySet());
    }
}
