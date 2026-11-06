package com.nobi.appbuilder.codeeditor.navigation;

import com.nobi.appbuilder.codeeditor.EditorDocument;

import java.util.List;

/** Go-to-line, go-to-symbol and diagnostic navigation for the editor. */
public class CodeNavigationManager {

    /** Offset of the start of a 1-based line, clamped to the text. */
    public static int offsetForLine(EditorDocument document, int line) {
        String text = document.getText();
        if (text == null || text.isEmpty()) {
            return 0;
        }
        int target = Math.max(1, Math.min(line, document.lineCount()));
        return document.lineStartOffset(target - 1);
    }

    /** Line (1-based) containing an offset. */
    public static int lineForOffset(String text, int offset) {
        int line = 1;
        int safe = Math.max(0, Math.min(offset,
                text == null ? 0 : text.length()));
        for (int i = 0; i < safe && text != null; i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    public static List<SymbolNavigator.Symbol> symbolsOf(
            EditorDocument document) {
        return SymbolNavigator.symbolsOf(document.getText());
    }

    public static int offsetOfSymbol(EditorDocument document,
                                     String symbolName) {
        int line = SymbolNavigator.lineOf(document.getText(), symbolName);
        return line < 0 ? -1 : offsetForLine(document, line);
    }

    public static int matchingBracketOffset(EditorDocument document,
                                            int offset) {
        return BracketMatcher.findMatching(document.getText(), offset);
    }
}
