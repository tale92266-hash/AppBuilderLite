package com.nobi.appbuilder.build.engine;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the fixed Gradle command for a build. Only the two known
 * assemble tasks are ever produced — user input never reaches a
 * command line. The wrapper is run through "sh" because Android
 * does not guarantee /bin/sh for the script's shebang.
 */
public class GradleCommandBuilder {

    public static final String TASK_DEBUG = "assembleDebug";
    public static final String TASK_RELEASE = "assembleRelease";

    /** Command to run inside the project workspace. */
    public static List<String> commandFor(File workspace, String task) {
        if (!TASK_DEBUG.equals(task) && !TASK_RELEASE.equals(task)) {
            throw new IllegalArgumentException("Unsupported Gradle task: "
                    + task);
        }
        List<String> command = new ArrayList<>();
        File wrapper = new File(workspace, "gradlew");
        if (wrapper.exists()) {
            command.add("sh");
            command.add("./gradlew");
        } else {
            command.add("gradle");
        }
        command.add(task);
        command.add("--stacktrace");
        return command;
    }

    public static String taskFor(boolean release) {
        return release ? TASK_RELEASE : TASK_DEBUG;
    }
}
