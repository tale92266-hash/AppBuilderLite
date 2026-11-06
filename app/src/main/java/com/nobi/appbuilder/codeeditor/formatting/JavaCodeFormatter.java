package com.nobi.appbuilder.codeeditor.formatting;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

/**
 * Brace-aware Java formatter. It walks the text tracking strings,
 * chars and comments, so a "{" inside a string literal never changes
 * indentation. Comments and string contents are preserved verbatim.
 */
public class JavaCodeFormatter implements CodeFormatter {

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.JAVA;
    }

    @Override
    public FormattingResult format(String text, int indentWidth) {
        return formatRange(text, 0, lineCount(text) - 1, indentWidth);
    }

    @Override
    public FormattingResult formatRange(String text, int startLine,
                                        int endLine, int indentWidth) {
        if (text == null) {
            return new FormattingResult("No text to format");
        }
        String[] lines = text.split("\n", -1);
        IndentationManager indent = new IndentationManager(indentWidth);
        int level = 0;
        boolean inBlockComment = false;
        int changed = 0;
        // Compute the level at startLine by scanning earlier lines.
        for (int i = 0; i < startLine && i < lines.length; i++) {
            level = scanLine(lines[i], level, new boolean[]{inBlockComment});
            inBlockComment = scanBlockComment(lines[i], inBlockComment);
        }
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (i >= startLine && i <= endLine) {
                String trimmed = line.trim();
                String formatted;
                if (trimmed.isEmpty()) {
                    formatted = "";
                } else {
                    int effectiveLevel = level;
                    if (trimmed.startsWith("}")
                            || trimmed.startsWith(")")
                            || trimmed.startsWith("]")) {
                        effectiveLevel = Math.max(0, level - 1);
                    }
                    formatted = indent.indentString(effectiveLevel) + trimmed;
                }
                if (!formatted.equals(line)) {
                    changed++;
                }
                out.append(formatted);
                level = scanLine(line, level, new boolean[]{inBlockComment});
                inBlockComment = scanBlockComment(line, inBlockComment);
            } else {
                out.append(line);
                level = scanLine(line, level, new boolean[]{inBlockComment});
                inBlockComment = scanBlockComment(line, inBlockComment);
            }
            if (i < lines.length - 1) {
                out.append("\n");
            }
        }
        return new FormattingResult(changed > 0, out.toString(), changed);
    }

    private int lineCount(String text) {
        int count = 1;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                count++;
            }
        }
        return count;
    }

    /** Brace delta of one line, ignoring strings/chars/comments. */
    private int scanLine(String line, int level, boolean[] inBlockComment) {
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean blockComment = inBlockComment[0];
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';
            if (inLineComment) {
                break;
            }
            if (blockComment) {
                if (c == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                }
                continue;
            }
            if (c == '/' && next == '/') {
                inLineComment = true;
            } else if (c == '/' && next == '*') {
                blockComment = true;
                i++;
            } else if (c == '"') {
                inString = true;
            } else if (c == '\'') {
                inChar = true;
            } else if (c == '{' || c == '(' || c == '[') {
                level++;
            } else if (c == '}' || c == ')' || c == ']') {
                level = Math.max(0, level - 1);
            }
        }
        inBlockComment[0] = blockComment;
        return level;
    }

    private boolean scanBlockComment(String line, boolean inBlockComment) {
        boolean blockComment = inBlockComment;
        boolean inString = false;
        boolean inChar = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            char next = i + 1 < line.length() ? line.charAt(i + 1) : '\0';
            if (blockComment) {
                if (c == '*' && next == '/') {
                    blockComment = false;
                    i++;
                }
                continue;
            }
            if (inString) {
                if (c == '\\') {
                    i++;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }
            if (inChar) {
                if (c == '\\') {
                    i++;
                } else if (c == '\'') {
                    inChar = false;
                }
                continue;
            }
            if (c == '/' && next == '/') {
                break;
            }
            if (c == '/' && next == '*') {
                blockComment = true;
                i++;
            } else if (c == '"') {
                inString = true;
            } else if (c == '\'') {
                inChar = true;
            }
        }
        return blockComment;
    }
}
