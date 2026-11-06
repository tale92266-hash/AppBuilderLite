package com.nobi.appbuilder.build.logs;

/** One captured build-output line, with its original text intact. */
public class BuildLogEntry {

    public static final String INFO = "INFO";
    public static final String DEBUG = "DEBUG";
    public static final String WARNING = "WARNING";
    public static final String ERROR = "ERROR";
    public static final String SUCCESS = "SUCCESS";

    private final long timestampMs;
    private final String level;
    private final String text;
    private final String taskName;

    public BuildLogEntry(String level, String text, String taskName) {
        this.timestampMs = System.currentTimeMillis();
        this.level = level;
        this.text = text;
        this.taskName = taskName;
    }

    public BuildLogEntry(long timestampMs, String level, String text,
                         String taskName) {
        this.timestampMs = timestampMs;
        this.level = level;
        this.text = text;
        this.taskName = taskName;
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getLevel() {
        return level;
    }

    public String getText() {
        return text;
    }

    public String getTaskName() {
        return taskName;
    }

    public boolean isError() {
        return ERROR.equals(level);
    }

    public boolean isWarning() {
        return WARNING.equals(level);
    }

    @Override
    public String toString() {
        return "[" + level + "] " + text;
    }
}
