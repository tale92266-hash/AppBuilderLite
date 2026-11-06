package com.nobi.appbuilder.codeeditor.syntax;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * State-aware Java tokenizer — NOT regex alone. It tracks strings,
 * char literals, line comments and block comments across the whole
 * range, so a brace inside a string never breaks indentation or
 * bracket matching, and a "/*" inside a string never starts a comment.
 */
public class JavaSyntaxHighlighter implements SyntaxHighlighter {

    private static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
            "abstract", "assert", "boolean", "break", "byte", "case",
            "catch", "char", "class", "const", "continue", "default", "do",
            "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int",
            "interface", "long", "native", "new", "package", "private",
            "protected", "public", "return", "short", "static", "strictfp",
            "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "var", "record",
            "sealed", "permits", "true", "false", "null"));
    private static final Set<String> TYPES = new HashSet<>(Arrays.asList(
            "String", "Object", "List", "Map", "Set", "ArrayList",
            "HashMap", "Integer", "Boolean", "Long", "Double", "Float",
            "Character", "Byte", "Short", "Void", "Exception", "RuntimeException"));

    private enum State {
        CODE, STRING, CHAR, LINE_COMMENT, BLOCK_COMMENT
    }

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.JAVA;
    }

    @Override
    public List<SyntaxToken> tokenize(String text, int startOffset,
                                      int endOffset) {
        List<SyntaxToken> tokens = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return tokens;
        }
        int start = Math.max(0, startOffset);
        int end = Math.min(text.length(), endOffset);
        // Determine the state at `start` by scanning from the top.
        State state = stateAt(text, start);
        int i = start;
        int tokenStart = start;
        SyntaxTokenType currentType = typeForState(state);
        while (i < end) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            if (state == State.CODE) {
                if (c == '/' && next == '/') {
                    flush(tokens, tokenStart, i, currentType);
                    state = State.LINE_COMMENT;
                    tokenStart = i;
                    currentType = SyntaxTokenType.COMMENT;
                    i += 2;
                    continue;
                }
                if (c == '/' && next == '*') {
                    flush(tokens, tokenStart, i, currentType);
                    state = State.BLOCK_COMMENT;
                    tokenStart = i;
                    currentType = SyntaxTokenType.COMMENT;
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    flush(tokens, tokenStart, i, currentType);
                    state = State.STRING;
                    tokenStart = i;
                    currentType = SyntaxTokenType.STRING;
                    i++;
                    continue;
                }
                if (c == '\'') {
                    flush(tokens, tokenStart, i, currentType);
                    state = State.CHAR;
                    tokenStart = i;
                    currentType = SyntaxTokenType.CHAR;
                    i++;
                    continue;
                }
                if (c == '@') {
                    flush(tokens, tokenStart, i, currentType);
                    int j = i + 1;
                    while (j < end && Character.isJavaIdentifierPart(
                            text.charAt(j))) {
                        j++;
                    }
                    tokens.add(new SyntaxToken(i, j,
                            SyntaxTokenType.ANNOTATION));
                    i = j;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
                if (Character.isJavaIdentifierStart(c)) {
                    flush(tokens, tokenStart, i, currentType);
                    int j = i;
                    while (j < end
                            && Character.isJavaIdentifierPart(text.charAt(j))) {
                        j++;
                    }
                    String word = text.substring(i, j);
                    SyntaxTokenType type = SyntaxTokenType.PLAIN;
                    if (KEYWORDS.contains(word)) {
                        type = SyntaxTokenType.KEYWORD;
                    } else if (TYPES.contains(word)) {
                        type = SyntaxTokenType.TYPE;
                    } else if (j < end && text.charAt(j) == '(') {
                        type = SyntaxTokenType.METHOD;
                    } else if (!word.isEmpty()
                            && Character.isUpperCase(word.charAt(0))) {
                        type = SyntaxTokenType.CLASS_NAME;
                    }
                    tokens.add(new SyntaxToken(i, j, type));
                    i = j;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
                if (Character.isDigit(c)) {
                    flush(tokens, tokenStart, i, currentType);
                    int j = i;
                    while (j < end && (Character.isDigit(text.charAt(j))
                            || text.charAt(j) == '.'
                            || text.charAt(j) == 'f'
                            || text.charAt(j) == 'L'
                            || text.charAt(j) == 'x'
                            || (text.charAt(j) >= 'a'
                            && text.charAt(j) <= 'f')
                            || (text.charAt(j) >= 'A'
                            && text.charAt(j) <= 'F'))) {
                        j++;
                    }
                    tokens.add(new SyntaxToken(i, j,
                            SyntaxTokenType.NUMBER));
                    i = j;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
            } else if (state == State.STRING) {
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == '"') {
                    i++;
                    tokens.add(new SyntaxToken(tokenStart, i,
                            SyntaxTokenType.STRING));
                    state = State.CODE;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
            } else if (state == State.CHAR) {
                if (c == '\\') {
                    i += 2;
                    continue;
                }
                if (c == '\'') {
                    i++;
                    tokens.add(new SyntaxToken(tokenStart, i,
                            SyntaxTokenType.CHAR));
                    state = State.CODE;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
            } else if (state == State.LINE_COMMENT) {
                if (c == '\n') {
                    tokens.add(new SyntaxToken(tokenStart, i,
                            SyntaxTokenType.COMMENT));
                    state = State.CODE;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                }
            } else if (state == State.BLOCK_COMMENT) {
                if (c == '*' && next == '/') {
                    i += 2;
                    tokens.add(new SyntaxToken(tokenStart, i,
                            SyntaxTokenType.COMMENT));
                    state = State.CODE;
                    tokenStart = i;
                    currentType = SyntaxTokenType.PLAIN;
                    continue;
                }
            }
            i++;
        }
        flush(tokens, tokenStart, end,
                state == State.CODE ? currentType : typeForState(state));
        return tokens;
    }

    /** State at an offset, found by scanning from the document start. */
    private State stateAt(String text, int offset) {
        State state = State.CODE;
        int i = 0;
        while (i < offset && i < text.length()) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            switch (state) {
                case CODE:
                    if (c == '/' && next == '/') {
                        state = State.LINE_COMMENT;
                        i++;
                    } else if (c == '/' && next == '*') {
                        state = State.BLOCK_COMMENT;
                        i++;
                    } else if (c == '"') {
                        state = State.STRING;
                    } else if (c == '\'') {
                        state = State.CHAR;
                    }
                    break;
                case STRING:
                    if (c == '\\') {
                        i++;
                    } else if (c == '"') {
                        state = State.CODE;
                    }
                    break;
                case CHAR:
                    if (c == '\\') {
                        i++;
                    } else if (c == '\'') {
                        state = State.CODE;
                    }
                    break;
                case LINE_COMMENT:
                    if (c == '\n') {
                        state = State.CODE;
                    }
                    break;
                case BLOCK_COMMENT:
                    if (c == '*' && next == '/') {
                        state = State.CODE;
                        i++;
                    }
                    break;
            }
            i++;
        }
        return state;
    }

    private SyntaxTokenType typeForState(State state) {
        switch (state) {
            case STRING:
                return SyntaxTokenType.STRING;
            case CHAR:
                return SyntaxTokenType.CHAR;
            case LINE_COMMENT:
            case BLOCK_COMMENT:
                return SyntaxTokenType.COMMENT;
            default:
                return SyntaxTokenType.PLAIN;
        }
    }

    private void flush(List<SyntaxToken> tokens, int start, int end,
                       SyntaxTokenType type) {
        if (end > start) {
            tokens.add(new SyntaxToken(start, end, type));
        }
    }
}
