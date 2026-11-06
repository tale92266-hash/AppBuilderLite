package com.nobi.appbuilder.codeeditor.diagnostics;

import com.nobi.appbuilder.codeeditor.EditorDocument;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Lightweight source diagnostics: malformed XML, missing closing
 * tags, duplicate ids, invalid package declarations, empty files,
 * and basic brace balance. These are heuristic checks — the real
 * verdict comes from a Phase 9 build.
 */
public class CodeDiagnosticManager {

    /** Diagnostics for one open document. */
    public List<CodeDiagnostic> diagnose(EditorDocument document) {
        List<CodeDiagnostic> out = new ArrayList<>();
        File file = document.getFile();
        String text = document.getText();
        if (text == null || text.trim().isEmpty()) {
            out.add(new CodeDiagnostic(file, 1, 1,
                    CodeDiagnosticType.WARNING, "File is empty"));
            return out;
        }
        switch (document.getLanguage()) {
            case XML:
                out.addAll(diagnoseXml(file, text));
                break;
            case JAVA:
                out.addAll(diagnoseJava(file, text));
                break;
            default:
                break;
        }
        return out;
    }

    private List<CodeDiagnostic> diagnoseXml(File file, String text) {
        List<CodeDiagnostic> out = new ArrayList<>();
        try {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            factory.setFeature(
                    "http://apache.org/xml/features/disallow-doctype-decl",
                    true);
            factory.setNamespaceAware(true);
            factory.newDocumentBuilder().parse(
                    new java.io.ByteArrayInputStream(
                            text.getBytes(
                                    java.nio.charset.StandardCharsets.UTF_8)));
        } catch (Exception e) {
            String message = e.getMessage() == null ? "Invalid XML"
                    : e.getMessage();
            out.add(new CodeDiagnostic(file, lineOf(message, text), 1,
                    CodeDiagnosticType.ERROR, "Malformed XML: " + message));
        }
        Set<String> ids = new HashSet<>();
        Matcher idMatcher = Pattern.compile("@\\+id/([A-Za-z0-9_]+)")
                .matcher(text);
        while (idMatcher.find()) {
            String id = idMatcher.group(1);
            if (!ids.add(id)) {
                out.add(new CodeDiagnostic(file,
                        lineAt(text, idMatcher.start()), 1,
                        CodeDiagnosticType.WARNING,
                        "Duplicate id: " + id));
            }
        }
        return out;
    }

    private List<CodeDiagnostic> diagnoseJava(File file, String text) {
        List<CodeDiagnostic> out = new ArrayList<>();
        Matcher packageMatcher = Pattern.compile(
                "^\\s*package\\s+([\\w.]+);", Pattern.MULTILINE)
                .matcher(text);
        if (packageMatcher.find()) {
            String declared = packageMatcher.group(1);
            String path = file.getAbsolutePath();
            String expected = path.replace('/', '.');
            if (!expected.contains(declared)) {
                out.add(new CodeDiagnostic(file, 1, 1,
                        CodeDiagnosticType.WARNING,
                        "Package declaration " + declared
                                + " does not match the file path"));
            }
        } else {
            out.add(new CodeDiagnostic(file, 1, 1,
                    CodeDiagnosticType.WARNING,
                    "No package declaration"));
        }
        int braces = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean inLineComment = false;
        boolean inBlockComment = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char next = i + 1 < text.length() ? text.charAt(i + 1) : '\0';
            if (inLineComment) {
                if (c == '\n') {
                    inLineComment = false;
                }
                continue;
            }
            if (inBlockComment) {
                if (c == '*' && next == '/') {
                    inBlockComment = false;
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
                inBlockComment = true;
            } else if (c == '"') {
                inString = true;
            } else if (c == '\'') {
                inChar = true;
            } else if (c == '{') {
                braces++;
            } else if (c == '}') {
                braces--;
            }
        }
        if (braces != 0) {
            out.add(new CodeDiagnostic(file, 1, 1,
                    CodeDiagnosticType.ERROR,
                    "Unbalanced braces: " + braces + " unclosed"));
        }
        return out;
    }

    private int lineAt(String text, int offset) {
        int line = 1;
        for (int i = 0; i < offset && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                line++;
            }
        }
        return line;
    }

    private int lineOf(String message, String text) {
        Matcher matcher = Pattern.compile("lineNumber: (\\d+)")
                .matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (Exception ignored) {
            }
        }
        return 1;
    }
}
