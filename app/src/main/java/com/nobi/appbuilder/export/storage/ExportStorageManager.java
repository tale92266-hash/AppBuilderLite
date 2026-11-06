package com.nobi.appbuilder.export.storage;

import android.content.Context;
import android.os.StatFs;

import com.nobi.appbuilder.utils.FileUtils;

import java.io.File;

/**
 * Where exported ZIPs live. Default is the app-specific exports
 * directory (no permission needed, FileProvider-covered). A custom
 * SAF tree URI may be set instead — see ExportDirectoryManager.
 */
public class ExportStorageManager {

    public static final String PREFS_EXPORT = "export_prefs";
    public static final String KEY_OUTPUT_TREE_URI = "output_tree_uri";

    private final Context appContext;

    public ExportStorageManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public File getDefaultExportDir() {
        return FileUtils.getExportsDir(appContext);
    }

    /** Unique output file: name.zip, name (2).zip, ... */
    public File uniqueOutputFile(String baseName) {
        File dir = getDefaultExportDir();
        String name = baseName.endsWith(".zip") ? baseName : baseName + ".zip";
        File file = new File(dir, name);
        int n = 2;
        while (file.exists()) {
            String stem = name.substring(0, name.length() - 4);
            file = new File(dir, stem + " (" + n + ").zip");
            n++;
        }
        return file;
    }

    /** True when the export dir has at least the given bytes free. */
    public boolean hasFreeSpace(long neededBytes) {
        try {
            StatFs stat = new StatFs(getDefaultExportDir().getAbsolutePath());
            long free = stat.getAvailableBytes();
            return free > neededBytes + (10L * 1024 * 1024);
        } catch (Exception e) {
            return false;
        }
    }

    public long getFreeSpaceBytes() {
        try {
            return new StatFs(getDefaultExportDir().getAbsolutePath())
                    .getAvailableBytes();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getCustomOutputTreeUri() {
        return appContext.getSharedPreferences(PREFS_EXPORT,
                Context.MODE_PRIVATE).getString(KEY_OUTPUT_TREE_URI, null);
    }

    public void setCustomOutputTreeUri(String uri) {
        appContext.getSharedPreferences(PREFS_EXPORT, Context.MODE_PRIVATE)
                .edit().putString(KEY_OUTPUT_TREE_URI, uri).apply();
    }

    public void clearCustomOutputTreeUri() {
        appContext.getSharedPreferences(PREFS_EXPORT, Context.MODE_PRIVATE)
                .edit().remove(KEY_OUTPUT_TREE_URI).apply();
    }
}
