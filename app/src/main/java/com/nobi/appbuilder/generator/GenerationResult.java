package com.nobi.appbuilder.generator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Everything one generation run produced: text files (relative path
 * -&gt; content, in deterministic order), binary files (image assets),
 * and the report. Text and binary files are kept apart because
 * assets cannot round-trip through a String.
 */
public class GenerationResult {

    private final Map<String, String> textFiles = new LinkedHashMap<>();
    private final Map<String, byte[]> binaryFiles = new LinkedHashMap<>();
    private final GenerationReport report;

    public GenerationResult(GenerationReport report) {
        this.report = report;
    }

    public void addTextFile(String relativePath, String content) {
        textFiles.put(relativePath, content);
        report.recordFile(relativePath);
    }

    public void addBinaryFile(String relativePath, byte[] content) {
        binaryFiles.put(relativePath, content);
        report.recordFile(relativePath);
    }

    public Map<String, String> getTextFiles() {
        return new LinkedHashMap<>(textFiles);
    }

    public Map<String, byte[]> getBinaryFiles() {
        return new LinkedHashMap<>(binaryFiles);
    }

    /** Every file path, text first then binary, sorted for the UI. */
    public List<String> getAllPaths() {
        List<String> paths = new ArrayList<>(textFiles.keySet());
        paths.addAll(binaryFiles.keySet());
        java.util.Collections.sort(paths);
        return paths;
    }

    public String getTextFile(String relativePath) {
        return textFiles.get(relativePath);
    }

    public boolean hasFile(String relativePath) {
        return textFiles.containsKey(relativePath)
                || binaryFiles.containsKey(relativePath);
    }

    public GenerationReport getReport() {
        return report;
    }

    public boolean isSuccess() {
        return report.isSuccess();
    }
}
