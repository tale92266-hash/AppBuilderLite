package com.nobi.appbuilder.project;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.repository.DocumentRepository;

/**
 * Exports a whole project to its IR JSON document (the same format
 * the import manager reads), ready to write to a Storage Access
 * Framework file or share. Export never touches build files.
 */
public class ProjectExportManager {

    private final DocumentRepository documentRepository;

    public ProjectExportManager(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
    }

    /** Returns the project JSON, or null when the project does not exist. */
    public String exportToJson(String projectId) {
        IRDocument document = documentRepository.loadDocument(projectId);
        if (document == null) {
            return null;
        }
        return IRSerializer.serializeDocument(document);
    }

    /** Suggested file name for the exported project. */
    public static String fileNameFor(String appName) {
        String safe = appName == null ? "project"
                : appName.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        if (safe.isEmpty() || "-".equals(safe)) {
            safe = "project";
        }
        return safe + ".appbuilder.json";
    }
}
