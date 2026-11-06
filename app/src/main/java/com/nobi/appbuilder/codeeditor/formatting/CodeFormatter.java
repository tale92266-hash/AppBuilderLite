package com.nobi.appbuilder.codeeditor.formatting;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

/** Formats a whole document (or a line range) for one language.
 * Formatting never destroys string literals or comments. */
public interface CodeFormatter {

    boolean supports(EditorLanguage language);

    FormattingResult format(String text, int indentWidth);

    FormattingResult formatRange(String text, int startLine, int endLine,
                                 int indentWidth);
}
