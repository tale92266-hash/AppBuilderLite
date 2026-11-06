package com.nobi.appbuilder.codeeditor.search;

/** Find/replace options. */
public class SearchOptions {

    private boolean caseSensitive;
    private boolean wholeWord;

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public boolean isWholeWord() {
        return wholeWord;
    }

    public void setWholeWord(boolean wholeWord) {
        this.wholeWord = wholeWord;
    }
}
