package com.nobi.appbuilder.build.apk;

import android.content.Context;

import com.nobi.appbuilder.build.storage.BuildStorageManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Lists and manages the APKs a project has produced. */
public class ApkManager {

    private final Context appContext;

    public ApkManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    /** All APKs in the builds dir, newest first. */
    public List<File> listApks() {
        List<File> out = new ArrayList<>();
        File dir = new BuildStorageManager(appContext).getBuildsDir();
        File[] files = dir.listFiles(
                (d, name) -> name.endsWith(".apk"));
        if (files != null) {
            java.util.Arrays.sort(files,
                    (a, b) -> Long.compare(b.lastModified(),
                            a.lastModified()));
            for (File file : files) {
                out.add(file);
            }
        }
        return out;
    }

    /** Deletes an APK only when the user explicitly asked. */
    public boolean deleteApk(File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            return false;
        }
        File dir = new BuildStorageManager(appContext).getBuildsDir();
        try {
            if (!apkFile.getCanonicalPath().startsWith(
                    dir.getCanonicalPath())) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return apkFile.delete();
    }
}
