package com.nobi.appbuilder.ui.assets;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetMetadata;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetPreviewManager;

import java.util.List;

/** Details dialog for one asset: preview, metadata, usages, rename, delete. */
public class AssetDetailsController {

    public interface DetailsListener {
        void onRename(AssetModel asset, String newResourceName);

        void onDelete(AssetModel asset);

        void onEditImage(AssetModel asset);
    }

    private final Context context;
    private final DetailsListener listener;

    public AssetDetailsController(Context context, DetailsListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(AssetModel asset, AssetMetadata metadata,
                     List<String> usages) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.dialog_asset_details, null);
        TextView name = view.findViewById(R.id.text_asset_details_name);
        TextView info = view.findViewById(R.id.text_asset_details_info);
        TextView usage = view.findViewById(R.id.text_asset_details_usage);
        LinearLayout previewContainer =
                view.findViewById(R.id.layout_asset_details_preview);
        EditText renameInput =
                view.findViewById(R.id.input_asset_details_name);
        Button rename = view.findViewById(R.id.btn_asset_details_rename);
        Button delete = view.findViewById(R.id.btn_asset_details_delete);
        Button edit = view.findViewById(R.id.btn_asset_details_edit);
        Button close = view.findViewById(R.id.btn_asset_details_close);
        name.setText(asset.getName());
        info.setText(AssetPreviewManager.previewDescription(asset, metadata)
                + "\nResource: " + asset.getResourceName());
        if (usages == null || usages.isEmpty()) {
            usage.setText(R.string.asset_unused);
        } else {
            usage.setText(context.getString(R.string.asset_used_by,
                    String.join("\n", usages)));
        }
        View preview = LayoutInflater.from(context)
                .inflate(R.layout.layout_asset_preview, previewContainer, false);
        new AssetPreviewController(preview).show(asset, metadata);
        previewContainer.addView(preview);
        renameInput.setText(asset.getResourceName());
        edit.setVisibility(asset.getType()
                == com.nobi.appbuilder.assets.AssetType.IMAGE
                ? View.VISIBLE : View.GONE);
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .create();
        rename.setOnClickListener(v -> {
            listener.onRename(asset,
                    renameInput.getText().toString().trim());
            dialog.dismiss();
        });
        delete.setOnClickListener(v -> {
            listener.onDelete(asset);
            dialog.dismiss();
        });
        edit.setOnClickListener(v -> {
            listener.onEditImage(asset);
            dialog.dismiss();
        });
        close.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
