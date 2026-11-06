package com.nobi.appbuilder.codeeditor;

/** Cursor position: offset plus derived line/column (1-based). */
public class EditorCursor {

    private int offset;
    private int line;
    private int column;

    public EditorCursor(int offset, int line, int column) {
        this.offset = offset;
        this.line = line;
        this.column = column;
    }

    public static EditorCursor fromText(String text, int offset) {
        int safeOffset = Math.max(0, Math.min(offset,
                text == null ? 0 : text.length()));
        int line = 1;
        int lineStart = 0;
        for (int i = 0; i < safeOffset && text != null; i++) {
            if (text.charAt(i) == '\n') {
                line++;
                lineStart = i + 1;
            }
        }
        return new EditorCursor(safeOffset, line,
                safeOffset - lineStart + 1);
    }

    public int getOffset() {
        return offset;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
