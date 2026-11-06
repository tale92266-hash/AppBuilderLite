package com.nobi.appbuilder.screens;

import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * Screen CRUD, reorder and launcher selection (Phase 5). Reuses the
 * Phase 1 database.ScreenRepository — there is intentionally no second
 * ScreenRepository class. Screen ordering lives in the sort_order
 * column; the launcher is the screen with is_launcher set.
 */
public class ScreenManager {

    private final ScreenRepository repository;

    public ScreenManager(ScreenRepository repository) {
        this.repository = repository;
    }

    public List<ScreenModel> getScreens(String projectId) {
        return repository.getByProject(projectId);
    }

    public ScreenModel getScreen(String screenId) {
        return repository.getById(screenId);
    }

    public ScreenModel getLauncherScreen(String projectId) {
        for (ScreenModel screen : getScreens(projectId)) {
            if (screen.isLauncher()) {
                return screen;
            }
        }
        return null;
    }

    public List<ScreenModel> searchScreens(String projectId, String query) {
        List<ScreenModel> found = new ArrayList<>();
        String needle = query == null ? "" : query.trim().toLowerCase();
        for (ScreenModel screen : getScreens(projectId)) {
            if (needle.isEmpty() || (screen.getName() != null
                    && screen.getName().toLowerCase().contains(needle))) {
                found.add(screen);
            }
        }
        return found;
    }

    /** Creates a screen with an empty root container. Returns null on error. */
    public ScreenModel createScreen(String projectId, String name, String type,
                                    String orientation) {
        List<ScreenModel> existing = getScreens(projectId);
        String error = ScreenValidator.validateName(name, existing, null);
        if (error != null) {
            return null;
        }
        if (ScreenValidator.validateType(type) != null) {
            return null;
        }
        int maxSort = -1;
        for (ScreenModel screen : existing) {
            maxSort = Math.max(maxSort, screen.getSortOrder());
        }
        ScreenModel screen = new ScreenModel(IDGenerator.newScreenId(), projectId,
                name.trim(), existing.isEmpty(), maxSort + 1);
        screen.setScreenType(type);
        screen.setOrientation(orientation == null
                ? ScreenModel.ORIENTATION_PORTRAIT : orientation);
        screen.setRootComponent(ComponentFactory.createRootContainer());
        return repository.insert(screen) ? screen : null;
    }

    public String renameScreen(String screenId, String newName) {
        ScreenModel screen = repository.getById(screenId);
        if (screen == null) {
            return "Screen not found.";
        }
        String error = ScreenValidator.validateName(newName,
                getScreens(screen.getProjectId()), screenId);
        if (error != null) {
            return error;
        }
        screen.setName(newName.trim());
        return repository.updateDesign(screen) ? null : "Could not rename screen.";
    }

    public ScreenModel duplicateScreen(String screenId, String newName) {
        ScreenModel source = repository.getById(screenId);
        if (source == null) {
            return null;
        }
        List<ScreenModel> existing = getScreens(source.getProjectId());
        if (ScreenValidator.validateName(newName, existing, null) != null) {
            return null;
        }
        int maxSort = -1;
        for (ScreenModel screen : existing) {
            maxSort = Math.max(maxSort, screen.getSortOrder());
        }
        ScreenModel copy = ScreenDuplicator.duplicate(source, newName.trim(),
                maxSort + 1);
        return repository.insert(copy) ? copy : null;
    }

    /** Deletes a screen; refuses the last one and promotes a new launcher. */
    public String deleteScreen(String screenId) {
        ScreenModel screen = repository.getById(screenId);
        if (screen == null) {
            return "Screen not found.";
        }
        List<ScreenModel> screens = getScreens(screen.getProjectId());
        String error = ScreenValidator.validateDelete(screens.size());
        if (error != null) {
            return error;
        }
        boolean wasLauncher = screen.isLauncher();
        if (!repository.delete(screenId)) {
            return "Could not delete screen.";
        }
        if (wasLauncher) {
            List<ScreenModel> remaining = getScreens(screen.getProjectId());
            if (!remaining.isEmpty()) {
                setLauncherScreen(remaining.get(0).getId());
            }
        }
        return null;
    }

    /** Persists a new order (screens already sorted by the caller). */
    public void reorderScreens(List<ScreenModel> ordered) {
        for (int i = 0; i < ordered.size(); i++) {
            ScreenModel screen = ordered.get(i);
            screen.setSortOrder(i);
            repository.updateDesign(screen);
        }
    }

    /** Makes one screen the launcher and clears the flag on the rest. */
    public boolean setLauncherScreen(String screenId) {
        ScreenModel target = repository.getById(screenId);
        if (target == null) {
            return false;
        }
        boolean ok = true;
        for (ScreenModel screen : getScreens(target.getProjectId())) {
            boolean launcher = screen.getId().equals(screenId);
            if (screen.isLauncher() != launcher) {
                screen.setLauncher(launcher);
                ok &= repository.updateDesign(screen);
            }
        }
        return ok;
    }

    public String setScreenType(String screenId, String type) {
        ScreenModel screen = repository.getById(screenId);
        if (screen == null) {
            return "Screen not found.";
        }
        String error = ScreenValidator.validateType(type);
        if (error != null) {
            return error;
        }
        screen.setScreenType(type);
        return repository.updateDesign(screen) ? null : "Could not update screen.";
    }

    public String setOrientation(String screenId, String orientation) {
        ScreenModel screen = repository.getById(screenId);
        if (screen == null) {
            return "Screen not found.";
        }
        screen.setOrientation(orientation);
        return repository.updateDesign(screen) ? null : "Could not update screen.";
    }
}
