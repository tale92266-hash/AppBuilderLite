package com.nobi.appbuilder.codeeditor.syntax;

/** One highlighted span: [start, end) with a token type. */
public class SyntaxToken {

    private final int start;
    private final int end;
    private final SyntaxTokenType type;

    public SyntaxToken(int start, int end, SyntaxTokenType type) {
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public SyntaxTokenType getType() {
        return type;
    }

    public int length() {
        return end - start;
    }
}
