package com.nobi.appbuilder.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * dp / sp / px conversions. The editor works in dp; this class is the
 * single place that converts between design coordinates and screen pixels.
 */
public final class DimensionUtils {

    private DimensionUtils() {
    }

    public static int dpToPx(Context context, float dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * metrics.density);
    }

    public static int pxToDp(Context context, float px) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(px / metrics.density);
    }

    public static int spToPx(Context context, float sp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(sp * metrics.scaledDensity);
    }

    public static int screenWidthPx(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int screenHeightPx(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int screenWidthDp(Context context) {
        return pxToDp(context, screenWidthPx(context));
    }
}
