package com.nobi.appbuilder.codeeditor.syntax;

/** Syntax colors for the light and dark editor themes. */
public class SyntaxTheme {

    public static final int LIGHT = 0;
    public static final int DARK = 1;

    private final int theme;

    public SyntaxTheme(int theme) {
        this.theme = theme;
    }

    public int colorFor(SyntaxTokenType type) {
        boolean dark = theme == DARK;
        switch (type) {
            case KEYWORD:
                return dark ? 0xFFC586C0 : 0xFF7C3AED;
            case TYPE:
            case CLASS_NAME:
                return dark ? 0xFF4EC9B0 : 0xFF267F99;
            case METHOD:
                return dark ? 0xFFDCDCAA : 0xFF795E26;
            case STRING:
                return dark ? 0xFFCE9178 : 0xFFA31515;
            case CHAR:
                return dark ? 0xFFCE9178 : 0xFFA31515;
            case NUMBER:
                return dark ? 0xFFB5CEA8 : 0xFF098658;
            case COMMENT:
                return dark ? 0xFF6A9955 : 0xFF008000;
            case ANNOTATION:
                return dark ? 0xFFDCDCAA : 0xFF795E26;
            case OPERATOR:
                return dark ? 0xFFD4D4D4 : 0xFF000000;
            case TAG:
                return dark ? 0xFF569CD6 : 0xFF0000FF;
            case ATTRIBUTE:
                return dark ? 0xFF9CDCFE : 0xFFFF0000;
            case ATTR_VALUE:
                return dark ? 0xFFCE9178 : 0xFF0000FF;
            case ENTITY:
                return dark ? 0xFFCE9178 : 0xFFA31515;
            case DECLARATION:
                return dark ? 0xFF808080 : 0xFF808080;
            default:
                return dark ? 0xFFD4D4D4 : 0xFF111827;
        }
    }

    public int getBackground() {
        return theme == DARK ? 0xFF1E1E1E : 0xFFFFFFFF;
    }

    public int getTextColor() {
        return theme == DARK ? 0xFFD4D4D4 : 0xFF111827;
    }

    public int getLineNumberColor() {
        return theme == DARK ? 0xFF858585 : 0xFF6B7280;
    }

    public int getCurrentLineColor() {
        return theme == DARK ? 0xFF2A2D2E : 0xFFF3F4F6;
    }
}
