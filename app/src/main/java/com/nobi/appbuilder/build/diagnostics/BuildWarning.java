package com.nobi.appbuilder.build.diagnostics;

/** One build warning — the build may still have succeeded. */
public class BuildWarning {

    private final String category;
    private final String message;
    private final String file;
    private final int line;

    public BuildWarning(String category, String message, String file,
                        int line) {
        this.category = category;
        this.message = message;
        this.file = file;
        this.line = line;
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

    @Override
    public String toString() {
        return category + ": " + message;
    }
}
