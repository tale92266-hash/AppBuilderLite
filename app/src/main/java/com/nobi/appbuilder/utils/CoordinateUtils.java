package com.nobi.appbuilder.utils;

import android.content.Context;

import com.nobi.appbuilder.editor.CanvasTransform;

/**
 * Coordinate helpers for the editor. Design coordinates are dp relative
 * to the top-left of the screen being designed (360x640 dp by default);
 * screen coordinates are raw pixels on the canvas View.
 */
public final class CoordinateUtils {

    private CoordinateUtils() {
    }

    public static float dpToPx(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public static float dpToPx(float density, float dp) {
        return dp * density;
    }

    public static float pxToDp(Context context, float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxToDp(float density, float px) {
        return px / density;
    }

    public static float designToScreenX(CanvasTransform transform, float designDp) {
        return transform.designToScreenX(designDp);
    }

    public static float designToScreenY(CanvasTransform transform, float designDp) {
        return transform.designToScreenY(designDp);
    }

    public static float screenToDesignX(CanvasTransform transform, float screenPx) {
        return transform.screenToDesignX(screenPx);
    }

    public static float screenToDesignY(CanvasTransform transform, float screenPx) {
        return transform.screenToDesignY(screenPx);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    public static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /** Snaps a design value to the nearest grid multiple. */
    public static float snapToGrid(float value, float gridDp) {
        if (gridDp <= 0) {
            return value;
        }
        return Math.round(value / gridDp) * gridDp;
    }

    public static boolean pointInRect(float x, float y,
                                      float left, float top, float right, float bottom) {
        return x >= left && x <= right && y >= top && y <= bottom;
    }
}
