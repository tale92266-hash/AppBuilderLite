package com.nobi.appbuilder.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The outcome of a generation run: every error and warning, the
 * files produced, and per-stage counts. A run with errors is never
 * reported as successful, even when some files were produced.
 */
public class GenerationReport {

    private final List<GenerationError> errors = new ArrayList<>();
    private final List<GenerationError> warnings = new ArrayList<>();
    private final Map<String, Integer> filesByType = new LinkedHashMap<>();
    private int screensGenerated;
    private int componentsGenerated;

    public void addError(GenerationError error) {
        if (error.isError()) {
            errors.add(error);
        } else {
            warnings.add(error);
        }
    }

    public void addAll(List<GenerationError> problems) {
        for (GenerationError problem : problems) {
            addError(problem);
        }
    }

    public List<GenerationError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<GenerationError> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public List<GenerationError> getAllProblems() {
        List<GenerationError> all = new ArrayList<>(errors);
        all.addAll(warnings);
        return all;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean isSuccess() {
        return errors.isEmpty();
    }

    public void recordFile(String relativePath) {
        String type = "other";
        if (relativePath.endsWith(".xml")) {
            type = relativePath.contains("/layout/") ? "layout" : "xml";
        } else if (relativePath.endsWith(".java")) {
            type = "java";
        } else if (relativePath.endsWith(".gradle")) {
            type = "gradle";
        }
        filesByType.put(type, filesByType.getOrDefault(type, 0) + 1);
    }

    public Map<String, Integer> getFilesByType() {
        return new LinkedHashMap<>(filesByType);
    }

    public int getTotalFiles() {
        int total = 0;
        for (int count : filesByType.values()) {
            total += count;
        }
        return total;
    }

    public int getScreensGenerated() {
        return screensGenerated;
    }

    public void setScreensGenerated(int screensGenerated) {
        this.screensGenerated = screensGenerated;
    }

    public int getComponentsGenerated() {
        return componentsGenerated;
    }

    public void setComponentsGenerated(int componentsGenerated) {
        this.componentsGenerated = componentsGenerated;
    }

    /** One-line summary plus every problem, for the Code Studio UI. */
    public String toText() {
        StringBuilder out = new StringBuilder();
        out.append(isSuccess() ? "Generation succeeded" : "Generation failed");
        out.append(" — ").append(getTotalFiles()).append(" files, ")
                .append(screensGenerated).append(" screens, ")
                .append(componentsGenerated).append(" components.\n");
        for (GenerationError problem : getAllProblems()) {
            out.append(problem.toString()).append("\n");
        }
        return out.toString().trim();
    }
}
