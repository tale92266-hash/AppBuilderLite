package com.nobi.appbuilder.generator.java;

import java.util.ArrayList;
import java.util.List;

/**
 * Structural checks on generated Java: balanced braces/parens
 * (string/comment aware), package line present, class name matches
 * the file name, and every R.id.* / R.layout.* reference the caller
 * collected actually resolves. This is a safety net, not a compiler
 * — the generated project is still meant to be built by Gradle.
 */
public final class JavaSourceValidator {

    private JavaSourceValidator() {
    }

    /** Returns a list of problems; empty means structurally valid. */
    public static List<String> validate(String packageName, String className,
                                        String source,
                                        java.util.Set<String> knownIds,
                                        java.util.Set<String> knownLayouts) {
        List<String> problems = new ArrayList<>();
        if (source == null || source.isEmpty()) {
            problems.add("empty source");
            return problems;
        }
        if (!source.contains("package " + packageName + ";")) {
            problems.add("package declaration does not match " + packageName);
        }
        if (!source.contains("public class " + className + " ")) {
            problems.add("class name does not match " + className);
        }
        String stripped = stripStringsAndComments(source);
        int braces = 0;
        int parens = 0;
        for (int i = 0; i < stripped.length(); i++) {
            char c = stripped.charAt(i);
            if (c == '{') {
                braces++;
            } else if (c == '}') {
                braces--;
            } else if (c == '(') {
                parens++;
            } else if (c == ')') {
                parens--;
            }
            if (braces < 0 || parens < 0) {
                problems.add("unbalanced brackets near offset " + i);
                break;
            }
        }
        if (braces != 0) {
            problems.add("unbalanced braces: " + braces + " unclosed");
        }
        if (parens != 0) {
            problems.add("unbalanced parentheses: " + parens + " unclosed");
        }
        java.util.regex.Matcher idRefs =
                java.util.regex.Pattern.compile("R\\.id\\.(\\w+)").matcher(source);
        while (idRefs.find()) {
            if (!knownIds.contains(idRefs.group(1))) {
                problems.add("R.id." + idRefs.group(1)
                        + " has no matching view in the layouts");
            }
        }
        java.util.regex.Matcher layoutRefs =
                java.util.regex.Pattern.compile("R\\.layout\\.(\\w+)")
                        .matcher(source);
        while (layoutRefs.find()) {
            if (!knownLayouts.contains(layoutRefs.group(1))) {
                problems.add("R.layout." + layoutRefs.group(1)
                        + " has no generated layout file");
            }
        }
        return problems;
    }

    /** Removes string/char literals and // + /* *\/ comments. */
    private static String stripStringsAndComments(String source) {
        StringBuilder out = new StringBuilder(source.length());
        int i = 0;
        while (i < source.length()) {
            char c = source.charAt(i);
            if (c == '/' && i + 1 < source.length() && source.charAt(i + 1) == '/') {
                while (i < source.length() && source.charAt(i) != '\n') {
                    i++;
                }
            } else if (c == '/' && i + 1 < source.length()
                    && source.charAt(i + 1) == '*') {
                i += 2;
                while (i + 1 < source.length()
                        && !(source.charAt(i) == '*' && source.charAt(i + 1) == '/')) {
                    i++;
                }
                i += 2;
            } else if (c == '"' || c == '\'') {
                char quote = c;
                out.append(' ');
                i++;
                while (i < source.length()) {
                    char d = source.charAt(i);
                    if (d == '\\') {
                        i += 2;
                        continue;
                    }
                    if (d == quote) {
                        i++;
                        break;
                    }
                    i++;
                }
            } else {
                out.append(c);
                i++;
            }
        }
        return out.toString();
    }

    /** Escapes a value for a Java string literal. */
    public static String javaString(String value) {
        if (value == null) {
            return "\"\"";
        }
        StringBuilder out = new StringBuilder("\"");
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"':
                    out.append("\\\"");
                    break;
                case '\\':
                    out.append("\\\\");
                    break;
                case '\n':
                    out.append("\\n");
                    break;
                case '\r':
                    out.append("\\r");
                    break;
                case '\t':
                    out.append("\\t");
                    break;
                default:
                    out.append(c);
                    break;
            }
        }
        out.append('"');
        return out.toString();
    }
}
