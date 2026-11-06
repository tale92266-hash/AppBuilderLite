package com.nobi.appbuilder.ui.library;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.library.ComponentLibraryItem;
import com.nobi.appbuilder.library.ComponentPreviewManager;

/** Details dialog for one library item, with a lightweight preview. */
public class ComponentDetailsController {

    public interface DetailsListener {
        void onAdd(ComponentLibraryItem item);

        void onToggleFavorite(ComponentLibraryItem item);
    }

    private final Context context;
    private final DetailsListener listener;
    private final ComponentPreviewManager previewManager =
            new ComponentPreviewManager();

    public ComponentDetailsController(Context context,
                                      DetailsListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(ComponentLibraryItem item) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_component_details, null);
        TextView name = view.findViewById(R.id.text_component_details_name);
        TextView description =
                view.findViewById(R.id.text_component_details_description);
        LinearLayout previewContainer =
                view.findViewById(R.id.layout_component_details_preview);
        Button add = view.findViewById(R.id.btn_component_details_add);
        Button favorite =
                view.findViewById(R.id.btn_component_details_favorite);
        Button close = view.findViewById(R.id.btn_component_details_close);
        name.setText(item.getDisplayName());
        description.setText(previewManager.previewSummary(item)
                + "\n\n" + item.getDescription());
        View preview = previewManager.buildPreview(context, item);
        previewContainer.addView(preview);
        add.setEnabled(item.isSupported());
        favorite.setText(item.isFavorite() ? "★ Favorite" : "☆ Favorite");
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        add.setOnClickListener(v -> {
            listener.onAdd(item);
            dialog.dismiss();
        });
        favorite.setOnClickListener(v -> {
            listener.onToggleFavorite(item);
            favorite.setText(!item.isFavorite() ? "★ Favorite" : "☆ Favorite");
        });
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
