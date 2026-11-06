package com.nobi.appbuilder.assets.image;

import android.graphics.BitmapFactory;

import com.nobi.appbuilder.assets.AssetMetadata;
import com.nobi.appbuilder.assets.AssetValidationManager;

import java.io.File;

/** Reads dimensions/format without decoding the full image. */
public final class ImageMetadataReader {

    private ImageMetadataReader() {
    }

    public static AssetMetadata read(File file, String assetId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null;
        }
        AssetMetadata metadata = new AssetMetadata();
        metadata.setAssetId(assetId);
        metadata.setWidth(options.outWidth);
        metadata.setHeight(options.outHeight);
        metadata.setFormat(options.outMimeType);
        metadata.setAnimated(AssetValidationManager.isAnimatedGif(file));
        return metadata;
    }
}
