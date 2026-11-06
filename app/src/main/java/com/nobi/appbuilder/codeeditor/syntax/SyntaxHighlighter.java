package com.nobi.appbuilder.codeeditor.syntax;

import java.util.List;

/**
 * A state-aware tokenizer. tokenize() works on a line range so the
 * editor can re-highlight only the lines that changed — never the
 * whole document per keystroke. The start state of the range is
 * computed by scanning from the document start only when the range
 * does not begin at line 0; callers pass the state via the context.
 */
public interface SyntaxHighlighter {

    /** Tokens for [startOffset, endOffset) in the given text. */
    List<SyntaxToken> tokenize(String text, int startOffset, int endOffset);

    /** True when the highlighter can handle this language. */
    boolean supports(com.nobi.appbuilder.codeeditor.EditorLanguage language);
}
