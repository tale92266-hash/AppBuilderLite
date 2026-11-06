package com.nobi.appbuilder.sync;

/** One problem found during a sync run. */
public class SynchronizationError {

    private final String code;
    private final String message;
    private final String file;
    private final int line;

    public SynchronizationError(String code, String message, String file,
                                int line) {
        this.code = code;
        this.message = message;
        this.file = file;
        this.line = line;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getFile() {
        return file;
    }

    public int getLine() {
        return line;
    }

    @Override
    public String toString() {
        return code + ": " + message
                + (file == null ? "" : " (" + file
                + (line > 0 ? ":" + line : "") + ")");
    }
}
