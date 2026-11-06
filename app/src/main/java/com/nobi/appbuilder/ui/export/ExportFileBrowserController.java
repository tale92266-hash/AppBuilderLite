package com.nobi.appbuilder.ui.export;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.export.storage.ExportHistoryRepository.ExportRecord;
import com.nobi.appbuilder.utils.FileUtils;

import java.io.File;

/**
 * Open / share / delete actions for export history items. Sharing
 * always goes through FileProvider — raw filesystem paths are never
 * put in a share intent.
 */
public class ExportFileBrowserController {

    private final Context context;

    public ExportFileBrowserController(Context context) {
        this.context = context;
    }

    /** Content URI for a ZIP in the default exports dir. */
    public Uri uriFor(ExportRecord record) {
        if (record.outputUri != null
                && record.outputUri.startsWith("content://")) {
            return Uri.parse(record.outputUri);
        }
        File file = new File(FileUtils.getExportsDir(context),
                record.zipFileName);
        if (!file.exists()) {
            return null;
        }
        return FileProvider.getUriForFile(context,
                "com.nobi.appbuilder.fileprovider", file);
    }

    public void open(ExportRecord record) {
        Uri uri = uriFor(record);
        if (uri == null) {
            Toast.makeText(context, R.string.export_file_missing,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "application/zip");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            Toast.makeText(context, R.string.export_no_viewer,
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void share(ExportRecord record) {
        Uri uri = uriFor(record);
        if (uri == null) {
            Toast.makeText(context, R.string.export_file_missing,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/zip");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent,
                context.getString(R.string.export_share)));
    }
}
