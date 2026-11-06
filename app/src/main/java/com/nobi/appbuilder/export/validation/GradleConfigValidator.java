package com.nobi.appbuilder.export.validation;

import com.nobi.appbuilder.export.PackagingError;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.templates.ProjectTemplate;

import java.util.ArrayList;
import java.util.List;

/** Gradle configuration checks: files, versions, SDK consistency. */
public class GradleConfigValidator {

    public List<PackagingError> validate(GenerationResult result,
                                         String packageName,
                                         int minSdk, int targetSdk) {
        List<PackagingError> problems = new ArrayList<>();
        String settings = result.getTextFile("settings.gradle");
        if (settings == null) {
            problems.add(PackagingError.error("gradle", "settings.gradle",
                    "settings.gradle was not generated"));
        } else if (!settings.contains("include ':app'")) {
            problems.add(PackagingError.error("gradle", "settings.gradle",
                    "settings.gradle does not include ':app'"));
        }
        String appGradle = result.getTextFile("app/build.gradle");
        if (appGradle == null) {
            problems.add(PackagingError.error("gradle", "app/build.gradle",
                    "app/build.gradle was not generated"));
            return problems;
        }
        if (!appGradle.contains("namespace '" + packageName + "'")) {
            problems.add(PackagingError.error("gradle", "app/build.gradle",
                    "namespace does not match package " + packageName));
        }
        if (!appGradle.contains("applicationId \"" + packageName + "\"")) {
            problems.add(PackagingError.error("gradle", "app/build.gradle",
                    "applicationId does not match package " + packageName));
        }
        if (!appGradle.contains("minSdk " + minSdk)) {
            problems.add(PackagingError.warning("gradle", "app/build.gradle",
                    "minSdk in app/build.gradle differs from export config"));
        }
        if (!appGradle.contains("targetSdk " + targetSdk)) {
            problems.add(PackagingError.warning("gradle", "app/build.gradle",
                    "targetSdk in app/build.gradle differs from export config"));
        }
        String rootGradle = result.getTextFile("build.gradle");
        if (rootGradle == null) {
            problems.add(PackagingError.error("gradle", "build.gradle",
                    "Root build.gradle was not generated"));
        } else if (!rootGradle.contains(ProjectTemplate.AGP_VERSION)) {
            problems.add(PackagingError.error("gradle", "build.gradle",
                    "Android Gradle Plugin version "
                            + ProjectTemplate.AGP_VERSION + " not found"));
        }
        String wrapperProps =
                result.getTextFile("gradle/wrapper/gradle-wrapper.properties");
        if (wrapperProps == null) {
            problems.add(PackagingError.warning("gradle",
                    "gradle/wrapper/gradle-wrapper.properties",
                    "Wrapper properties missing — `gradle wrapper` must be "
                            + "run once before building"));
        } else if (!wrapperProps.contains(ProjectTemplate.GRADLE_VERSION)) {
            problems.add(PackagingError.warning("gradle",
                    "gradle/wrapper/gradle-wrapper.properties",
                    "Wrapper distribution is not Gradle "
                            + ProjectTemplate.GRADLE_VERSION));
        }
        if (minSdk < 21 || targetSdk < minSdk || targetSdk > 35) {
            problems.add(PackagingError.error("gradle", "app/build.gradle",
                    "Invalid SDK configuration: minSdk=" + minSdk
                            + " targetSdk=" + targetSdk));
        }
        return problems;
    }
}
