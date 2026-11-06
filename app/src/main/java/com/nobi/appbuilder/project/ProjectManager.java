package com.nobi.appbuilder.project;

import com.nobi.appbuilder.autosave.ProjectBackupManager;
import com.nobi.appbuilder.autosave.RecoveryManager;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.model.ProjectModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Dashboard project management (Phase 5): recent projects, search,
 * sort, duplicate, rename, and safe delete. Deleting a project also
 * removes its recovery snapshot and backup; screens, assets and
 * settings disappear through the database's ON DELETE CASCADE.
 */
public class ProjectManager {

    public static final int SORT_RECENT = 0;
    public static final int SORT_NAME = 1;
    public static final int SORT_OLDEST = 2;

    private final ProjectRepository repository;
    private final RecoveryManager recoveryManager;
    private final ProjectBackupManager backupManager;

    public ProjectManager(ProjectRepository repository,
                          RecoveryManager recoveryManager,
                          ProjectBackupManager backupManager) {
        this.repository = repository;
        this.recoveryManager = recoveryManager;
        this.backupManager = backupManager;
    }

    public List<ProjectModel> getProjects(int sortMode) {
        List<ProjectModel> projects = repository.getAll();
        return sort(projects, sortMode);
    }

    public List<ProjectModel> searchProjects(String query, int sortMode) {
        List<ProjectModel> projects = query == null || query.trim().isEmpty()
                ? repository.getAll()
                : repository.search(query.trim());
        return sort(projects, sortMode);
    }

    private List<ProjectModel> sort(List<ProjectModel> projects, int sortMode) {
        List<ProjectModel> sorted = new ArrayList<>(projects);
        switch (sortMode) {
            case SORT_NAME:
                Collections.sort(sorted, Comparator.comparing(
                        ProjectModel::getAppName,
                        String.CASE_INSENSITIVE_ORDER));
                break;
            case SORT_OLDEST:
                Collections.sort(sorted, Comparator.comparingLong(
                        ProjectModel::getCreatedAt));
                break;
            case SORT_RECENT:
            default:
                Collections.sort(sorted, (a, b) -> Long.compare(
                        b.getUpdatedAt(), a.getUpdatedAt()));
                break;
        }
        return sorted;
    }

    public ProjectModel duplicateProject(String projectId) {
        return repository.duplicate(projectId);
    }

    /** Returns an error string, or null on success. */
    public String renameProject(String projectId, String newName) {
        ProjectModel project = repository.getById(projectId);
        if (project == null) {
            return "Project not found.";
        }
        if (!ProjectModel.isValidAppName(newName)) {
            return "Please enter a valid app name.";
        }
        project.setAppName(newName.trim());
        return repository.update(project) ? null : "Could not rename project.";
    }

    /** Deletes the project and its recovery data. Returns false on failure. */
    public boolean deleteProject(String projectId) {
        recoveryManager.deleteSnapshot(projectId);
        backupManager.deleteBackup(projectId);
        return repository.delete(projectId);
    }

    public int countScreens(String projectId) {
        return repository.countScreens(projectId);
    }
}
