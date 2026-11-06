package com.nobi.appbuilder.build.apk;

import android.content.Context;

import com.nobi.appbuilder.build.BuildConfiguration;
import com.nobi.appbuilder.build.apk.ApkMetadataReader.ApkMetadata;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Validates a built APK against the requested configuration. */
public class ApkValidator {

    private final ApkMetadataReader reader;

    public ApkValidator(Context context) {
        this.reader = new ApkMetadataReader(context);
    }

    /** Problems; empty means the APK is valid for this build. */
    public List<String> validate(File apkFile, BuildConfiguration config) {
        List<String> problems = new ArrayList<>();
        if (apkFile == null || !apkFile.exists()) {
            problems.add("APK file does not exist");
            return problems;
        }
        if (apkFile.length() == 0) {
            problems.add("APK file is empty");
            return problems;
        }
        ApkMetadata metadata = reader.read(apkFile, config.getBuildType());
        if (metadata == null) {
            problems.add("APK cannot be parsed as an Android package");
            return problems;
        }
        if (config.getApplicationId() != null
                && !config.getApplicationId().equals(metadata.packageName)) {
            problems.add("APK package " + metadata.packageName
                    + " does not match requested "
                    + config.getApplicationId());
        }
        if (metadata.versionCode != config.getVersionCode()) {
            problems.add("APK version code " + metadata.versionCode
                    + " does not match requested "
                    + config.getVersionCode());
        }
        if (metadata.minSdk > config.getTargetSdk()) {
            problems.add("APK min SDK " + metadata.minSdk
                    + " is above the target SDK");
        }
        return problems;
    }

    public ApkMetadata readMetadata(File apkFile, String buildType) {
        return reader.read(apkFile, buildType);
    }
}
