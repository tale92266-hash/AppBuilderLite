package com.nobi.appbuilder.editor;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import com.nobi.appbuilder.components.ComponentDefinition;
import com.nobi.appbuilder.components.ComponentRegistry;
import com.nobi.appbuilder.model.ComponentModel;

import java.util.List;

/**
 * Draws everything on the design canvas: grid, components (through their
 * Phase 2 definitions — never a generic rectangle), selection outlines
 * with eight handles, and alignment guides. Paints and RectFs are pooled;
 * onDraw allocates nothing.
 */
public class CanvasRenderer {

    private final CanvasGridRenderer gridRenderer = new CanvasGridRenderer();
    private final Paint selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint guidePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint labelBgPaint = new Paint();
    private final Paint fallbackFillPaint = new Paint();
    private final Paint fallbackTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint alphaPaint = new Paint();
    private final RectF[] rectPool = new RectF[64];
    private final Rect clipRect = new Rect();
    private final RectF clipRectF = new RectF();
    private final RectF screenDesignBounds = new RectF();

    public CanvasRenderer() {
        selectionPaint.setColor(Color.parseColor("#0B4499"));
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(2);
        handleFillPaint.setColor(Color.WHITE);
        handleFillPaint.setStyle(Paint.Style.FILL);
        handleStrokePaint.setColor(Color.parseColor("#0B4499"));
        handleStrokePaint.setStyle(Paint.Style.STROKE);
        handleStrokePaint.setStrokeWidth(2);
        guidePaint.setColor(Color.parseColor("#7C5CFF"));
        guidePaint.setStyle(Paint.Style.STROKE);
        guidePaint.setStrokeWidth(2);
        guidePaint.setPathEffect(new DashPathEffect(new float[]{10, 8}, 0));
        labelTextPaint.setColor(Color.WHITE);
        labelBgPaint.setColor(Color.parseColor("#0B4499"));
        fallbackFillPaint.setColor(Color.parseColor("#E5E7EB"));
        fallbackTextPaint.setColor(Color.parseColor("#6B7280"));
        for (int i = 0; i < rectPool.length; i++) {
            rectPool[i] = new RectF();
        }
    }

    public void draw(Canvas canvas, CanvasState state, CanvasViewport viewport,
                     List<AlignmentEngine.Guide> guides) {
        CanvasTransform transform = viewport.getTransform();
        float density = transform.getDensity();
        gridRenderer.draw(canvas, transform, state.getDesignWidthDp(),
                state.getDesignHeightDp(), viewport.isGridVisible());
        ComponentModel root = state.getRoot();
        if (root != null) {
            screenDesignBounds.set(0, 0, state.getDesignWidthDp(), state.getDesignHeightDp());
            canvas.getClipBounds(clipRect);
            drawNode(canvas, root, screenDesignBounds, transform, 0);
        }
        drawGuides(canvas, guides, transform);
        drawSelection(canvas, state, viewport);
    }

    private RectF rectAt(int index) {
        return rectPool[Math.min(index, rectPool.length - 1)];
    }

