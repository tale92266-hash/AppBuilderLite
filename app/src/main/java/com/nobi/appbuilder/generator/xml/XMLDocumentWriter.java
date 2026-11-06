package com.nobi.appbuilder.generator.xml;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Structured XML writer: elements, attributes and text go through
 * here so no generated file is ever built by raw string
 * concatenation. Attribute and text values are escaped
 * (&amp; &lt; &gt; &quot; &apos;), output is indented, and
 * unclosed elements are a programming error, not a corrupt file.
 */
public class XMLDocumentWriter {

    private final StringBuilder out = new StringBuilder();
    private final Deque<String> stack = new ArrayDeque<>();
    private int depth;

    public XMLDocumentWriter() {
        out.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    }

    public void startElement(String tag) {
        indent();
        out.append('<').append(tag).append(">\n");
        stack.push(tag);
        depth++;
    }

    public void startElement(String tag, Map<String, String> attributes) {
        indent();
        out.append('<').append(tag);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            out.append('\n');
            indent();
            out.append("    ").append(attr.getKey()).append("=\"")
                    .append(escape(attr.getValue())).append('"');
        }
        out.append(">\n");
        stack.push(tag);
        depth++;
    }

    /** Empty element: <tag attr="..." /> on one logical block. */
    public void emptyElement(String tag, Map<String, String> attributes) {
        indent();
        out.append('<').append(tag);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            out.append('\n');
            indent();
            out.append("    ").append(attr.getKey()).append("=\"")
                    .append(escape(attr.getValue())).append('"');
        }
        out.append(" />\n");
    }

    public void textElement(String tag, Map<String, String> attributes,
                            String text) {
        indent();
        out.append('<').append(tag);
        for (Map.Entry<String, String> attr : attributes.entrySet()) {
            out.append(' ').append(attr.getKey()).append("=\"")
                    .append(escape(attr.getValue())).append('"');
        }
        out.append('>').append(escape(text)).append("</").append(tag).append(">\n");
    }

    public void endElement() {
        if (stack.isEmpty()) {
            throw new IllegalStateException("No open element to close");
        }
        depth--;
        indent();
        out.append("</").append(stack.pop()).append(">\n");
    }

    public void comment(String text) {
        indent();
        out.append("<!-- ").append(text.replace("--", "- -")).append(" -->\n");
    }

    public String build() {
        if (!stack.isEmpty()) {
            throw new IllegalStateException(
                    "Unclosed elements: " + stack.toString());
        }
        return out.toString();
    }

    private void indent() {
        for (int i = 0; i < depth; i++) {
            out.append("    ");
        }
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder escaped = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '&':
                    escaped.append("&amp;");
                    break;
                case '<':
                    escaped.append("&lt;");
                    break;
                case '>':
                    escaped.append("&gt;");
                    break;
                case '"':
                    escaped.append("&quot;");
                    break;
                case '\'':
                    escaped.append("&apos;");
                    break;
                default:
                    escaped.append(c);
                    break;
            }
        }
        return escaped.toString();
    }

    /** Convenience: a flat attribute map in insertion order. */
    public static Map<String, String> attrs(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) {
            map.put(keyValues[i], keyValues[i + 1]);
        }
        return map;
    }
}
