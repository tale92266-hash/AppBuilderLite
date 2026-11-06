package com.nobi.appbuilder.transitions;

import android.app.Activity;

/**
 * Applies a transition after a preview navigation, using only
 * framework animations. "none" applies nothing. Slide up/down use
 * their fade fallback, exactly as the registry defines.
 */
public class TransitionPreviewManager {

    public void applyAfterNavigation(Activity activity,
                                     TransitionDefinition definition) {
        if (activity == null || definition == null || definition.isNone()) {
            return;
        }
        int inAnim = animRes(definition.getAnimIn());
        int outAnim = animRes(definition.getAnimOut());
        if (inAnim == 0 || outAnim == 0) {
            return;
        }
        activity.overridePendingTransition(inAnim, outAnim);
    }

    public void applyAfterNavigation(Activity activity, String transitionId) {
        applyAfterNavigation(activity, TransitionRegistry.get(transitionId));
    }

    private int animRes(String name) {
        switch (name) {
            case "fade_in":
                return android.R.anim.fade_in;
            case "fade_out":
                return android.R.anim.fade_out;
            case "slide_in_left":
                return android.R.anim.slide_in_left;
            case "slide_out_right":
                return android.R.anim.slide_out_right;
            default:
                return 0;
        }
    }
}
