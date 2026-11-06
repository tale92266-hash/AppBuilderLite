package com.nobi.appbuilder.ui.flow;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.nobi.appbuilder.flow.FlowConnection;
import com.nobi.appbuilder.flow.FlowGraph;
import com.nobi.appbuilder.flow.FlowNode;

/**
 * Draws a flow graph: nodes via FlowNodeRenderer, connections via
 * FlowConnectionRenderer. Tapping a node selects it.
 */
public class FlowCanvasView extends View {

    public interface OnNodeSelectedListener {
        void onNodeSelected(FlowNode node);
    }

    private FlowGraph graph;
    private String selectedNodeId;
    private OnNodeSelectedListener listener;
    private final FlowNodeRenderer nodeRenderer = new FlowNodeRenderer();
    private final FlowConnectionRenderer connectionRenderer =
            new FlowConnectionRenderer();

    public FlowCanvasView(Context context) {
        super(context);
    }

    public FlowCanvasView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setGraph(FlowGraph graph) {
        this.graph = graph;
        invalidate();
    }

    public void setSelectedNodeId(String nodeId) {
        this.selectedNodeId = nodeId;
        invalidate();
    }

    public void setOnNodeSelectedListener(OnNodeSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (graph == null) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        for (FlowConnection connection : graph.getConnections()) {
            FlowNode from = graph.getNode(connection.getFromNodeId());
            FlowNode to = graph.getNode(connection.getToNodeId());
            if (from == null || to == null) {
                continue;
            }
            float fromX = (from.getX() + FlowNodeRenderer.NODE_WIDTH_DP / 2)
                    * density;
            float fromY = (from.getY() + FlowNodeRenderer.NODE_HEIGHT_DP)
                    * density;
            float toX = (to.getX() + FlowNodeRenderer.NODE_WIDTH_DP / 2)
                    * density;
            float toY = to.getY() * density;
            connectionRenderer.draw(canvas, fromX, fromY, toX, toY,
                    connection.getFromPort(), density);
        }
        for (FlowNode node : graph.getNodes()) {
            nodeRenderer.draw(canvas, node, density,
                    node.getId() != null
                            && node.getId().equals(selectedNodeId));
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
        for (FlowNode node : graph.getNodes()) {
            android.graphics.RectF rect = nodeRenderer.rectFor(node, density);
            if (rect.contains(x, y)) {
                selectedNodeId = node.getId();
                invalidate();
                if (listener != null) {
                    listener.onNodeSelected(node);
                }
                return true;
            }
        }
        return super.onTouchEvent(event);
    }
}
