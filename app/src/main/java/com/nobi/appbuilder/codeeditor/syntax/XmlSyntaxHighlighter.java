package com.nobi.appbuilder.codeeditor.syntax;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

import java.util.ArrayList;
import java.util.List;

/**
 * State-aware XML tokenizer. Tolerates malformed or incomplete XML
 * while editing — a half-typed tag never crashes highlighting, it
 * just stays a TAG token until the user finishes it.
 */
public class XmlSyntaxHighlighter implements SyntaxHighlighter {

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.XML;
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
        int i = start;
        while (i < end) {
            char c = text.charAt(i);
            // XML comment
            if (c == '<' && i + 3 < end && text.charAt(i + 1) == '!'
                    && text.charAt(i + 2) == '-' && text.charAt(i + 3) == '-') {
                int close = text.indexOf("-->", i + 4);
                int tokenEnd = close >= 0 && close + 3 <= end ? close + 3 : end;
                tokens.add(new SyntaxToken(i, tokenEnd,
                        SyntaxTokenType.COMMENT));
                i = tokenEnd;
                continue;
            }
            // XML declaration / doctype
            if (c == '<' && i + 1 < end
                    && (text.charAt(i + 1) == '?' || text.charAt(i + 1) == '!')) {
                int close = text.indexOf('>', i + 2);
                int tokenEnd = close >= 0 ? close + 1 : end;
                tokens.add(new SyntaxToken(i, tokenEnd,
                        SyntaxTokenType.DECLARATION));
                i = tokenEnd;
                continue;
            }
            // Tag open
            if (c == '<') {
                int close = text.indexOf('>', i + 1);
                int tagEnd = close >= 0 ? close + 1 : end;
                int nameStart = i + 1;
                if (nameStart < tagEnd && text.charAt(nameStart) == '/') {
                    nameStart++;
                }
                int nameEnd = nameStart;
                while (nameEnd < tagEnd
                        && !Character.isWhitespace(text.charAt(nameEnd))
                        && text.charAt(nameEnd) != '>'
                        && text.charAt(nameEnd) != '/') {
                    nameEnd++;
                }
                tokens.add(new SyntaxToken(i, nameEnd, SyntaxTokenType.TAG));
                i = nameEnd;
                // Attributes inside the tag
                while (i < tagEnd) {
                    char ch = text.charAt(i);
                    if (ch == '"' || ch == '\'') {
                        int valueEnd = text.indexOf(ch, i + 1);
                        int valueStop = valueEnd >= 0 && valueEnd < tagEnd
                                ? valueEnd + 1 : tagEnd;
                        tokens.add(new SyntaxToken(i, valueStop,
                                SyntaxTokenType.ATTR_VALUE));
                        i = valueStop;
                        continue;
                    }
                    if (Character.isWhitespace(ch) || ch == '>' || ch == '/'
                            || ch == '=') {
                        i++;
                        continue;
                    }
                    int attrStart = i;
                    while (i < tagEnd && !Character.isWhitespace(
                            text.charAt(i)) && text.charAt(i) != '='
                            && text.charAt(i) != '>'
                            && text.charAt(i) != '/') {
                        i++;
                    }
                    if (i > attrStart) {
                        tokens.add(new SyntaxToken(attrStart, i,
                                SyntaxTokenType.ATTRIBUTE));
                    }
                }
                continue;
            }
            // Entity reference
            if (c == '&') {
                int semi = text.indexOf(';', i + 1);
                int tokenEnd = semi >= 0 && semi < end ? semi + 1
                        : Math.min(i + 1, end);
                tokens.add(new SyntaxToken(i, tokenEnd,
                        SyntaxTokenType.ENTITY));
                i = tokenEnd;
                continue;
            }
            // Plain text until the next < or &
            int j = i;
            while (j < end && text.charAt(j) != '<' && text.charAt(j) != '&') {
                j++;
            }
            if (j > i) {
                tokens.add(new SyntaxToken(i, j, SyntaxTokenType.PLAIN));
            }
            i = j;
        }
        return tokens;
    }
}
