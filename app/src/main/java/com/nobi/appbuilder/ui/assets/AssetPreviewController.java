package com.nobi.appbuilder.ui.assets;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetMetadata;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetPreviewManager;

import java.io.File;

/** Binds a preview area: downsampled image + description. */
public class AssetPreviewController {

    private final View root;

    public AssetPreviewController(View root) {
        this.root = root;
    }

    public void show(AssetModel asset, AssetMetadata metadata) {
        ImageView image = root.findViewById(R.id.image_asset_preview);
        TextView detail = root.findViewById(R.id.text_asset_preview_detail);
        detail.setText(AssetPreviewManager.previewDescription(asset, metadata));
        Bitmap bitmap = null;
        if (asset != null && asset.getPath() != null) {
            bitmap = AssetPreviewManager.previewBitmap(
                    new File(asset.getPath()), 512);
        }
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.GONE);
        }
    }
}
