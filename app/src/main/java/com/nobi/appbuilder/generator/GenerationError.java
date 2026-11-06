package com.nobi.appbuilder.generator;

/**
 * One structured generation problem: which stage produced it, which
 * file and component it concerns, and a human-readable message.
 * Severity ERROR fails the generation; WARNING is reported but the
 * output is still usable.
 */
public class GenerationError {

    public static final int SEVERITY_ERROR = 1;
    public static final int SEVERITY_WARNING = 2;

    private final int severity;
    private final String stage;
    private final String file;
    private final String componentId;
    private final String message;

    public GenerationError(int severity, String stage, String file,
                           String componentId, String message) {
        this.severity = severity;
        this.stage = stage;
        this.file = file;
        this.componentId = componentId;
        this.message = message;
    }

    public static GenerationError error(String stage, String file,
                                        String componentId, String message) {
        return new GenerationError(SEVERITY_ERROR, stage, file, componentId, message);
    }

    public static GenerationError warning(String stage, String file,
                                          String componentId, String message) {
        return new GenerationError(SEVERITY_WARNING, stage, file, componentId, message);
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

    public String getComponentId() {
        return componentId;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        String where = file == null ? stage : stage + " · " + file;
        if (componentId != null) {
            where += " · " + componentId;
        }
        return (isError() ? "ERROR: " : "Warning: ") + where + " — " + message;
    }
}
