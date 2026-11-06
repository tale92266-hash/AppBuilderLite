package com.nobi.appbuilder.navigation;

import android.app.Activity;

import com.nobi.appbuilder.transitions.TransitionDefinition;
import com.nobi.appbuilder.transitions.TransitionPreviewManager;
import com.nobi.appbuilder.transitions.TransitionRegistry;

/**
 * Resolves the transition stored on a navigation edge and applies it
 * after a preview navigation. Edges without a transition use "none".
 */
public class ScreenTransitionManager {

    private final TransitionPreviewManager preview = new TransitionPreviewManager();

    public TransitionDefinition getTransitionForEdge(NavigationEdge edge) {
        if (edge == null) {
            return TransitionRegistry.get(TransitionRegistry.NONE);
        }
        return TransitionRegistry.withDuration(edge.getTransitionId(),
                edge.getDurationMs());
    }

    public void setTransitionForEdge(NavigationEdge edge, String transitionId) {
        if (edge == null) {
            return;
        }
        edge.setTransitionId(TransitionRegistry.contains(transitionId)
                ? transitionId : TransitionRegistry.NONE);
    }

    public void applyAfterNavigation(Activity activity, NavigationEdge edge) {
        preview.applyAfterNavigation(activity, getTransitionForEdge(edge));
    }
}
