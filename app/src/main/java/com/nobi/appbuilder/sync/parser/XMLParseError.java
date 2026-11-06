package com.nobi.appbuilder.sync.parser;

/** One XML parsing problem, with its location when known. */
public class XMLParseError {

    private final String message;
    private final int line;
    private final int column;

    public XMLParseError(String message, int line, int column) {
        this.message = message;
        this.line = line;
        this.column = column;
    }

    public String getMessage() {
        return message;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    @Override
    public String toString() {
        return message + (line > 0 ? " (line " + line + ")" : "");
    }
}
