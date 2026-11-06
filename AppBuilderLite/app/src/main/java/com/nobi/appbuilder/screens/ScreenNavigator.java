package com.nobi.appbuilder.screens;

import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Screen navigation by stable screen ids (never display names).
 * Validates navigation references: every click event with action
 * "navigate" must point at a screen that still exists in the project.
 */
public class ScreenNavigator {

    private final ScreenRepository repository;

    public ScreenNavigator(ScreenRepository repository) {
        this.repository = repository;
    }

    /** Returns the screen to open, or null when it does not exist. */
    public ScreenModel openScreen(String projectId, String screenId) {
        ScreenModel screen = repository.getById(screenId);
        if (screen == null || !projectId.equals(screen.getProjectId())) {
            return null;
        }
        return screen;
    }

    public boolean screenExists(String projectId, String screenId) {
        return openScreen(projectId, screenId) != null;
    }

    public ScreenModel getStartingScreen(String projectId) {
        List<ScreenModel> screens = repository.getByProject(projectId);
        for (ScreenModel screen : screens) {
            if (screen.isLauncher()) {
                return screen;
            }
        }
        return screens.isEmpty() ? null : screens.get(0);
    }

    /**
     * Finds navigate events whose target screen no longer exists.
     * Returns human-readable problems (empty = all references valid).
     */
    public List<String> validateNavigationReferences(String projectId) {
        List<String> problems = new ArrayList<>();
        List<ScreenModel> screens = repository.getByProject(projectId);
        Set<String> ids = new HashSet<>();
        for (ScreenModel screen : screens) {
            ids.add(screen.getId());
        }
        for (ScreenModel screen : screens) {
            ComponentModel root = screen.getRootComponent();
            if (root == null) {
                continue;
            }
            List<ComponentModel> all = new ArrayList<>();
            collectAll(root, all);
            for (ComponentModel model : all) {
                for (EventModel event : model.getEvents()) {
                    if (EventModel.ACTION_NAVIGATE.equals(event.getAction())) {
                        String target = event.getTargetScreenId();
                        if (target == null || target.isEmpty()) {
                            problems.add("Screen \"" + screen.getName() + "\": "
                                    + model.getType() + " has a navigation "
                                    + "event with no target screen.");
                        } else if (!ids.contains(target)) {
                            problems.add("Screen \"" + screen.getName() + "\": "
                                    + model.getType() + " navigates to a "
                                    + "deleted screen.");
                        }
                    }
                }
            }
        }
        return problems;
    }

    private void collectAll(ComponentModel model, List<ComponentModel> out) {
        out.add(model);
        for (ComponentModel child : model.getChildren()) {
            collectAll(child, out);
        }
    }
}
