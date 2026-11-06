package com.nobi.appbuilder.export.report;

import android.content.Context;

import com.nobi.appbuilder.utils.FileUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Per-export run log: in-memory lines + a file beside the ZIP. */
public class ExportLogManager {

    private final Context appContext;
    private final List<String> lines = new ArrayList<>();
    private final SimpleDateFormat timeFormat =
            new SimpleDateFormat("HH:mm:ss", Locale.US);

    public ExportLogManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public void log(String message) {
        lines.add("[" + timeFormat.format(new Date()) + "] " + message);
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    /** Writes the log next to the exported ZIP; returns the file or null. */
    public File writeLogFile(String zipBaseName) {
        try {
            StringBuilder out = new StringBuilder();
            for (String line : lines) {
                out.append(line).append("\n");
            }
            File file = new File(FileUtils.getExportsDir(appContext),
                    zipBaseName + ".export.log");
            FileUtils.writeTextFile(file, out.toString());
            return file;
        } catch (Exception e) {
            return null;
        }
    }
}
