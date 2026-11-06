package com.nobi.appbuilder.editor;

import android.graphics.RectF;

/**
 * The editor's coordinate math. Design coordinates are dp; screen
 * coordinates are pixels on the canvas View.
 *
 * ScreenX = TranslationX + DesignX * Density * Zoom
 * DesignX = (ScreenX - TranslationX) / (Density * Zoom)
 *
 * Zoom never changes stored component dimensions — it only changes how
 * design coordinates map to the screen.
 */
public class CanvasTransform {

    public static final float MIN_ZOOM = 0.25f;
    public static final float MAX_ZOOM = 3.0f;

    private final float density;
    private float zoom;
    private float translateX;
    private float translateY;

    public CanvasTransform(float density) {
        this.density = density;
        this.zoom = 1.0f;
    }

    public float getDensity() {
        return density;
    }

    public float getZoom() {
        return zoom;
    }

    public float getTranslateX() {
        return translateX;
    }

    public float getTranslateY() {
        return translateY;
    }

    /** Effective pixels per design dp. */
    public float getScale() {
        return density * zoom;
    }

    public float designToScreenX(float designDp) {
        return translateX + designDp * getScale();
    }

    public float designToScreenY(float designDp) {
        return translateY + designDp * getScale();
    }

    public float screenToDesignX(float screenPx) {
        return (screenPx - translateX) / getScale();
    }

    public float screenToDesignY(float screenPx) {
        return (screenPx - translateY) / getScale();
    }

    public void designToScreenRect(RectF designRect, RectF outScreenRect) {
        outScreenRect.set(
                designToScreenX(designRect.left),
                designToScreenY(designRect.top),
                designToScreenX(designRect.right),
                designToScreenY(designRect.bottom));
    }

    public void screenToDesignRect(RectF screenRect, RectF outDesignRect) {
        outDesignRect.set(
                screenToDesignX(screenRect.left),
                screenToDesignY(screenRect.top),
                screenToDesignX(screenRect.right),
                screenToDesignY(screenRect.bottom));
    }

    public void setZoom(float newZoom) {
        zoom = clampZoom(newZoom);
    }

    /**
     * Changes zoom keeping the design point under the screen focal point
     * exactly where it is (pinch focal point, double-tap point, centre).
     */
    public void setZoomAround(float focusScreenX, float focusScreenY, float newZoom) {
        float designX = screenToDesignX(focusScreenX);
        float designY = screenToDesignY(focusScreenY);
        zoom = clampZoom(newZoom);
        translateX = focusScreenX - designX * getScale();
        translateY = focusScreenY - designY * getScale();
    }

    public void translateBy(float dxPx, float dyPx) {
        translateX += dxPx;
        translateY += dyPx;
    }

    public void setTranslation(float xPx, float yPx) {
        translateX = xPx;
        translateY = yPx;
    }

    /**
     * Fits the whole design screen into the view and centres it.
     * Used on first layout and for "fit to screen".
     */
    public void fitToScreen(float viewWidthPx, float viewHeightPx,
                            float designWidthDp, float designHeightDp) {
        if (viewWidthPx <= 0 || viewHeightPx <= 0) {
            return;
        }
        float zoomX = viewWidthPx / (designWidthDp * density);
        float zoomY = viewHeightPx / (designHeightDp * density);
        zoom = clampZoom(Math.min(zoomX, zoomY) * 0.95f);
        translateX = (viewWidthPx - designWidthDp * getScale()) / 2f;
        translateY = (viewHeightPx - designHeightDp * getScale()) / 2f;
    }

    public static float clampZoom(float zoom) {
        return Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom));
    }
}
