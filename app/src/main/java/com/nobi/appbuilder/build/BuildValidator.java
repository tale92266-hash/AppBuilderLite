package com.nobi.appbuilder.build;

import android.content.Context;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationError;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.ProjectGenerator;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.project.ProjectSettingsManager;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayList;
import java.util.List;

/** Validates a build configuration and the generated project it
 * would build, using the real Phase 7 generator — never a guess. */
public class BuildValidator {

    private final Context appContext;

    public BuildValidator(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Configuration problems; empty means valid. */
    public List<String> validateConfiguration(BuildConfiguration config) {
        return config.validate();
    }

    /**
     * Runs Phase 7 generation for the project and returns every
     * problem. An empty list means the generated project is
     * structurally sound; it does not mean a Gradle build would pass.
     */
    public List<String> validateProject(String projectId,
                                        BuildConfiguration config) {
        List<String> problems = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(appContext);
        try {
            DocumentRepository repository = new DocumentRepository(dbHelper);
            IRDocument document = repository.loadDocument(projectId);
            if (document == null || document.getProject() == null) {
                problems.add("Project could not be loaded");
                return problems;
            }
            GenerationContext context = new GenerationContext(document,
                    document.getProject(), config.getApplicationId(),
                    config.getMinSdk(), config.getTargetSdk(),
                    config.getVersionCode(), config.getVersionName(),
                    new AssetRepository(dbHelper));
            GenerationResult result =
                    new ProjectGenerator(context).generate();
            if (!result.isSuccess()) {
                for (GenerationError error : result.getReport().getErrors()) {
                    problems.add(error.getMessage());
                }
            }
            for (GenerationError warning : result.getReport().getWarnings()) {
                problems.add("Warning: " + warning.getMessage());
            }
        } catch (Exception e) {
            problems.add("Validation failed: " + e.getMessage());
        } finally {
            dbHelper.close();
        }
        return problems;
    }
}
