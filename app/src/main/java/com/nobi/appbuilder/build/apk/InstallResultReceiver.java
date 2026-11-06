package com.nobi.appbuilder.build.apk;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

import com.nobi.appbuilder.R;

/**
 * Receives the PackageInstaller session result for an install
 * started by ApkInstaller. The important case is
 * STATUS_PENDING_USER_ACTION: the system hands us the installer
 * confirmation intent, and NOTHING opens unless we start it — that
 * is the step the old ACTION_VIEW path left to the OEM installer,
 * which on some devices never shows anything.
 */
public class InstallResultReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS,
                PackageInstaller.STATUS_FAILURE);
        if (status == PackageInstaller.STATUS_PENDING_USER_ACTION) {
            Intent confirm = intent.getParcelableExtra(Intent.EXTRA_INTENT);
            if (confirm != null) {
                confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(confirm);
            }
            return;
        }
        if (status == PackageInstaller.STATUS_SUCCESS) {
            Toast.makeText(context, R.string.build_install_success,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        String message = intent.getStringExtra(
                PackageInstaller.EXTRA_STATUS_MESSAGE);
        Toast.makeText(context,
                context.getString(R.string.build_install_failed_status,
                        message != null ? message : ""),
                Toast.LENGTH_LONG).show();
    }
}
