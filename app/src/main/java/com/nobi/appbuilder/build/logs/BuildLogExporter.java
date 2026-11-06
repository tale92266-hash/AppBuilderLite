package com.nobi.appbuilder.build.logs;

import android.content.Context;

import com.nobi.appbuilder.build.storage.BuildStorageManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/** Writes a build's captured log to a TXT file under the builds dir. */
public class BuildLogExporter {

    private final Context appContext;

    public BuildLogExporter(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** Returns the written file, or null on failure. */
    public File export(String buildId, BuildLogManager logManager) {
        File dir = new BuildStorageManager(appContext).getBuildLogsDir();
        File file = new File(dir, "build-" + buildId + ".log.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(logManager.toText());
            return file;
        } catch (IOException e) {
            return null;
        }
    }

    public File exportText(String buildId, String text) {
        File dir = new BuildStorageManager(appContext).getBuildLogsDir();
        File file = new File(dir, "build-" + buildId + ".log.txt");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(text);
            return file;
        } catch (IOException e) {
            return null;
        }
    }
}
