package com.nobi.appbuilder.export;

import android.content.Context;

import com.nobi.appbuilder.export.report.ExportSummaryGenerator;
import com.nobi.appbuilder.generator.GenerationResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the complete Android Studio project inside a workspace:
 * directory structure, generated files, Gradle wrapper (genuine jar
 * from assets), proguard rules, styles, launcher icon and README.
 * Counts files by type for the report.
 */
public class AndroidStudioProjectBuilder {

    public static class BuildCounts {
        public int javaFiles;
        public int xmlFiles;
        public int resources;
        public int activities;
        public int totalFiles;
    }

    private final Context appContext;

    public AndroidStudioProjectBuilder(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /**
     * Builds the project. Problems (warnings) are collected, never
     * thrown — the caller decides whether they block the export.
     */
    public BuildCounts build(File workspaceRoot, ProjectPackagingConfig config,
                             GenerationResult result, List<String> problems) {
        BuildCounts counts = new BuildCounts();
        ProjectStructureBuilder structureBuilder = new ProjectStructureBuilder();
        ProjectStructureBuilder.Structure structure =
                structureBuilder.createStructure(workspaceRoot,
                        config.getPackageName());
        if (structure == null) {
            problems.add("Could not create the Android Studio directory structure");
            return counts;
        }
        ProjectFileManager fileManager = new ProjectFileManager(workspaceRoot);
        ProjectCopyManager copyManager =
                new ProjectCopyManager(appContext, fileManager);

        problems.addAll(copyManager.copyGeneratedFiles(result));

        String wrapperProblem =
                copyManager.copyWrapperFiles(config.isIncludeWrapper());
        if (wrapperProblem != null) {
            problems.add(wrapperProblem);
        }
        try {
            copyManager.copyProguardRules();
        } catch (Exception e) {
            problems.add("proguard-rules.pro: " + e.getMessage());
        }
        try {
            copyManager.copyStylesXml();
        } catch (Exception e) {
            problems.add("styles.xml: " + e.getMessage());
        }
        try {
            copyManager.copyLauncherIcon();
        } catch (Exception e) {
            problems.add("launcher icon: " + e.getMessage());
        }
        try {
            String readme = ExportSummaryGenerator.generate(config, result);
            copyManager.copyReadme(readme);
        } catch (Exception e) {
            problems.add("README.md: " + e.getMessage());
        }

        for (String path : fileManager.getWrittenPaths()) {
            counts.totalFiles++;
            if (path.endsWith(".java")) {
                counts.javaFiles++;
            } else if (path.endsWith(".xml")) {
                counts.xmlFiles++;
            } else if (path.contains("/res/")) {
                counts.resources++;
            }
        }
        counts.activities = countActivities(result);
        return counts;
    }

    private int countActivities(GenerationResult result) {
        int count = 0;
        for (String path : result.getAllPaths()) {
            if (path.endsWith(".java")
                    && path.contains("/java/")
                    && !path.contains("$")) {
                count++;
            }
        }
        return count;
    }

    /** Files the validator requires to exist in the workspace. */
    public static List<String> requiredFiles(ProjectPackagingConfig config) {
        List<String> required = new ArrayList<>();
        required.add("settings.gradle");
        required.add("build.gradle");
        required.add("gradle.properties");
        required.add("app/build.gradle");
        required.add("app/proguard-rules.pro");
        required.add("app/src/main/AndroidManifest.xml");
        required.add("app/src/main/res/values/colors.xml");
        required.add("app/src/main/res/values/strings.xml");
        required.add("app/src/main/res/values/dimens.xml");
        required.add("app/src/main/res/values/themes.xml");
        required.add("app/src/main/res/values/styles.xml");
        required.add("README.md");
        if (config.isIncludeWrapper()) {
            required.add("gradle/wrapper/gradle-wrapper.properties");
        }
        return required;
    }
}
