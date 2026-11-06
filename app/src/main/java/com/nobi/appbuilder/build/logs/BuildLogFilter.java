package com.nobi.appbuilder.build.logs;

import java.util.ArrayList;
import java.util.List;

/** Filters captured log entries by level and free text. */
public class BuildLogFilter {

    private boolean showInfo = true;
    private boolean showDebug;
    private boolean showWarnings = true;
    private boolean showErrors = true;
    private boolean showSuccess = true;
    private String query;

    public void setShowInfo(boolean showInfo) {
        this.showInfo = showInfo;
    }

    public void setShowDebug(boolean showDebug) {
        this.showDebug = showDebug;
    }

    public void setShowWarnings(boolean showWarnings) {
        this.showWarnings = showWarnings;
    }

    public void setShowErrors(boolean showErrors) {
        this.showErrors = showErrors;
    }

    public void setShowSuccess(boolean showSuccess) {
        this.showSuccess = showSuccess;
    }

    public void setQuery(String query) {
        this.query = query == null || query.trim().isEmpty()
                ? null : query.trim().toLowerCase();
    }

    public String getQuery() {
        return query;
    }

    public List<BuildLogEntry> apply(List<BuildLogEntry> entries) {
        List<BuildLogEntry> out = new ArrayList<>();
        for (BuildLogEntry entry : entries) {
            if (!levelVisible(entry.getLevel())) {
                continue;
            }
            if (query != null
                    && !entry.getText().toLowerCase().contains(query)) {
                continue;
            }
            out.add(entry);
        }
        return out;
    }

    private boolean levelVisible(String level) {
        switch (level) {
            case BuildLogEntry.INFO:
                return showInfo;
            case BuildLogEntry.DEBUG:
                return showDebug;
            case BuildLogEntry.WARNING:
                return showWarnings;
            case BuildLogEntry.ERROR:
                return showErrors;
            case BuildLogEntry.SUCCESS:
                return showSuccess;
            default:
                return true;
        }
    }
}