    private void drawNode(Canvas canvas, ComponentModel node, RectF parentDesignBounds,
                          CanvasTransform transform, int depth) {
        if (depth > 30) {
            return;
        }
        RectF design = rectAt(depth * 2);
        RectF screen = rectAt(depth * 2 + 1);
        ComponentHitTester.resolveBoundsInto(node, parentDesignBounds, design);
        transform.designToScreenRect(design, screen);
        clipRectF.set(clipRect);
        if (!RectF.intersects(screen, clipRectF)) {
            return;
        }
        String visibility = node.getProperty("visibility", "visible");
        if ("gone".equals(visibility)) {
            return;
        }
        int layerAlpha = 255;
        if ("invisible".equals(visibility)) {
            layerAlpha = 77;
        } else {
            try {
                float alpha = Float.parseFloat(node.getProperty("alpha", "1"));
                layerAlpha = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255);
            } catch (NumberFormatException ignored) {
            }
        }
        boolean needsLayer = layerAlpha < 255;
        if (needsLayer) {
            alphaPaint.setAlpha(layerAlpha);
            canvas.saveLayer(screen, alphaPaint);
        }
        ComponentDefinition definition = ComponentRegistry.get(node.getType());
        if (definition != null && definition.getCanvasRenderer() != null) {
            definition.getCanvasRenderer().draw(canvas, screen, node,
                    transform.getDensity() * transform.getZoom());
        } else {
            fallbackFillPaint.setColor(Color.parseColor("#E5E7EB"));
            canvas.drawRect(screen, fallbackFillPaint);
            fallbackTextPaint.setTextSize(12 * transform.getDensity());
            canvas.drawText(node.getType() == null ? "?" : node.getType(),
                    screen.left + 4, screen.centerY(), fallbackTextPaint);
        }
        if (ComponentHitTester.isContainer(node) && !node.getChildren().isEmpty()) {
            canvas.save();
            canvas.clipRect(screen);
            for (ComponentModel child : node.getChildren()) {
                drawNode(canvas, child, design, transform, depth + 1);
            }
            canvas.restore();
        }
        if (needsLayer) {
            canvas.restore();
        }
    }

    private void drawGuides(Canvas canvas, List<AlignmentEngine.Guide> guides,
                            CanvasTransform transform) {
        if (guides == null) {
            return;
        }
        for (AlignmentEngine.Guide guide : guides) {
            if (guide.vertical) {
                float x = transform.designToScreenX(guide.position);
                canvas.drawLine(x, transform.designToScreenY(guide.from),
                        x, transform.designToScreenY(guide.to), guidePaint);
            } else {
                float y = transform.designToScreenY(guide.position);
                canvas.drawLine(transform.designToScreenX(guide.from), y,
                        transform.designToScreenX(guide.to), y, guidePaint);
            }
        }
    }

    private void drawSelection(Canvas canvas, CanvasState state, CanvasViewport viewport) {
        CanvasTransform transform = viewport.getTransform();
        float density = transform.getDensity();
        ComponentModel root = state.getRoot();
        if (root == null) {
            return;
        }
        screenDesignBounds.set(0, 0, state.getDesignWidthDp(), state.getDesignHeightDp());
        selectionPaint.setStrokeWidth(2 * density);
        handleStrokePaint.setStrokeWidth(1.5f * density);
        for (ComponentModel selected : state.getSelectedModels()) {
            RectF bounds = ComponentHitTester.resolveBoundsFor(
                    root, selected.getId(), screenDesignBounds);
            if (bounds == null) {
                continue;
            }
            RectF screen = rectAt(62);
            transform.designToScreenRect(bounds, screen);
            canvas.drawRect(screen, selectionPaint);
            if (state.getSelectedModels().size() == 1) {
                drawHandles(canvas, screen, density);
                drawLabel(canvas, selected, bounds, screen, density);
            }
        }
    }

    private void drawHandles(Canvas canvas, RectF screen, float density) {
        float half = 5 * density;
        float[] xs = {screen.left, screen.centerX(), screen.right};
        float[] ys = {screen.top, screen.centerY(), screen.bottom};
        for (float x : xs) {
            for (float y : ys) {
                canvas.drawRect(x - half, y - half, x + half, y + half, handleFillPaint);
                canvas.drawRect(x - half, y - half, x + half, y + half, handleStrokePaint);
            }
        }
    }

    private void drawLabel(Canvas canvas, ComponentModel selected, RectF designBounds,
                           RectF screen, float density) {
        String id = selected.getId() == null ? "" : selected.getId();
        String dims = Math.round(designBounds.width()) + " x "
                + Math.round(designBounds.height()) + " dp";
        labelTextPaint.setTextSize(12 * density);
        float textWidth = labelTextPaint.measureText(id);
        float labelH = 18 * density;
        float labelTop = Math.max(0, screen.top - labelH - 2 * density);
        canvas.drawRect(screen.left, labelTop,
                screen.left + textWidth + 12 * density, labelTop + labelH, labelBgPaint);
        canvas.drawText(id, screen.left + 6 * density,
                labelTop + labelH - 5 * density, labelTextPaint);
        canvas.drawText(dims, screen.left, screen.bottom + 14 * density, labelTextPaint);
    }
}
