package com.nobi.appbuilder.export.validation;

import com.nobi.appbuilder.export.PackagingError;
import com.nobi.appbuilder.generator.GenerationResult;

import java.util.ArrayList;
import java.util.List;

/** Required-file and structural checks over the generated output. */
public class ProjectStructureValidator {

    public List<PackagingError> validate(GenerationResult result,
                                         String packageName) {
        List<PackagingError> problems = new ArrayList<>();
        boolean hasLayout = false;
        boolean hasJava = false;
        boolean hasManifest = false;
        boolean hasAppGradle = false;
        boolean hasSettingsGradle = false;
        for (String path : result.getAllPaths()) {
            if (path.contains("/layout/") && path.endsWith(".xml")) {
                hasLayout = true;
            }
            if (path.endsWith(".java")) {
                hasJava = true;
            }
            if (path.endsWith("AndroidManifest.xml")) {
                hasManifest = true;
            }
            if (path.equals("app/build.gradle")) {
                hasAppGradle = true;
            }
            if (path.equals("settings.gradle")) {
                hasSettingsGradle = true;
            }
            if (path.endsWith(".java") && path.contains(" ")) {
                problems.add(PackagingError.error("structure", path,
                        "Java file path contains a space"));
            }
        }
        if (!hasLayout) {
            problems.add(PackagingError.error("structure", null,
                    "No layout XML files were generated"));
        }
        if (!hasJava) {
            problems.add(PackagingError.error("structure", null,
                    "No Java files were generated"));
        }
        if (!hasManifest) {
            problems.add(PackagingError.error("structure", null,
                    "AndroidManifest.xml was not generated"));
        }
        if (!hasAppGradle) {
            problems.add(PackagingError.error("structure", null,
                    "app/build.gradle was not generated"));
        }
        if (!hasSettingsGradle) {
            problems.add(PackagingError.error("structure", null,
                    "settings.gradle was not generated"));
        }
        if (packageName == null || packageName.isEmpty()) {
            problems.add(PackagingError.error("structure", null,
                    "Package name is missing"));
        }
        return problems;
    }
}
