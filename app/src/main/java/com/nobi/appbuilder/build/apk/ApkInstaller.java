package com.nobi.appbuilder.build.apk;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import java.io.File;

/**
 * Installs a built APK through the system package installer. Never
 * silent: the user sees the installer and confirms. Handles the
 * "install unknown apps" permission by sending the user to settings
 * when it is not granted.
 */
public class ApkInstaller {

    private final Context context;

    public ApkInstaller(Context context) {
        this.context = context;
    }

    /** True when the app may request installs from unknown sources. */
    public boolean canRequestInstall() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return true;
        }
        return context.getPackageManager().canRequestPackageInstalls();
    }

    /** Intent that opens the system install-unknown-apps settings. */
    public Intent installPermissionIntent() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        return intent;
    }

    /** Starts the system installer for this APK. Returns false when
     * the file is not installable from here. */
    public boolean install(File apkFile) {
        if (apkFile == null || !apkFile.exists() || apkFile.length() == 0) {
            return false;
        }
        Uri uri = FileProvider.getUriForFile(context,
                "com.nobi.appbuilder.fileprovider", apkFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri,
                "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(context.getPackageManager()) == null) {
            return false;
        }
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            // A resolving-but-unlaunchable installer must surface as a
            // "could not start" failure, never a crash or a silent tap.
            return false;
        }
        return true;
    }
}
