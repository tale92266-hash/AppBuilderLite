package com.nobi.appbuilder.codeeditor.navigation;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds top-level symbols (classes, methods, fields) in a Java file
 * for jump-to navigation. */
public class SymbolNavigator {

    /** One symbol: name, kind, 1-based line. */
    public static class Symbol {
        public final String name;
        public final String kind;
        public final int line;

        public Symbol(String name, String kind, int line) {
            this.name = name;
            this.kind = kind;
            this.line = line;
        }
    }

    private static final Pattern CLASS_PATTERN = Pattern.compile(
            "^\\s*(?:public|private|protected|static|final|abstract|\\s)*"
                    + "(?:class|interface|enum)\\s+(\\w+)");
    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "^\\s*(?:public|private|protected|static|final|\\s)*"
                    + "[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\(");

    public static List<Symbol> symbolsOf(String text) {
        List<Symbol> out = new ArrayList<>();
        if (text == null) {
            return out;
        }
        String[] lines = text.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            Matcher cls = CLASS_PATTERN.matcher(line);
            if (cls.find()) {
                out.add(new Symbol(cls.group(1), "class", i + 1));
                continue;
            }
            Matcher method = METHOD_PATTERN.matcher(line);
            if (method.find() && !line.trim().startsWith("if")
                    && !line.trim().startsWith("for")
                    && !line.trim().startsWith("while")
                    && !line.trim().startsWith("switch")
                    && !line.trim().startsWith("catch")
                    && !line.trim().startsWith("return")) {
                out.add(new Symbol(method.group(1), "method", i + 1));
            }
        }
        return out;
    }

    /** Line (1-based) of the named symbol, or -1. */
    public static int lineOf(String text, String symbolName) {
        for (Symbol symbol : symbolsOf(text)) {
            if (symbol.name.equals(symbolName)) {
                return symbol.line;
            }
        }
        return -1;
    }
}
