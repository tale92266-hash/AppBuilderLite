package com.nobi.appbuilder.codeeditor.search;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Find/replace inside one document, and project-wide search over
 * generated Java/XML files. Binary assets are never searched as text. */
public class CodeSearchManager {

    /** All matches in one document's text. */
    public List<SearchResult> findAll(String text, String query,
                                      SearchOptions options) {
        List<SearchResult> out = new ArrayList<>();
        if (text == null || query == null || query.isEmpty()) {
            return out;
        }
        String haystack = options.isCaseSensitive() ? text
                : text.toLowerCase();
        String needle = options.isCaseSensitive() ? query
                : query.toLowerCase();
        int index = 0;
        while (true) {
            int found = haystack.indexOf(needle, index);
            if (found < 0) {
                break;
            }
            if (options.isWholeWord() && !isWholeWord(text, found,
                    found + query.length())) {
                index = found + 1;
                continue;
            }
            int line = 1;
            int lineStart = 0;
            for (int i = 0; i < found; i++) {
                if (text.charAt(i) == '\n') {
                    line++;
                    lineStart = i + 1;
                }
            }
            int lineEnd = text.indexOf('\n', found);
            if (lineEnd < 0) {
                lineEnd = text.length();
            }
            out.add(new SearchResult(null, line, found - lineStart + 1,
                    found, found + query.length(),
                    text.substring(lineStart, lineEnd)));
            index = found + query.length();
        }
        return out;
    }

    /** Next match at or after fromOffset, wrapping to the start. */
    public SearchResult findNext(String text, String query,
                                 SearchOptions options, int fromOffset) {
        List<SearchResult> all = findAll(text, query, options);
        for (SearchResult result : all) {
            if (result.getStart() >= fromOffset) {
                return result;
            }
        }
        return all.isEmpty() ? null : all.get(0);
    }

    public SearchResult findPrevious(String text, String query,
                                     SearchOptions options, int fromOffset) {
        List<SearchResult> all = findAll(text, query, options);
        SearchResult previous = null;
        for (SearchResult result : all) {
            if (result.getStart() < fromOffset) {
                previous = result;
            } else {
                break;
            }
        }
        return previous == null && !all.isEmpty()
                ? all.get(all.size() - 1) : previous;
    }

    /** Project-wide search over .java/.xml/.gradle/.properties files. */
    public List<SearchResult> searchProject(File workspaceRoot, String query,
                                            SearchOptions options) {
        List<SearchResult> out = new ArrayList<>();
        if (workspaceRoot == null || !workspaceRoot.isDirectory()) {
            return out;
        }
        searchDirectory(workspaceRoot, query, options, out);
        return out;
    }

    private void searchDirectory(File dir, String query, SearchOptions options,
                                 List<SearchResult> out) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                searchDirectory(file, query, options, out);
                continue;
            }
            String name = file.getName().toLowerCase();
            if (!(name.endsWith(".java") || name.endsWith(".xml")
                    || name.endsWith(".gradle")
                    || name.endsWith(".properties"))) {
                continue;
            }
            if (file.length() > 2 * 1024 * 1024) {
                continue;
            }
            try {
                String text = new String(
                        java.nio.file.Files.readAllBytes(file.toPath()),
                        java.nio.charset.StandardCharsets.UTF_8);
                for (SearchResult hit : findAll(text, query, options)) {
                    out.add(new SearchResult(file, hit.getLine(),
                            hit.getColumn(), hit.getStart(), hit.getEnd(),
                            hit.getLineText()));
                }
            } catch (Exception ignored) {
            }
        }
    }

    private boolean isWholeWord(String text, int start, int end) {
        if (start > 0 && Character.isJavaIdentifierPart(text.charAt(start - 1))) {
            return false;
        }
        return end >= text.length()
                || !Character.isJavaIdentifierPart(text.charAt(end));
    }
}
