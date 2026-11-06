package com.nobi.appbuilder.ui.flow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.nobi.appbuilder.flow.FlowNode;
import com.nobi.appbuilder.flow.FlowNodeType;

/**
 * Draws one flow node as a rounded box: type colour, label, and the
 * action id for ACTION nodes. Node size is fixed (260x90 dp).
 */
public class FlowNodeRenderer {

    public static final float NODE_WIDTH_DP = 260;
    public static final float NODE_HEIGHT_DP = 90;

    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public FlowNodeRenderer() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(36);
    }

    public RectF rectFor(FlowNode node, float density) {
        float left = node.getX() * density;
        float top = node.getY() * density;
        return new RectF(left, top,
                left + NODE_WIDTH_DP * density,
                top + NODE_HEIGHT_DP * density);
    }

    public void draw(Canvas canvas, FlowNode node, float density,
                     boolean selected) {
        RectF rect = rectFor(node, density);
        fillPaint.setColor(colorFor(node.getType()));
        canvas.drawRoundRect(rect, 18 * density, 18 * density, fillPaint);
        strokePaint.setColor(selected ? Color.YELLOW : Color.WHITE);
        strokePaint.setStrokeWidth(selected ? 6 : 3);
        canvas.drawRoundRect(rect, 18 * density, 18 * density, strokePaint);
        float textSize = 15 * density;
        textPaint.setTextSize(textSize);
        String title = node.getType().displayName() + ": "
                + (node.getLabel() == null ? "" : node.getLabel());
        canvas.drawText(title, rect.left + 12 * density,
                rect.top + 30 * density, textPaint);
        String detail = detailFor(node);
        if (detail != null && !detail.isEmpty()) {
            textPaint.setTextSize(12 * density);
            canvas.drawText(detail, rect.left + 12 * density,
                    rect.top + 56 * density, textPaint);
        }
    }

    private String detailFor(FlowNode node) {
        switch (node.getType()) {
            case ACTION:
                return node.getActionId() == null ? "" : node.getActionId();
            case CONDITION:
                return (node.getParam("operator") == null
                        ? "" : node.getParam("operator"))
                        + " " + (node.getParam("left") == null
                        ? "" : node.getParam("left"));
            case NAVIGATION:
                return node.getParam("targetScreenId") == null
                        ? "" : "→ " + node.getParam("targetScreenId");
            case VARIABLE:
                return (node.getParam("name") == null ? "" : node.getParam("name"))
                        + " = " + (node.getParam("value") == null
                        ? "" : node.getParam("value"));
            default:
                return "";
        }
    }

    public static int colorFor(FlowNodeType type) {
        switch (type) {
            case START:
                return Color.parseColor("#2E7D32");
            case END:
                return Color.parseColor("#C62828");
            case CONDITION:
                return Color.parseColor("#EF6C00");
            case NAVIGATION:
                return Color.parseColor("#6A1B9A");
            case VARIABLE:
                return Color.parseColor("#00838F");
            case EVENT:
                return Color.parseColor("#546E7A");
            case ACTION:
            default:
                return Color.parseColor("#1565C0");
        }
    }
}
