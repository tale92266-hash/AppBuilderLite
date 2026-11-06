package com.nobi.appbuilder.export.validation;

import com.nobi.appbuilder.export.PackagingError;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.java.JavaSourceValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Orchestrates every pre-ZIP validation: structure, resources,
 * manifest, Gradle, and a Java structural check over the generated
 * sources. Structural validation is not a Gradle build test — the
 * report says so plainly.
 */
public class ExportValidator {

    /** Runs all validators; returns every problem found. */
    public List<PackagingError> validate(GenerationResult result,
                                         ProjectPackagingConfig config) {
        List<PackagingError> problems = new ArrayList<>();
        problems.addAll(new ProjectStructureValidator()
                .validate(result, config.getPackageName()));
        problems.addAll(new ResourceValidator().validate(result));
        problems.addAll(new ManifestValidator().validate(result));
        problems.addAll(new GradleConfigValidator().validate(result,
                config.getPackageName(), config.getMinSdk(),
                config.getTargetSdk()));
        problems.addAll(validateJava(result));
        return problems;
    }

    /** Java structural checks: no empty files, braces balance, package match. */
    private List<PackagingError> validateJava(GenerationResult result) {
        List<PackagingError> problems = new ArrayList<>();
        Set<String> knownIds = new HashSet<>();
        Set<String> knownLayouts = new HashSet<>();
        for (String path : result.getAllPaths()) {
            String text = result.getTextFile(path);
            if (text == null) {
                continue;
            }
            if (path.contains("/layout/") && path.endsWith(".xml")) {
                knownLayouts.add(fileBaseName(path));
                Matcher ids = Pattern.compile("@\\+id/([A-Za-z0-9_]+)")
                        .matcher(text);
                while (ids.find()) {
                    knownIds.add(ids.group(1));
                }
            }
        }
        for (String path : result.getAllPaths()) {
            if (!path.endsWith(".java")) {
                continue;
            }
            String source = result.getTextFile(path);
            if (source == null || source.trim().isEmpty()) {
                problems.add(PackagingError.error("java", path,
                        "Generated Java file is empty"));
                continue;
            }
            String className = fileBaseName(path);
            String packageName = packageOf(path);
            for (String problem : JavaSourceValidator.validate(packageName,
                    className, source, knownIds, knownLayouts)) {
                problems.add(PackagingError.error("java", path, problem));
            }
        }
        return problems;
    }

    private String packageOf(String javaPath) {
        // app/src/main/java/com/example/app/MainActivity.java
        String marker = "app/src/main/java/";
        int start = javaPath.indexOf(marker);
        if (start < 0) {
            return "";
        }
        String rest = javaPath.substring(start + marker.length());
        int slash = rest.lastIndexOf('/');
        if (slash < 0) {
            return "";
        }
        return rest.substring(0, slash).replace('/', '.');
    }

    private String fileBaseName(String path) {
        int slash = path.lastIndexOf('/');
        String name = slash >= 0 ? path.substring(slash + 1) : path;
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(0, dot) : name;
    }
}
