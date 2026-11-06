package com.nobi.appbuilder.transitions;

/**
 * One screen transition. The animation always references a framework
 * animation (android.R.anim.*) or nothing at all — a transition never
 * generates a reference to an animation resource that does not exist.
 * Custom durations are clamped to the safe range 100-1000ms.
 */
public class TransitionDefinition {

    public static final int MIN_DURATION_MS = 100;
    public static final int MAX_DURATION_MS = 1000;
    public static final int DEFAULT_DURATION_MS = 300;

    private final String id;
    private final String displayName;
    private final String animIn;
    private final String animOut;
    private final int durationMs;

    public TransitionDefinition(String id, String displayName,
                                String animIn, String animOut, int durationMs) {
        this.id = id;
        this.displayName = displayName;
        this.animIn = animIn;
        this.animOut = animOut;
        this.durationMs = clampDuration(durationMs);
    }

    public static int clampDuration(int durationMs) {
        if (durationMs < MIN_DURATION_MS) {
            return MIN_DURATION_MS;
        }
        if (durationMs > MAX_DURATION_MS) {
            return MAX_DURATION_MS;
        }
        return durationMs;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Framework anim resource name, or "none". */
    public String getAnimIn() {
        return animIn;
    }

    /** Framework anim resource name, or "none". */
    public String getAnimOut() {
        return animOut;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public boolean isNone() {
        return "none".equals(id);
    }

    /** The exact Java line a generated Activity would use after
     * startActivity, or "" for the none transition. Framework anims
     * only — no generated resource references. */
    public String javaSnippet() {
        if (isNone()) {
            return "";
        }
        return "overridePendingTransition(android.R.anim." + animIn
                + ", android.R.anim." + animOut + ");";
    }
}
