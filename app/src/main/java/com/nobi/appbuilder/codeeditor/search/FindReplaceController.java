package com.nobi.appbuilder.codeeditor.search;

import java.util.List;

/** Find/replace state for the current document. */
public class FindReplaceController {

    private final CodeSearchManager searchManager = new CodeSearchManager();
    private final SearchOptions options = new SearchOptions();
    private String query = "";
    private List<SearchResult> matches;
    private int currentIndex = -1;

    public SearchOptions getOptions() {
        return options;
    }

    public String getQuery() {
        return query;
    }

    public int getMatchCount() {
        return matches == null ? 0 : matches.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setQuery(String query, String text) {
        this.query = query == null ? "" : query;
        if (this.query.isEmpty()) {
            matches = null;
            currentIndex = -1;
            return;
        }
        matches = searchManager.findAll(text, this.query, options);
        currentIndex = matches.isEmpty() ? -1 : 0;
    }

    public SearchResult next(String text) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex + 1) % matches.size();
        return matches.get(currentIndex);
    }

    public SearchResult previous(String text) {
        if (matches == null || matches.isEmpty()) {
            return null;
        }
        currentIndex = (currentIndex - 1 + matches.size()) % matches.size();
        return matches.get(currentIndex);
    }

    public SearchResult current() {
        if (matches == null || matches.isEmpty() || currentIndex < 0) {
            return null;
        }
        return matches.get(currentIndex);
    }

    /** Replaces the current match; returns the new text or null. */
    public String replaceCurrent(String text, String replacement) {
        SearchResult match = current();
        if (match == null) {
            return null;
        }
        String out = text.substring(0, match.getStart()) + replacement
                + text.substring(match.getEnd());
        setQuery(query, out);
        return out;
    }

    /** Replaces every match; returns the new text. */
    public String replaceAll(String text, String replacement) {
        if (query.isEmpty()) {
            return text;
        }
        List<SearchResult> all = searchManager.findAll(text, query, options);
        StringBuilder out = new StringBuilder();
        int cursor = 0;
        for (SearchResult match : all) {
            out.append(text, cursor, match.getStart());
            out.append(replacement);
            cursor = match.getEnd();
        }
        out.append(text.substring(cursor));
        setQuery(query, out.toString());
        return out.toString();
    }
}
