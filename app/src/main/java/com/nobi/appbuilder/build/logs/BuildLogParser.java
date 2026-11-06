package com.nobi.appbuilder.build.logs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Classifies raw Gradle output lines. The original line is always
 * kept — parsing only adds a level and, where recognizable, a task
 * name. Nothing is discarded.
 */
public class BuildLogParser {

    private static final Pattern TASK_LINE =
            Pattern.compile("^> Task :([\\w:]+)");
    private static final Pattern ERROR_LINE =
            Pattern.compile("(?i)(error:|FAILED|Execution failed|e: .+\\.java)");
    private static final Pattern WARNING_LINE =
            Pattern.compile("(?i)(warning:|w: .+\\.java|deprecated)");
    private static final Pattern SUCCESS_LINE =
            Pattern.compile("(?i)(BUILD SUCCESSFUL)");
    private static final Pattern COMPILER_ERROR =
            Pattern.compile("^e: (.+\\.java):(\\d+), (\\d+): (.+)$");

    /** Parses one line into an entry. */
    public BuildLogEntry parse(String line) {
        String level = BuildLogEntry.INFO;
        String taskName = null;
        Matcher task = TASK_LINE.matcher(line);
        if (task.find()) {
            taskName = task.group(1);
            level = BuildLogEntry.DEBUG;
        } else if (SUCCESS_LINE.matcher(line).find()) {
            level = BuildLogEntry.SUCCESS;
        } else if (ERROR_LINE.matcher(line).find()) {
            level = BuildLogEntry.ERROR;
        } else if (WARNING_LINE.matcher(line).find()) {
            level = BuildLogEntry.WARNING;
        }
        return new BuildLogEntry(level, line, taskName);
    }

    /** True when the line looks like a javac/kotlinc error. */
    public static boolean isCompilerError(String line) {
        return COMPILER_ERROR.matcher(line).find()
                || line.contains("error: ");
    }

    /** Extracts "file:line: message" from a compiler error line. */
    public static String[] compilerErrorParts(String line) {
        Matcher matcher = COMPILER_ERROR.matcher(line);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2),
                    matcher.group(4)};
        }
        return null;
    }
}
