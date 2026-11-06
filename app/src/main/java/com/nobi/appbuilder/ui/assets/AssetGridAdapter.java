package com.nobi.appbuilder.ui.assets;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetPreviewManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** Asset grid: downsampled thumbnail + name + detail. */
public class AssetGridAdapter
        extends RecyclerView.Adapter<AssetGridAdapter.Holder> {

    public interface OnAssetActionListener {
        void onOpen(AssetModel asset);

        void onDelete(AssetModel asset);
    }

    private final List<AssetModel> assets = new ArrayList<>();
    private final OnAssetActionListener listener;

    public AssetGridAdapter(OnAssetActionListener listener) {
        this.listener = listener;
    }

    public void setAssets(List<AssetModel> assets) {
        this.assets.clear();
        this.assets.addAll(assets);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        AssetModel asset = assets.get(position);
        holder.name.setText(asset.getName());
        holder.detail.setText(asset.getType().getDisplayName() + " · "
                + AssetPreviewManager.formatSize(asset.getSizeBytes()));
        Bitmap thumb = AssetPreviewManager.previewBitmap(
                new File(asset.getPath()), 256);
        if (thumb != null) {
            holder.thumb.setImageBitmap(thumb);
        } else {
            holder.thumb.setImageResource(android.R.drawable.ic_menu_gallery);
        }
        holder.itemView.setOnClickListener(v -> listener.onOpen(asset));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onDelete(asset);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return assets.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final ImageView thumb;
        final TextView name;
        final TextView detail;

        Holder(View itemView) {
            super(itemView);
            thumb = itemView.findViewById(R.id.image_asset_thumb);
            name = itemView.findViewById(R.id.text_asset_name);
            detail = itemView.findViewById(R.id.text_asset_detail);
        }
    }
}
