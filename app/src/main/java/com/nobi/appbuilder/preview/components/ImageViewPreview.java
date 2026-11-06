package com.nobi.appbuilder.preview.components;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;

import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.preview.PreviewContext;
import com.nobi.appbuilder.preview.theme.PreviewColorResolver;
import com.nobi.appbuilder.preview.theme.PreviewStyleResolver;
import com.nobi.appbuilder.utils.DimensionUtils;

import java.io.File;

/**
 * Real android.widget.ImageView. Sources, in order: a drawable
 * resource by name, a project asset (by id or name), an absolute
 * file path. Large files are decoded on the background executor with
 * downsampling and set on the main thread. A missing image gets a
 * clear grey placeholder instead of a crash or a blank hole.
 */
public final class ImageViewPreview {

    private static final int MAX_BITMAP_EDGE_PX = 1024;

    private ImageViewPreview() {
    }

    public static void apply(Context context, ComponentModel model,
                             ImageView view, PreviewContext previewContext) {
        view.setScaleType(PreviewStyleResolver.scaleTypeFor(
                model.getProperty("scaleType")));
        PreviewStyleResolver.applyBackground(view, model,
                previewContext.getThemeManager());
        float radius = PreviewStyleResolver.parseFloat(model, "cornerRadius", 0f);
        if (radius > 0) {
            final float radiusPx = DimensionUtils.dpToPx(context, radius);
            view.setClipToOutline(true);
            view.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View v, Outline outline) {
                    outline.setRoundRect(0, 0, v.getWidth(), v.getHeight(), radiusPx);
                }
            });
        }
        String src = model.getProperty("src");
        if (src == null || src.isEmpty()) {
            setPlaceholder(context, view);
            return;
        }
        int drawableId = context.getResources().getIdentifier(
                src, "drawable", context.getPackageName());
        if (drawableId != 0) {
            view.setImageResource(drawableId);
            return;
        }
        String path = resolveAssetPath(model, src, previewContext);
        if (path == null) {
            setPlaceholder(context, view);
            return;
        }
        loadBitmapAsync(path, view, previewContext);
    }

    private static String resolveAssetPath(ComponentModel model, String src,
                                           PreviewContext previewContext) {
        AssetRepository assets = previewContext.getAssetRepository();
        String projectId = previewContext.getSession().getProjectId();
        if (assets != null && projectId != null) {
            for (AssetRepository.Asset asset : assets.getByProject(projectId)) {
                if (src.equals(asset.id) || src.equals(asset.name)) {
                    return asset.path;
                }
            }
        }
        if (src.startsWith("/") && new File(src).exists()) {
            return src;
        }
        return null;
    }

    private static void loadBitmapAsync(final String path, final ImageView view,
                                        final PreviewContext previewContext) {
        previewContext.getBackgroundExecutor().execute(() -> {
            Bitmap bitmap = decodeSampled(path);
            previewContext.getMainHandler().post(() -> {
                if (bitmap != null) {
                    view.setImageBitmap(bitmap);
                } else {
                    setPlaceholder(view.getContext(), view);
                }
            });
        });
    }

    private static Bitmap decodeSampled(String path) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, bounds);
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                return null;
            }
            int sample = 1;
            while (bounds.outWidth / sample > MAX_BITMAP_EDGE_PX
                    || bounds.outHeight / sample > MAX_BITMAP_EDGE_PX) {
                sample *= 2;
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sample;
            return BitmapFactory.decodeFile(path, options);
        } catch (Exception e) {
            return null;
        }
    }

    private static void setPlaceholder(Context context, ImageView view) {
        view.setImageResource(android.R.drawable.ic_menu_gallery);
        view.setBackgroundColor(PreviewColorResolver.parse("#E8EEF7", 0xFFE8EEF7));
        view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        view.setContentDescription("Image not found");
    }
}
