package com.nobi.appbuilder.codeeditor.diagnostics;

import java.io.File;

/** One lightweight source diagnostic. Heuristic checks only — never
 * presented as a complete Java compiler. */
public class CodeDiagnostic {

    private final File file;
    private final int line;
    private final int column;
    private final CodeDiagnosticType type;
    private final String message;

    public CodeDiagnostic(File file, int line, int column,
                          CodeDiagnosticType type, String message) {
        this.file = file;
        this.line = line;
        this.column = column;
        this.type = type;
        this.message = message;
    }

    public File getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public CodeDiagnosticType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
