package com.nobi.appbuilder.preview;

/**
 * Activity-level preview UI state: zoom and the optional device frame.
 * Kept separate from PreviewSession (which owns the document and
 * navigation state) so zooming or hiding the frame never touches the
 * design. Survives rotation through the activity's saved state.
 */
public class PreviewState {

    public static final float MIN_ZOOM = 0.5f;
    public static final float MAX_ZOOM = 2.0f;
    public static final float ZOOM_STEP = 0.1f;

    private float zoom = 1.0f;
    private boolean frameVisible = true;

    public float getZoom() {
        return zoom;
    }

    public void setZoom(float zoom) {
        if (zoom < MIN_ZOOM) {
            zoom = MIN_ZOOM;
        }
        if (zoom > MAX_ZOOM) {
            zoom = MAX_ZOOM;
        }
        this.zoom = zoom;
    }

    public void zoomIn() {
        setZoom(zoom + ZOOM_STEP);
    }

    public void zoomOut() {
        setZoom(zoom - ZOOM_STEP);
    }

    /** Zoom as a whole percent for the bottom toolbar label. */
    public int getZoomPercent() {
        return Math.round(zoom * 100f);
    }

    public boolean isFrameVisible() {
        return frameVisible;
    }

    public void setFrameVisible(boolean frameVisible) {
        this.frameVisible = frameVisible;
    }
}
