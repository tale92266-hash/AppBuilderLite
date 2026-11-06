package com.nobi.appbuilder.codeeditor.search;

import java.io.File;

/** One search hit, with its file and line for navigation. */
public class SearchResult {

    private final File file;
    private final int line;
    private final int column;
    private final int start;
    private final int end;
    private final String lineText;

    public SearchResult(File file, int line, int column, int start, int end,
                        String lineText) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.start = start;
        this.end = end;
        this.lineText = lineText;
    }

    public File getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getLineText() {
        return lineText;
    }
}
