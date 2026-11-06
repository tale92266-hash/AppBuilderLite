package com.nobi.appbuilder.build.apk;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.io.File;

/** Reads APK metadata through PackageManager — no hand-rolled
 * manifest parsing, no fake values. */
public class ApkMetadataReader {

    /** Parsed metadata, or null when the file is not a valid APK. */
    public static class ApkMetadata {
        public String packageName;
        public String versionName;
        public int versionCode;
        public int minSdk;
        public long fileSize;
        public String buildType;
    }

    private final Context appContext;

    public ApkMetadataReader(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public ApkMetadata read(File apkFile, String buildType) {
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            return null;
        }
        PackageManager pm = appContext.getPackageManager();
        PackageInfo info = pm.getPackageArchiveInfo(
                apkFile.getAbsolutePath(), 0);
        if (info == null || info.applicationInfo == null) {
            return null;
        }
        ApkMetadata metadata = new ApkMetadata();
        metadata.packageName = info.packageName;
        metadata.versionName = info.versionName;
        metadata.versionCode = info.versionCode;
        metadata.minSdk = info.applicationInfo.minSdkVersion;
        metadata.fileSize = apkFile.length();
        metadata.buildType = buildType;
        return metadata;
    }
}
