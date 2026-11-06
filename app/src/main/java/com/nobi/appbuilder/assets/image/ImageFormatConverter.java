package com.nobi.appbuilder.assets.image;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Format conversion between PNG, JPEG and WebP. Animated GIFs are
 * never silently converted to a static format — convert() refuses
 * them and reports why.
 */
public final class ImageFormatConverter {

    private ImageFormatConverter() {
    }

    public static boolean toWebP(File source, File target) {
        return ImageCompressor.compress(source, target,
                Bitmap.CompressFormat.WEBP, 90);
    }

    public static boolean toPng(File source, File target) {
        return ImageCompressor.compress(source, target,
                Bitmap.CompressFormat.PNG, 100);
    }

    public static boolean toJpeg(File source, File target, int quality) {
        return ImageCompressor.compress(source, target,
                Bitmap.CompressFormat.JPEG, quality);
    }

    /**
     * Converts only static images. Returns null on success, or the
     * reason the conversion was refused.
     */
    public static String convertChecked(File source, File target,
                                        Bitmap.CompressFormat format) {
        if (com.nobi.appbuilder.assets.AssetValidationManager
                .isAnimatedGif(source)) {
            return "Animated GIFs are not converted to a static format";
        }
        if (!ImageCompressor.compress(source, target, format, 90)) {
            return "Conversion failed";
        }
        return null;
    }
}
