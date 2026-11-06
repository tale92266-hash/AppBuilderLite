package com.nobi.appbuilder.codeeditor;

/** A text selection range (start inclusive, end exclusive). */
public class EditorSelection {

    private final int start;
    private final int end;

    public EditorSelection(int start, int end) {
        this.start = Math.min(start, end);
        this.end = Math.max(start, end);
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int length() {
        return end - start;
    }

    public boolean isEmpty() {
        return start == end;
    }

    public boolean contains(int offset) {
        return offset >= start && offset <= end;
    }
}
