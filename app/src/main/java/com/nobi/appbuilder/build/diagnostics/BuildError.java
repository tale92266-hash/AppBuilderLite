package com.nobi.appbuilder.build.diagnostics;

/** One confirmed build error, with the actual error text kept. */
public class BuildError {

    private final String category;
    private final String message;
    private final String file;
    private final int line;
    private final String logExcerpt;
    private final String suggestion;

    public BuildError(String category, String message, String file, int line,
                      String logExcerpt, String suggestion) {
        this.category = category;
        this.message = message;
        this.file = file;
        this.line = line;
        this.logExcerpt = logExcerpt;
        this.suggestion = suggestion;
    }

    public String getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public String getLogExcerpt() {
        return logExcerpt;
    }

    public String getSuggestion() {
        return suggestion;
    }

    @Override
    public String toString() {
        return category + ": " + message
                + (file != null ? " (" + file
                + (line > 0 ? ":" + line : "") + ")" : "");
    }
}
