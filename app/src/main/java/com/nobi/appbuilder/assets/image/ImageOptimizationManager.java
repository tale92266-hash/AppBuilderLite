package com.nobi.appbuilder.assets.image;

import android.graphics.Bitmap;

import java.io.File;

/**
 * One-step optimization: downsample to a max edge, then WebP.
 * The source file is never touched — output goes to a new file.
 */
public final class ImageOptimizationManager {

    private ImageOptimizationManager() {
    }

    public static boolean optimize(File source, File target, int maxEdge,
                                   int quality) {
        Bitmap bitmap = ImageProcessor.decode(source, maxEdge);
        if (bitmap == null) {
            return false;
        }
        try {
            File parent = target.getParentFile();
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                return false;
            }
            return ImageResizer.writeBitmap(bitmap, target);
        } finally {
            bitmap.recycle();
        }
    }

    public static File optimizedPath(File source) {
        String name = source.getName();
        int dot = name.lastIndexOf('.');
        String base = dot > 0 ? name.substring(0, dot) : name;
        return new File(source.getParentFile(), base + "-optimized.webp");
    }
}
