package com.nobi.appbuilder.project;

import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

/**
 * Imports a whole project from an IR JSON document (Storage Access
 * Framework file, shared file, or clipboard text). A fresh project id
 * is generated when the document has none or collides with an
 * existing project, so an import can never overwrite local work.
 */
public class ProjectImportManager {

    private final DocumentRepository documentRepository;

    public ProjectImportManager(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    public static class ImportResult {
        public ProjectModel project;
        public String error;
    }

    public ImportResult importFromJson(String json) {
        ImportResult result = new ImportResult();
        if (json == null || json.trim().isEmpty()) {
            result.error = "The file is empty.";
            return result;
        }
        IRDocument document;
        try {
            document = IRDeserializer.deserializeDocument(json);
        } catch (Exception e) {
            result.error = "Could not read this project file.";
            return result;
        }
        if (document == null || document.getProject() == null) {
            result.error = "This file does not contain a valid project.";
            return result;
        }
        ProjectModel project = document.getProject();
        if (project.getId() == null || project.getId().isEmpty()
                || documentRepository.loadDocument(project.getId()) != null) {
            String freshId = com.nobi.appbuilder.utils.IDGenerator.newProjectId();
            project.setId(freshId);
            for (ScreenModel screen : document.getScreens()) {
                screen.setProjectId(freshId);
            }
        }
        if (!documentRepository.saveDocument(document)) {
            result.error = "The project could not be saved.";
            return result;
        }
        result.project = project;
        return result;
    }
}
