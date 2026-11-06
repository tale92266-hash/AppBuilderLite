package com.nobi.appbuilder.preview.device;

import android.content.Context;

import com.nobi.appbuilder.preview.PreviewState;
import com.nobi.appbuilder.utils.DimensionUtils;

/**
 * Turns a device profile + orientation + zoom into the exact pixel
 * size of the device frame, and works out the zoom that fits a frame
 * inside the space the phone actually gives the preview area.
 * Content is never re-measured: only the frame is resized, so the
 * saved project dimensions stay untouched.
 */
public final class PreviewViewport {

    private PreviewViewport() {
    }

    public static int frameWidthPx(Context context, PreviewDeviceProfile profile,
                                   String orientation, float zoom) {
        float dp = profile.widthForOrientation(orientation) * zoom;
        return DimensionUtils.dpToPx(context, dp);
    }

    public static int frameHeightPx(Context context, PreviewDeviceProfile profile,
                                    String orientation, float zoom) {
        float dp = profile.heightForOrientation(orientation) * zoom;
        return DimensionUtils.dpToPx(context, dp);
    }

    /** Largest zoom (0.5–2.0) that still fits the frame in the area. */
    public static float fitZoom(Context context, PreviewDeviceProfile profile,
                                String orientation, int areaWidthPx,
                                int areaHeightPx) {
        if (areaWidthPx <= 0 || areaHeightPx <= 0) {
            return 1.0f;
        }
        float density = context.getResources().getDisplayMetrics().density;
        float frameW = profile.widthForOrientation(orientation) * density;
        float frameH = profile.heightForOrientation(orientation) * density;
        float zoom = Math.min(areaWidthPx / frameW, areaHeightPx / frameH);
        if (zoom < PreviewState.MIN_ZOOM) {
            zoom = PreviewState.MIN_ZOOM;
        }
        if (zoom > PreviewState.MAX_ZOOM) {
            zoom = PreviewState.MAX_ZOOM;
        }
        return zoom;
    }
}
