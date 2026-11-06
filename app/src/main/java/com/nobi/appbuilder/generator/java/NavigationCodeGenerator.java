package com.nobi.appbuilder.generator.java;

import com.nobi.appbuilder.generator.GenerationContext;

/**
 * Screen navigation code: stable screen ids in, explicit Intents
 * out. A destination that does not exist is a generation error, not
 * a dangling Intent.
 */
public class NavigationCodeGenerator {

    private final GenerationContext context;

    public NavigationCodeGenerator(GenerationContext context) {
        this.context = context;
    }

    /** True when the screen id resolves to a generated Activity. */
    public boolean isValidDestination(String screenId) {
        return screenId != null && context.activityClassFor(screenId) != null;
    }

    /** "new Intent(this, HomeActivity.class)" for a target screen. */
    public String intentFor(String screenId) {
        String activityClass = context.activityClassFor(screenId);
        if (activityClass == null) {
            return null;
        }
        return "new Intent(this, " + activityClass + ".class)";
    }

    /** The Activity class name for a screen id (or null). */
    public String activityClassFor(String screenId) {
        return context.activityClassFor(screenId);
    }
}
