package com.nobi.appbuilder.codeeditor.formatting;

import com.nobi.appbuilder.codeeditor.EditorLanguage;

/** Depth-aware XML formatter. Attribute values and comments are
 * preserved verbatim; only leading indentation is adjusted. */
public class XmlCodeFormatter implements CodeFormatter {

    @Override
    public boolean supports(EditorLanguage language) {
        return language == EditorLanguage.XML;
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
        int depth = 0;
        int changed = 0;
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmed = line.trim();
            if (i >= startLine && i <= endLine && !trimmed.isEmpty()) {
                int effectiveDepth = depth;
                if (trimmed.startsWith("</")) {
                    effectiveDepth = Math.max(0, depth - 1);
                }
                String formatted = indent.indentString(effectiveDepth)
                        + trimmed;
                if (!formatted.equals(line)) {
                    changed++;
                }
                out.append(formatted);
            } else {
                out.append(line);
            }
            if (!trimmed.isEmpty() && !trimmed.startsWith("<?")
                    && !trimmed.startsWith("<!--")) {
                if (trimmed.startsWith("</")) {
                    depth = Math.max(0, depth - 1);
                } else if (trimmed.startsWith("<")
                        && !trimmed.endsWith("/>")
                        && !trimmed.contains("</")) {
                    depth++;
                }
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
}
