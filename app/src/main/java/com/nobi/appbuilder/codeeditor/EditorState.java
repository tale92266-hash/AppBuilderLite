package com.nobi.appbuilder.codeeditor;

/** Per-tab editor state: cursor, selection, scroll, dirty flag. */
public class EditorState {

    private EditorCursor cursor = new EditorCursor(0, 1, 1);
    private EditorSelection selection = new EditorSelection(0, 0);
    private int scrollX;
    private int scrollY;
    private boolean dirty;

    public EditorCursor getCursor() {
        return cursor;
    }

    public void setCursor(EditorCursor cursor) {
        this.cursor = cursor;
    }

    public EditorSelection getSelection() {
        return selection;
    }

    public void setSelection(EditorSelection selection) {
        this.selection = selection;
    }

    public int getScrollX() {
        return scrollX;
    }

    public void setScrollX(int scrollX) {
        this.scrollX = scrollX;
    }

    public int getScrollY() {
        return scrollY;
    }

    public void setScrollY(int scrollY) {
        this.scrollY = scrollY;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
