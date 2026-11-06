package com.nobi.appbuilder.codeeditor.syntax;

/** Resolves a token's color from the active theme. */
public class SyntaxColorResolver {

    private SyntaxTheme theme;

    public SyntaxColorResolver(int themeId) {
        this.theme = new SyntaxTheme(themeId);
    }

    public void setTheme(int themeId) {
        this.theme = new SyntaxTheme(themeId);
    }

    public SyntaxTheme getTheme() {
        return theme;
    }

    public int colorFor(SyntaxTokenType type) {
        return theme.colorFor(type);
    }
}
