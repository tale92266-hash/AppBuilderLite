package com.nobi.appbuilder.editor;

/**
 * Owns the CanvasTransform plus the view size and the editor's display
 * flags (grid, snap). All zoom/pan gestures go through here so the
 * transform stays consistent: zoom is centred on the gesture focal
 * point, pan only moves the translation.
 */
public class CanvasViewport {

    private static final float ZOOM_STEP = 1.25f;

    private final CanvasTransform transform;
    private float viewWidthPx;
    private float viewHeightPx;
    private float designWidthDp;
    private float designHeightDp;
    private boolean gridVisible;
    private boolean snapEnabled;
    private boolean fitted;

    public CanvasViewport(float density, float designWidthDp, float designHeightDp) {
        this.transform = new CanvasTransform(density);
        this.designWidthDp = designWidthDp;
        this.designHeightDp = designHeightDp;
        this.gridVisible = true;
        this.snapEnabled = true;
    }

    public CanvasTransform getTransform() {
        return transform;
    }

    public float getZoom() {
        return transform.getZoom();
    }

    public int getZoomPercent() {
        return Math.round(transform.getZoom() * 100);
    }

    public boolean isGridVisible() {
        return gridVisible;
    }

    public void setGridVisible(boolean gridVisible) {
        this.gridVisible = gridVisible;
    }

    public boolean isSnapEnabled() {
        return snapEnabled;
    }

    public void setSnapEnabled(boolean snapEnabled) {
        this.snapEnabled = snapEnabled;
    }

    public float getDesignWidthDp() {
        return designWidthDp;
    }

    public float getDesignHeightDp() {
        return designHeightDp;
    }

    /** Called from the canvas View's onSizeChanged. */
    public void onSizeChanged(float viewWidthPx, float viewHeightPx) {
        this.viewWidthPx = viewWidthPx;
        this.viewHeightPx = viewHeightPx;
        if (!fitted) {
            fitToScreen();
            fitted = true;
        }
    }

    public void zoomIn() {
        zoomAroundCentre(transform.getZoom() * ZOOM_STEP);
    }

    public void zoomOut() {
        zoomAroundCentre(transform.getZoom() / ZOOM_STEP);
    }

    public void zoomAroundCentre(float newZoom) {
        transform.setZoomAround(viewWidthPx / 2f, viewHeightPx / 2f, newZoom);
    }

    /** Pinch zoom around the gesture focal point. */
    public void pinchZoom(float focusX, float focusY, float scaleFactor) {
        transform.setZoomAround(focusX, focusY, transform.getZoom() * scaleFactor);
    }

    /** Double-tap toggles between 100% and 200% around the tap point. */
    public void doubleTapZoom(float x, float y) {
        float target = transform.getZoom() < 1.5f ? 2.0f : 1.0f;
        transform.setZoomAround(x, y, target);
    }

    public void panBy(float dxPx, float dyPx) {
        transform.translateBy(dxPx, dyPx);
    }

    public void resetZoom() {
        transform.setZoomAround(viewWidthPx / 2f, viewHeightPx / 2f, 1.0f);
    }

    public void fitToScreen() {
        transform.fitToScreen(viewWidthPx, viewHeightPx, designWidthDp, designHeightDp);
    }
}
