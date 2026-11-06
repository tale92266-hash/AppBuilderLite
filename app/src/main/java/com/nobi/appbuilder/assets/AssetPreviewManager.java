package com.nobi.appbuilder.assets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

/**
 * Lightweight previews: images are downsampled to a small edge
 * before decoding — full-resolution images are never loaded into
 * memory for a thumbnail.
 */
public final class AssetPreviewManager {

    private AssetPreviewManager() {
    }

    public static Bitmap previewBitmap(File file, int maxEdge) {
        if (file == null || !file.exists()) {
            return null;
        }
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file.getAbsolutePath(), bounds);
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            return null;
        }
        int sample = 1;
        int largest = Math.max(bounds.outWidth, bounds.outHeight);
        while (largest / sample > maxEdge) {
            sample *= 2;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sample;
        return BitmapFactory.decodeFile(file.getAbsolutePath(), options);
    }

    public static String previewDescription(AssetModel asset,
                                            AssetMetadata metadata) {
        if (asset == null) {
            return "";
        }
        StringBuilder description = new StringBuilder();
        description.append(asset.getName()).append(" · ")
                .append(asset.getType().getDisplayName());
        if (metadata != null && metadata.getWidth() > 0) {
            description.append(" · ").append(metadata.getWidth())
                    .append("×").append(metadata.getHeight());
            if (metadata.isAnimated()) {
                description.append(" · animated");
            }
        }
        if (asset.getSizeBytes() > 0) {
            description.append(" · ").append(formatSize(asset.getSizeBytes()));
        }
        return description.toString();
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        if (bytes < 1024 * 1024) {
            return (bytes / 1024) + " KB";
        }
        return String.format("%.1f MB", bytes / (1024f * 1024f));
    }
}
