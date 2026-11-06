package com.nobi.appbuilder.build.apk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;

import java.io.File;

/** Shares a built APK through a FileProvider content URI. */
public class ApkShareManager {

    private final Context context;

    public ApkShareManager(Context context) {
        this.context = context;
    }

    public void share(File apkFile) {
        if (apkFile == null || !apkFile.exists()) {
            return;
        }
        Uri uri = FileProvider.getUriForFile(context,
                "com.nobi.appbuilder.fileprovider", apkFile);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.android.package-archive");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent,
                "Share APK"));
    }
}
