package com.nobi.appbuilder.build.apk;

import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Installs a built APK through the system package installer. Never
 * silent: the user sees the installer and confirms. Handles the
 * "install unknown apps" permission by sending the user to settings
 * when it is not granted.
 *
 * The install itself first tries the PackageInstaller session API:
 * the APK bytes are handed to the system in a session and the system
 * shows its own install confirmation. This does not depend on any
 * OEM installer activity resolving an ACTION_VIEW intent for the APK
 * — on some devices that intent silently goes nowhere and the user
 * lands back on the previous screen with nothing opened. If the
 * session path throws, it falls back to the ACTION_VIEW intent.
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
        if (installViaSession(apkFile)) {
            return true;
        }
        return installViaIntent(apkFile);
    }

    /**
     * Session API path. Returns false when the session could not be
     * created, written or committed — the caller then tries the
     * ACTION_VIEW fallback. The install confirmation and the result
     * are delivered to InstallResultReceiver.
     */
    private boolean installViaSession(File apkFile) {
        PackageInstaller installer =
                context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams params =
                new PackageInstaller.SessionParams(
                        PackageInstaller.SessionParams.MODE_FULL_INSTALL);
        int sessionId = -1;
        try {
            sessionId = installer.createSession(params);
            PackageInstaller.Session session =
                    installer.openSession(sessionId);
            try (InputStream in = new FileInputStream(apkFile);
                 OutputStream out = session.openWrite(
                         "package", 0, apkFile.length())) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                session.fsync(out);
            }
            session.close();
            Intent callback = new Intent(
                    context, InstallResultReceiver.class);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE;
            }
            PendingIntent pending = PendingIntent.getBroadcast(
                    context, sessionId, callback, flags);
            session.commit(pending.getIntentSender());
            return true;
        } catch (IOException | SecurityException | IllegalStateException e) {
            if (sessionId != -1) {
                try {
                    installer.abandonSession(sessionId);
                } catch (Exception ignored) {
                    // Best effort cleanup only.
                }
            }
            return false;
        }
    }

    /** Legacy ACTION_VIEW path, kept as a fallback. */
    private boolean installViaIntent(File apkFile) {
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
