package com.nobi.appbuilder.build.diagnostics;

import com.nobi.appbuilder.build.logs.BuildLogEntry;
import com.nobi.appbuilder.build.logs.BuildLogParser;

import java.util.ArrayList;
import java.util.List;

/** Turns captured log lines into classified errors and warnings. */
public class BuildDiagnosticsManager {

    private final List<BuildError> errors = new ArrayList<>();
    private final List<BuildWarning> warnings = new ArrayList<>();

    public void analyse(List<BuildLogEntry> entries) {
        errors.clear();
        warnings.clear();
        for (BuildLogEntry entry : entries) {
            if (entry.isError()) {
                String category = BuildIssueClassifier.classify(
                        entry.getText());
                String[] parts = BuildLogParser.compilerErrorParts(
                        entry.getText());
                String file = parts != null ? parts[0] : null;
                int line = 0;
                if (parts != null) {
                    try {
                        line = Integer.parseInt(parts[1]);
                    } catch (Exception ignored) {
                    }
                }
                String message = parts != null ? parts[2] : entry.getText();
                errors.add(new BuildError(category, message, file, line,
                        entry.getText(),
                        BuildSuggestionEngine.suggestionFor(category)));
            } else if (entry.isWarning()) {
                warnings.add(new BuildWarning(
                        BuildIssueClassifier.classify(entry.getText()),
                        entry.getText(), null, 0));
            }
        }
    }

    public List<BuildError> getErrors() {
        return new ArrayList<>(errors);
    }

    public List<BuildWarning> getWarnings() {
        return new ArrayList<>(warnings);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }
}
