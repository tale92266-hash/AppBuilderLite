package com.nobi.appbuilder.ui.flow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nobi.appbuilder.navigation.NavigationEdge;
import com.nobi.appbuilder.navigation.NavigationGraph;
import com.nobi.appbuilder.navigation.NavigationNode;

/**
 * Draws the navigation graph: one box per screen, arrows for edges.
 * Tapping a screen selects it. Positions come from the graph nodes.
 */
public class NavigationGraphView extends View {

    public interface OnScreenSelectedListener {
        void onScreenSelected(String screenId);
    }

    private static final float BOX_WIDTH_DP = 180;
    private static final float BOX_HEIGHT_DP = 64;

    private NavigationGraph graph;
    private String selectedScreenId;
    private OnScreenSelectedListener listener;
    private final Paint boxPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public NavigationGraphView(Context context) {
        super(context);
        init();
    }

    public NavigationGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(3);
        strokePaint.setColor(Color.WHITE);
        textPaint.setColor(Color.WHITE);
        linePaint.setColor(Color.DKGRAY);
        linePaint.setStrokeWidth(4);
    }

    public void setGraph(NavigationGraph graph) {
        this.graph = graph;
        invalidate();
    }

    public void setSelectedScreenId(String screenId) {
        this.selectedScreenId = screenId;
        invalidate();
    }

    public void setOnScreenSelectedListener(OnScreenSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (graph == null) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        for (NavigationEdge edge : graph.getEdges()) {
            NavigationNode from = graph.getNode(edge.getFromScreenId());
            NavigationNode to = graph.getNode(edge.getToScreenId());
            if (from == null || to == null) {
                continue;
            }
            float fromX = (from.getX() + BOX_WIDTH_DP / 2) * density;
            float fromY = (from.getY() + BOX_HEIGHT_DP) * density;
            float toX = (to.getX() + BOX_WIDTH_DP / 2) * density;
            float toY = to.getY() * density;
            canvas.drawLine(fromX, fromY, toX, toY, linePaint);
        }
        for (NavigationNode node : graph.getNodes()) {
            RectF rect = new RectF(node.getX() * density,
                    node.getY() * density,
                    (node.getX() + BOX_WIDTH_DP) * density,
                    (node.getY() + BOX_HEIGHT_DP) * density);
            boxPaint.setColor(node.isLauncher()
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#1565C0"));
            canvas.drawRoundRect(rect, 14 * density, 14 * density, boxPaint);
            strokePaint.setStrokeWidth(node.getScreenId() != null
                    && node.getScreenId().equals(selectedScreenId) ? 6 : 3);
            canvas.drawRoundRect(rect, 14 * density, 14 * density,
                    strokePaint);
            textPaint.setTextSize(15 * density);
            String name = node.getName() == null ? "" : node.getName();
            if (name.length() > 18) {
                name = name.substring(0, 18) + "…";
            }
            canvas.drawText(name, rect.left + 12 * density,
                    rect.top + 28 * density, textPaint);
            textPaint.setTextSize(11 * density);
            canvas.drawText(node.isLauncher() ? "Start screen" : "Screen",
                    rect.left + 12 * density, rect.top + 48 * density,
                    textPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() != MotionEvent.ACTION_UP || graph == null) {
            return super.onTouchEvent(event);
        }
        float density = getResources().getDisplayMetrics().density;
        float x = event.getX();
        float y = event.getY();
        for (NavigationNode node : graph.getNodes()) {
            RectF rect = new RectF(node.getX() * density,
                    node.getY() * density,
                    (node.getX() + BOX_WIDTH_DP) * density,
                    (node.getY() + BOX_HEIGHT_DP) * density);
            if (rect.contains(x, y)) {
                selectedScreenId = node.getScreenId();
                invalidate();
                if (listener != null) {
                    listener.onScreenSelected(node.getScreenId());
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
