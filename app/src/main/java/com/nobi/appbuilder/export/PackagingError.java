package com.nobi.appbuilder.export;

/** One packaging problem: stage, file (or null) and an actionable message. */
public class PackagingError {

    public static final int SEVERITY_ERROR = 1;
    public static final int SEVERITY_WARNING = 2;

    private final int severity;
    private final String stage;
    private final String file;
    private final String message;

    public PackagingError(int severity, String stage, String file,
                          String message) {
        this.severity = severity;
        this.stage = stage;
        this.file = file;
        this.message = message;
    }

    public static PackagingError error(String stage, String file, String message) {
        return new PackagingError(SEVERITY_ERROR, stage, file, message);
    }

    public static PackagingError warning(String stage, String file, String message) {
        return new PackagingError(SEVERITY_WARNING, stage, file, message);
    }

    public boolean isError() {
        return severity == SEVERITY_ERROR;
    }

    public boolean isWarning() {
        return severity == SEVERITY_WARNING;
    }

    public String getStage() {
        return stage;
    }

    public String getFile() {
        return file;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        StringBuilder out = new StringBuilder(isError() ? "ERROR" : "WARNING");
        out.append(": ").append(stage);
        if (file != null) {
            out.append(" · ").append(file);
        }
        out.append(" — ").append(message);
        return out.toString();
    }
}
