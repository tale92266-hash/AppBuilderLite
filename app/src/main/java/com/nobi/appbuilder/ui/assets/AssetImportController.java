package com.nobi.appbuilder.ui.assets;

import android.content.Context;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * Owns the SAF pickers: multiple images, or any file. Results go
 * to the listener on the main thread; importing itself runs on the
 * caller's executor.
 */
public class AssetImportController {

    public interface ImportListener {
        void onFilesPicked(List<Uri> uris, List<String> displayNames);
    }

    private final ActivityResultLauncher<String> imagePicker;
    private final ActivityResultLauncher<String> filePicker;
    private final ImportListener listener;

    public AssetImportController(AppCompatActivity activity,
                                 ImportListener listener) {
        this.listener = listener;
        imagePicker = activity.registerForActivityResult(
                new ActivityResultContracts.GetMultipleContents(),
                uris -> {
                    if (uris == null || uris.isEmpty()) {
                        return;
                    }
                    listener.onFilesPicked(uris, namesOf(activity, uris));
                });
        filePicker = activity.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri == null) {
                        return;
                    }
                    List<Uri> uris = new ArrayList<>();
                    uris.add(uri);
                    listener.onFilesPicked(uris, namesOf(activity, uris));
                });
    }

    public void pickImages() {
        imagePicker.launch("image/*");
    }

    public void pickFile() {
        filePicker.launch("*/*");
    }

    private List<String> namesOf(Context context, List<Uri> uris) {
        List<String> names = new ArrayList<>();
        for (Uri uri : uris) {
            String name = null;
            try (android.database.Cursor cursor = context.getContentResolver()
                    .query(uri, new String[]{
                            android.provider.OpenableColumns.DISPLAY_NAME},
                            null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    name = cursor.getString(0);
                }
            } catch (Exception ignored) {
            }
            if (name == null || name.isEmpty()) {
                name = uri.getLastPathSegment();
            }
            names.add(name == null ? "asset" : name);
        }
        return names;
    }
}
