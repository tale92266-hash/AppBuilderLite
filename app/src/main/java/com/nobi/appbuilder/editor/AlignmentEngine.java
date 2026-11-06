package com.nobi.appbuilder.editor;

import android.graphics.RectF;

import java.util.ArrayList;
import java.util.List;

/**
 * Alignment guides: when a moving component's edge or centre comes within
 * a few dp of a sibling's edge/centre (or the parent's), snap it exactly
 * and report the guide line to draw. Only the closest candidate per axis
 * wins, so guides never cause large jumps.
 */
public final class AlignmentEngine {

    /** One guide line, in design dp. */
    public static class Guide {
        public final boolean vertical;
        public final float position;
        public final float from;
        public final float to;

        public Guide(boolean vertical, float position, float from, float to) {
            this.vertical = vertical;
            this.position = position;
            this.from = from;
            this.to = to;
        }
    }

    /** Snap adjustment plus the guides to draw. */
    public static class Alignment {
        public float dx;
        public float dy;
        public final List<Guide> guides = new ArrayList<>();
    }

    private AlignmentEngine() {
    }

    /**
     * Finds the best vertical and horizontal alignment for a rectangle at
     * its proposed position against sibling rectangles and the parent.
     */
    public static Alignment findAlignment(RectF moving, List<RectF> siblings,
                                          RectF parentBounds, float thresholdDp) {
        Alignment result = new Alignment();
        float bestDx = 0;
        float bestDy = 0;
        float bestDxDistance = thresholdDp + 1;
        float bestDyDistance = thresholdDp + 1;
        Guide bestVertical = null;
        Guide bestHorizontal = null;

        List<float[]> verticalCandidates = new ArrayList<>();
        List<float[]> horizontalCandidates = new ArrayList<>();
        if (parentBounds != null) {
            verticalCandidates.add(new float[]{parentBounds.left, parentBounds.top, parentBounds.bottom});
            verticalCandidates.add(new float[]{parentBounds.centerX(), parentBounds.top, parentBounds.bottom});
            verticalCandidates.add(new float[]{parentBounds.right, parentBounds.top, parentBounds.bottom});
            horizontalCandidates.add(new float[]{parentBounds.top, parentBounds.left, parentBounds.right});
            horizontalCandidates.add(new float[]{parentBounds.centerY(), parentBounds.left, parentBounds.right});
            horizontalCandidates.add(new float[]{parentBounds.bottom, parentBounds.left, parentBounds.right});
        }
        for (RectF sibling : siblings) {
            verticalCandidates.add(new float[]{sibling.left, sibling.top, sibling.bottom});
            verticalCandidates.add(new float[]{sibling.centerX(), sibling.top, sibling.bottom});
            verticalCandidates.add(new float[]{sibling.right, sibling.top, sibling.bottom});
            horizontalCandidates.add(new float[]{sibling.top, sibling.left, sibling.right});
            horizontalCandidates.add(new float[]{sibling.centerY(), sibling.left, sibling.right});
            horizontalCandidates.add(new float[]{sibling.bottom, sibling.left, sibling.right});
        }

        float[] movingVertical = {moving.left, moving.centerX(), moving.right};
        for (float[] candidate : verticalCandidates) {
            for (float edge : movingVertical) {
                float diff = candidate[0] - edge;
                float distance = Math.abs(diff);
                if (distance <= thresholdDp && distance < bestDxDistance) {
                    bestDxDistance = distance;
                    bestDx = diff;
                    float from = Math.min(moving.top, candidate[1]);
                    float to = Math.max(moving.bottom, candidate[2]);
                    bestVertical = new Guide(true, candidate[0], from, to);
                }
            }
        }
        float[] movingHorizontal = {moving.top, moving.centerY(), moving.bottom};
        for (float[] candidate : horizontalCandidates) {
            for (float edge : movingHorizontal) {
                float diff = candidate[0] - edge;
                float distance = Math.abs(diff);
                if (distance <= thresholdDp && distance < bestDyDistance) {
                    bestDyDistance = distance;
                    bestDy = diff;
                    float from = Math.min(moving.left, candidate[1]);
                    float to = Math.max(moving.right, candidate[2]);
                    bestHorizontal = new Guide(false, candidate[0], from, to);
                }
            }
        }
        result.dx = bestDx;
        result.dy = bestDy;
        if (bestVertical != null) {
            result.guides.add(bestVertical);
        }
        if (bestHorizontal != null) {
            result.guides.add(bestHorizontal);
        }
        return result;
    }
}
