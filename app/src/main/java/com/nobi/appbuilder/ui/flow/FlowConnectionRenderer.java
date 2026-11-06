package com.nobi.appbuilder.ui.flow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

/**
 * Draws one flow connection: a line from the bottom-centre of the
 * source node to the top-centre of the target node, with an arrowhead
 * and a port label ("true"/"false" for conditions).
 */
public class FlowConnectionRenderer {

    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FlowConnectionRenderer() {
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStrokeWidth(4);
        textPaint.setColor(Color.DKGRAY);
        textPaint.setTextSize(30);
    }

    public void draw(Canvas canvas, float fromX, float fromY,
                     float toX, float toY, String port, float density) {
        canvas.drawLine(fromX, fromY, toX, toY, linePaint);
        drawArrow(canvas, fromX, fromY, toX, toY, density);
        if (port != null && !"next".equals(port)) {
            textPaint.setTextSize(13 * density);
            canvas.drawText(port, (fromX + toX) / 2 + 8 * density,
                    (fromY + toY) / 2, textPaint);
        }
    }

    private void drawArrow(Canvas canvas, float fromX, float fromY,
                           float toX, float toY, float density) {
        double angle = Math.atan2(toY - fromY, toX - fromX);
        float size = 16 * density;
        Path path = new Path();
        path.moveTo(toX, toY);
        path.lineTo((float) (toX - size * Math.cos(angle - 0.4)),
                (float) (toY - size * Math.sin(angle - 0.4)));
        path.lineTo((float) (toX - size * Math.cos(angle + 0.4)),
                (float) (toY - size * Math.sin(angle + 0.4)));
        path.close();
        Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
        fill.setColor(Color.DKGRAY);
        fill.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, fill);
    }
}
