package com.nobi.appbuilder.build;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Locates real APK outputs inside a Gradle build directory. */
public class BuildOutputManager {

    /**
     * Finds APKs under app/build/outputs/apk/<type>/ — newest first.
     * Returns an empty list when nothing was produced; a missing APK
     * is never papered over with a placeholder.
     */
    public List<File> findApks(File projectDir, String buildType) {
        List<File> out = new ArrayList<>();
        File apkDir = new File(projectDir,
                "app/build/outputs/apk/" + buildType);
        File[] files = apkDir.listFiles(
                (dir, name) -> name.endsWith(".apk"));
        if (files != null) {
            java.util.Arrays.sort(files,
                    (a, b) -> Long.compare(b.lastModified(),
                            a.lastModified()));
            for (File file : files) {
                if (file.length() > 0) {
                    out.add(file);
                }
            }
        }
        return out;
    }

    /** The single APK a build produced, or null. */
    public File findPrimaryApk(File projectDir, String buildType) {
        List<File> apks = findApks(projectDir, buildType);
        return apks.isEmpty() ? null : apks.get(0);
    }
}
