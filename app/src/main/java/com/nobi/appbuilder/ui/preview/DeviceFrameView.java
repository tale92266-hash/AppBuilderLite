package com.nobi.appbuilder.ui.preview;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.nobi.appbuilder.utils.DimensionUtils;

/**
 * The device frame: a fixed-size container that holds the rendered
 * root view. When the frame is visible it draws a subtle rounded
 * phone border; when hidden it is just the content, edge to edge.
 * Sizing is the only thing that ever changes here — the rendered
 * content is never re-measured, so zoom / orientation / device
 * changes are cheap and never touch the project.
 */
public class DeviceFrameView extends FrameLayout {

    public DeviceFrameView(Context context) {
        super(context);
    }

    public DeviceFrameView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /** Replaces the preview content with a freshly rendered root. */
    public void setContent(View content) {
        removeAllViews();
        if (content != null) {
            addView(content, new LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    /** Exact pixel size + optional decorative border. */
    public void applySize(int widthPx, int heightPx, boolean frameVisible) {
        ViewGroup.LayoutParams params = getLayoutParams();
        if (params == null) {
            params = new LayoutParams(widthPx, heightPx);
        }
        params.width = widthPx;
        params.height = heightPx;
        setLayoutParams(params);
        if (frameVisible) {
            GradientDrawable border = new GradientDrawable();
            border.setColor(Color.TRANSPARENT);
            border.setStroke(DimensionUtils.dpToPx(getContext(), 2),
                    Color.parseColor("#33000000"));
            border.setCornerRadius(DimensionUtils.dpToPx(getContext(), 24));
            setBackground(border);
        } else {
            setBackground(null);
        }
    }
}
