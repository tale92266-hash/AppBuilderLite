package com.nobi.appbuilder.build.storage;

import android.content.Context;
import android.os.StatFs;

import java.io.File;

/** Where builds live: APKs and logs under the app-specific
 * external files dir (FileProvider-covered, no permission needed). */
public class BuildStorageManager {

    private final Context appContext;

    public BuildStorageManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public File getBuildsDir() {
        File dir = new File(appContext.getExternalFilesDir(null), "builds");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public File getBuildLogsDir() {
        File dir = new File(getBuildsDir(), "logs");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** Unique APK destination: name.apk, name (2).apk, ... */
    public File uniqueApkFile(String baseName) {
        File dir = getBuildsDir();
        String name = baseName.endsWith(".apk") ? baseName : baseName + ".apk";
        File file = new File(dir, name);
        int n = 2;
        while (file.exists()) {
            String stem = name.substring(0, name.length() - 4);
            file = new File(dir, stem + " (" + n + ").apk");
            n++;
        }
        return file;
    }

    public boolean hasFreeSpace(long neededBytes) {
        try {
            StatFs stat = new StatFs(getBuildsDir().getAbsolutePath());
            return stat.getAvailableBytes()
                    > neededBytes + (50L * 1024 * 1024);
        } catch (Exception e) {
            return false;
        }
    }

    public long getFreeSpaceBytes() {
        try {
            return new StatFs(getBuildsDir().getAbsolutePath())
                    .getAvailableBytes();
        } catch (Exception e) {
            return 0;
        }
    }
}
