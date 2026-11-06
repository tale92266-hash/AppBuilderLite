package com.nobi.appbuilder.editor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Draws the canvas background, the phone-screen sheet and the design
 * grid. All Paints are created once; onDraw only does arithmetic, so
 * panning and zooming stay cheap. Grid: 8dp minor lines, 40dp major.
 */
public class CanvasGridRenderer {

    private static final float GRID_DP = 8f;
    private static final int MAJOR_EVERY = 5;

    private final Paint outsidePaint = new Paint();
    private final Paint screenPaint = new Paint();
    private final Paint framePaint = new Paint();
    private final Paint minorPaint = new Paint();
    private final Paint majorPaint = new Paint();
    private final RectF screenRect = new RectF();
    private final RectF designRect = new RectF();

    public CanvasGridRenderer() {
        outsidePaint.setColor(Color.parseColor("#EDEFF2"));
        screenPaint.setColor(Color.WHITE);
        framePaint.setColor(Color.parseColor("#C7CFDD"));
        framePaint.setStyle(Paint.Style.STROKE);
        framePaint.setStrokeWidth(2);
        minorPaint.setColor(Color.parseColor("#E5E7EB"));
        minorPaint.setStrokeWidth(1);
        majorPaint.setColor(Color.parseColor("#D1D5DB"));
        majorPaint.setStrokeWidth(1);
    }

    public void draw(Canvas canvas, CanvasTransform transform,
                     float designWidthDp, float designHeightDp, boolean gridVisible) {
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), outsidePaint);
        designRect.set(0, 0, designWidthDp, designHeightDp);
        transform.designToScreenRect(designRect, screenRect);
        canvas.drawRect(screenRect, screenPaint);
        if (!gridVisible) {
            canvas.drawRect(screenRect, framePaint);
            return;
        }
        float visibleLeft = Math.max(0, transform.screenToDesignX(0));
        float visibleTop = Math.max(0, transform.screenToDesignY(0));
        float visibleRight = Math.min(designWidthDp, transform.screenToDesignX(canvas.getWidth()));
        float visibleBottom = Math.min(designHeightDp, transform.screenToDesignY(canvas.getHeight()));
        float startX = (float) Math.floor(visibleLeft / GRID_DP) * GRID_DP;
        float startY = (float) Math.floor(visibleTop / GRID_DP) * GRID_DP;
        int index = 0;
        for (float gx = startX; gx <= visibleRight; gx += GRID_DP, index++) {
            Paint paint = (Math.round(gx / GRID_DP) % MAJOR_EVERY == 0) ? majorPaint : minorPaint;
            float sx = transform.designToScreenX(gx);
            canvas.drawLine(sx, screenRect.top, sx, screenRect.bottom, paint);
        }
        index = 0;
        for (float gy = startY; gy <= visibleBottom; gy += GRID_DP, index++) {
            Paint paint = (Math.round(gy / GRID_DP) % MAJOR_EVERY == 0) ? majorPaint : minorPaint;
            float sy = transform.designToScreenY(gy);
            canvas.drawLine(screenRect.left, sy, screenRect.right, sy, paint);
        }
        canvas.drawRect(screenRect, framePaint);
    }
}
