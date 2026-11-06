package com.nobi.appbuilder.assets.image;

import android.graphics.Bitmap;

import java.io.File;

/** Center-crop to a target aspect ratio (writes a new file). */
public final class ImageCropManager {

    private ImageCropManager() {
    }

    public static boolean centerCrop(File source, File target,
                                     int aspectWidth, int aspectHeight) {
        Bitmap full = ImageProcessor.decodeFull(source);
        if (full == null || aspectWidth <= 0 || aspectHeight <= 0) {
            return false;
        }
        float targetAspect = aspectWidth / (float) aspectHeight;
        float sourceAspect = full.getWidth() / (float) full.getHeight();
        Bitmap cropped;
        if (sourceAspect > targetAspect) {
            int width = Math.round(full.getHeight() * targetAspect);
            int x = (full.getWidth() - width) / 2;
            cropped = Bitmap.createBitmap(full, x, 0, width, full.getHeight());
        } else {
            int height = Math.round(full.getWidth() / targetAspect);
            int y = (full.getHeight() - height) / 2;
            cropped = Bitmap.createBitmap(full, 0, y, full.getWidth(), height);
        }
        boolean ok = ImageResizer.writeBitmap(cropped, target);
        if (cropped != full) {
            cropped.recycle();
        }
        full.recycle();
        return ok;
    }
}
