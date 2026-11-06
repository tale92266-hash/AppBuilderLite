package com.nobi.appbuilder.assets.image;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Compression to a NEW file — the original is never overwritten.
 * JPEG quality 0–100; WebP uses the same scale.
 */
public final class ImageCompressor {

    private ImageCompressor() {
    }

    public static boolean compress(File source, File target,
                                   Bitmap.CompressFormat format, int quality) {
        if (source == null || target == null
                || source.getAbsolutePath().equals(target.getAbsolutePath())) {
            return false;
        }
        Bitmap bitmap = ImageProcessor.decodeFull(source);
        if (bitmap == null) {
            return false;
        }
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return false;
            }
            try (FileOutputStream out = new FileOutputStream(target)) {
                return bitmap.compress(format,
                        Math.max(0, Math.min(100, quality)), out);
            }
        } catch (IOException e) {
            return false;
        } finally {
            bitmap.recycle();
        }
    }
}
