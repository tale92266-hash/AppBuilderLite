package com.nobi.appbuilder.codeeditor.navigation;

/**
 * Finds the bracket matching the one at the cursor, skipping strings,
 * char literals and comments — a brace inside a string never matches.
 */
public class BracketMatcher {

    /** Offset of the matching bracket, or -1 when there is none. */
    public static int findMatching(String text, int offset) {
        if (text == null || text.isEmpty()
                || offset < 0 || offset >= text.length()) {
            return -1;
        }
        char bracket = text.charAt(offset);
        char open;
        char close;
        int direction;
        switch (bracket) {
            case '(':
                open = '(';
                close = ')';
                direction = 1;
                break;
            case ')':
                open = '(';
                close = ')';
                direction = -1;
                break;
            case '{':
                open = '{';
                close = '}';
                direction = 1;
                break;
            case '}':
                open = '{';
                close = '}';
                direction = -1;
                break;
            case '[':
                open = '[';
                close = ']';
                direction = 1;
                break;
            case ']':
                open = '[';
                close = ']';
                direction = -1;
                break;
            default:
                return -1;
        }
        int depth = 0;
        int i = offset;
        while (i >= 0 && i < text.length()) {
            if (!isInCode(text, i)) {
                i += direction;
                continue;
            }
            char c = text.charAt(i);
            if (c == open) {
                depth += direction;
            } else if (c == close) {
                depth -= direction;
            }
            if (depth == 0 && i != offset) {
                return i;
            }
            i += direction;
        }
        return -1;
    }

    /** True when the character at offset is real code (not inside a
     * string, char literal or comment). */
    public static boolean isInCode(String text, int offset) {
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i <= offset && i < text.length(); i++) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
            } else if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
                    i++;
                }
            } else if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
            } else if (inChar) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                }
            } else {
                if (c == '/' && next == '/') {
                    inLineComment = true;
                    i++;
                } else if (c == '/' && next == '*') {
                    inBlockComment = true;
                    i++;
                } else if (c == '"') {
                    inString = true;
                } else if (c == '\'') {
                    inChar = true;
                }
            }
            if (i == offset) {
                return !inString && !inChar && !inLineComment
                        && !inBlockComment;
            }
        }
        return true;
    }
}
