package com.nobi.appbuilder.clipboard;

/**
 * The editor clipboard: holds one ClipboardData at a time. Owned by
 * the EditorActivity (one per editor session, never static), so a
 * copied component never leaks between projects.
 */
public class EditorClipboard {

    private ClipboardData data;

    public void set(ClipboardData data) {
        this.data = data;
    }

    public ClipboardData get() {
        return data;
    }

    public boolean hasContent() {
        return data != null && !data.isEmpty();
    }

    public void clear() {
        data = null;
    }
}
