package com.nobi.appbuilder.codeeditor.formatting;

/** Outcome of a format operation. */
public class FormattingResult {

    private final boolean changed;
    private final String text;
    private final int linesChanged;
    private final String error;

    public FormattingResult(boolean changed, String text, int linesChanged) {
        this.changed = changed;
        this.text = text;
        this.linesChanged = linesChanged;
        this.error = null;
    }

    public FormattingResult(String error) {
        this.changed = false;
        this.text = null;
        this.linesChanged = 0;
        this.error = error;
    }

    public boolean isChanged() {
        return changed;
    }

    public String getText() {
        return text;
    }

    public int getLinesChanged() {
        return linesChanged;
    }

    public String getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}
