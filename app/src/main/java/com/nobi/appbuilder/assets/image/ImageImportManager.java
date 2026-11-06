package com.nobi.appbuilder.assets.image;

import android.content.Context;
import android.net.Uri;

import com.nobi.appbuilder.assets.AssetImportManager;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetType;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Batch image import through the asset importer. Animated GIFs
 * are kept animated — they are imported as-is and flagged, never
 * converted to a static frame.
 */
public class ImageImportManager {

    private final AssetImportManager delegate;

    public ImageImportManager(Context context, DatabaseHelper dbHelper) {
        this.delegate = new AssetImportManager(context, dbHelper);
    }

    public static class BatchResult {
        public final List<AssetModel> imported = new ArrayList<>();
        public final List<String> errors = new ArrayList<>();
        public int duplicates;
    }

    public BatchResult importImages(String projectId, List<Uri> uris,
                                    List<String> displayNames) {
        BatchResult result = new BatchResult();
        for (int i = 0; i < uris.size(); i++) {
            String name = i < displayNames.size() ? displayNames.get(i) : null;
            AssetImportManager.ImportResult imported =
                    delegate.importFromUri(projectId, uris.get(i), name);
            if (imported.error != null) {
                result.errors.add((name == null ? "Image" : name) + ": "
                        + imported.error);
            } else if (imported.duplicate) {
                result.duplicates++;
            } else if (imported.asset != null
                    && imported.asset.getType() == AssetType.IMAGE) {
                result.imported.add(imported.asset);
            } else {
                result.errors.add((name == null ? "Image" : name)
                        + ": not an image");
            }
        }
        return result;
    }
}
