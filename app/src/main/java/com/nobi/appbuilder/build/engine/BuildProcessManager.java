package com.nobi.appbuilder.build.engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/** Runs one external process, streams its output line by line, and
 * supports cancellation by destroying the process. */
public class BuildProcessManager {

    public interface LineListener {
        void onLine(String line);
    }

    public interface CancelChecker {
        boolean isCancelled();
    }

    /** Runs the command; returns the exit code, or -1 when cancelled. */
    public int run(File workingDir, List<String> command,
                   Map<String, String> extraEnv, LineListener listener,
                   CancelChecker cancelChecker) {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            builder.directory(workingDir);
            builder.redirectErrorStream(true);
            if (extraEnv != null) {
                builder.environment().putAll(extraEnv);
            }
            process = builder.start();
            final Process running = process;
            Thread watcher = new Thread(() -> {
                try {
                    while (!cancelChecker.isCancelled()) {
                        Thread.sleep(200);
                    }
                    running.destroy();
                } catch (Exception ignored) {
                }
            });
            watcher.setDaemon(true);
            watcher.start();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listener.onLine(line);
                    if (cancelChecker.isCancelled()) {
                        break;
                    }
                }
            }
            int exit = process.waitFor();
            watcher.interrupt();
            return cancelChecker.isCancelled() ? -1 : exit;
        } catch (Exception e) {
            listener.onLine("Process failed to start: " + e.getMessage());
            return -1;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }
}
