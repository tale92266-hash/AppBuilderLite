package com.nobi.appbuilder.utils;

import android.content.Context;
import android.view.ViewConfiguration;

/**
 * Touch helpers: tap-vs-drag decisions use the platform touch slop and a
 * tap timeout so a finger that barely moves still counts as a tap.
 */
public final class TouchUtils {

    /** Maximum press length that still counts as a tap. */
    public static final long TAP_TIMEOUT_MS = 300;

    private TouchUtils() {
    }

    public static int getTouchSlop(Context context) {
        return ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public static boolean isTap(float downX, float downY, float upX, float upY,
                                int touchSlop, long downTime, long upTime) {
        float dx = upX - downX;
        float dy = upY - downY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        return distance <= touchSlop && (upTime - downTime) <= TAP_TIMEOUT_MS;
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    public static float midX(float x1, float x2) {
        return (x1 + x2) / 2f;
    }

    public static float midY(float y1, float y2) {
        return (y1 + y2) / 2f;
    }
}
