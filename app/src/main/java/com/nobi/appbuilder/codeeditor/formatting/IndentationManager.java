package com.nobi.appbuilder.codeeditor.formatting;

/** Computes indentation for new lines and dedents. */
public class IndentationManager {

    private final int indentWidth;

    public IndentationManager(int indentWidth) {
        this.indentWidth = indentWidth;
    }

    public String indentString(int level) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < level * indentWidth; i++) {
            out.append(' ');
        }
        return out.toString();
    }

    /** Leading whitespace of a line. */
    public String leadingWhitespace(String line) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ' || c == '\t') {
                out.append(c);
            } else {
                break;
            }
        }
        return out.toString();
    }

    public int indentLevelOf(String line) {
        int spaces = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == ' ') {
                spaces++;
            } else if (c == '\t') {
                spaces += indentWidth;
            } else {
                break;
            }
        }
        return spaces / indentWidth;
    }

    /** Indent for a new line after this one (Java rules). */
    public String indentForNewLine(String previousLine, int currentLevel) {
        String base = leadingWhitespace(previousLine);
        String trimmed = previousLine.trim();
        if (trimmed.endsWith("{") || trimmed.endsWith("(")
                || trimmed.endsWith("[")) {
            return base + indentString(1);
        }
        return base;
    }

    /** Indent when the user just typed a closing brace. */
    public String indentForClosingBrace(String previousLine,
                                        int currentLevel) {
        int level = Math.max(0, currentLevel - 1);
        return indentString(level);
    }
}
