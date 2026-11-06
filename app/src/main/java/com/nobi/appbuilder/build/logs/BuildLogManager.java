package com.nobi.appbuilder.build.logs;

import java.util.ArrayList;
import java.util.List;

/** In-memory capture of one build's log, with live listeners. */
public class BuildLogManager {

    public interface LogListener {
        void onLogEntry(BuildLogEntry entry);
    }

    private final List<BuildLogEntry> entries = new ArrayList<>();
    private final List<LogListener> listeners = new ArrayList<>();
    private final BuildLogParser parser = new BuildLogParser();
    private int errorCount;
    private int warningCount;

    public synchronized void addListener(LogListener listener) {
        listeners.add(listener);
    }

    public synchronized void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    /** Appends a raw line; the parser classifies it. */
    public void appendLine(String line) {
        append(parser.parse(line));
    }

    public synchronized void append(BuildLogEntry entry) {
        entries.add(entry);
        if (entry.isError()) {
            errorCount++;
        }
        if (entry.isWarning()) {
            warningCount++;
        }
        for (LogListener listener : new ArrayList<>(listeners)) {
            listener.onLogEntry(entry);
        }
    }

    public synchronized List<BuildLogEntry> getEntries() {
        return new ArrayList<>(entries);
    }

    public synchronized int getErrorCount() {
        return errorCount;
    }

    public synchronized int getWarningCount() {
        return warningCount;
    }

    public synchronized void clear() {
        entries.clear();
        errorCount = 0;
        warningCount = 0;
    }

    /** The complete original output, one line per entry. */
    public synchronized String toText() {
        StringBuilder out = new StringBuilder();
        for (BuildLogEntry entry : entries) {
            out.append(entry.getText()).append("\n");
        }
        return out.toString();
    }
}
