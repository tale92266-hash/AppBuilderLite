package com.nobi.appbuilder.assets.image;

import android.graphics.Bitmap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Resizes from the ORIGINAL file only — never from an already
 * resized output. Density variants treat the original as the
 * xxxhdpi (4.0x) baseline and downscale, so nothing is upscaled.
 */
public final class ImageResizer {

    /** Density folders and their scale relative to the baseline. */
    public static final String[] DENSITY_DIRS = {
            "drawable-mdpi", "drawable-hdpi", "drawable-xhdpi",
            "drawable-xxhdpi", "drawable-xxxhdpi"
    };
    public static final float[] DENSITY_SCALES = {0.25f, 0.375f, 0.5f, 0.75f, 1.0f};

    private ImageResizer() {
    }

    public static boolean resizeTo(File source, File target,
                                   int targetWidth, int targetHeight) {
        Bitmap full = ImageProcessor.decodeFull(source);
        if (full == null) {
            return false;
        }
        Bitmap scaled = Bitmap.createScaledBitmap(full, Math.max(1, targetWidth),
                Math.max(1, targetHeight), true);
        boolean ok = writeBitmap(scaled, target);
        if (scaled != full) {
            scaled.recycle();
        }
        full.recycle();
        return ok;
    }

    /**
     * Writes density variants of an image into a res/ tree.
     * The original also goes to drawable/ unchanged.
     */
    public static List<String> generateDensityVariants(File source,
                                                       File resDir,
                                                       String baseName,
                                                       String extension) {
        List<String> written = new ArrayList<>();
        Bitmap full = ImageProcessor.decodeFull(source);
        if (full == null) {
            return written;
        }
        try {
            String fileName = baseName + "." + extension;
            File base = new File(resDir, "drawable");
            if (base.mkdirs() || base.exists()) {
                if (copyFile(source, new File(base, fileName))) {
                    written.add("drawable/" + fileName);
                }
            }
            for (int i = 0; i < DENSITY_DIRS.length; i++) {
                File dir = new File(resDir, DENSITY_DIRS[i]);
                if (!dir.mkdirs() && !dir.exists()) {
                    continue;
                }
                int width = Math.max(1,
                        Math.round(full.getWidth() * DENSITY_SCALES[i]));
                int height = Math.max(1,
                        Math.round(full.getHeight() * DENSITY_SCALES[i]));
                Bitmap scaled = Bitmap.createScaledBitmap(full, width, height, true);
                File target = new File(dir, fileName);
                if (writeBitmap(scaled, target)) {
                    written.add(DENSITY_DIRS[i] + "/" + fileName);
                }
                if (scaled != full) {
                    scaled.recycle();
                }
            }
        } finally {
            full.recycle();
        }
        return written;
    }

    public static boolean writeBitmap(Bitmap bitmap, File target) {
        if (bitmap == null || target == null) {
            return false;
        }
        File parent = target.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.PNG;
        String name = target.getName().toLowerCase();
        if (name.endsWith(".jpg") || name.endsWith(".jpeg")) {
            format = Bitmap.CompressFormat.JPEG;
        } else if (name.endsWith(".webp")) {
            format = Bitmap.CompressFormat.WEBP;
        }
        try (FileOutputStream out = new FileOutputStream(target)) {
            return bitmap.compress(format, 90, out);
        } catch (IOException e) {
            return false;
        }
    }

    private static boolean copyFile(File source, File target) {
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
