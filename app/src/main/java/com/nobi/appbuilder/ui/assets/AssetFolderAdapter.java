package com.nobi.appbuilder.ui.assets;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;

import java.util.ArrayList;
import java.util.List;

/** Folder chips: name + asset count. */
public class AssetFolderAdapter
        extends RecyclerView.Adapter<AssetFolderAdapter.Holder> {

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderId, String folderName);
    }

    private final List<String[]> folders = new ArrayList<>();
    private final OnFolderSelectedListener listener;
    private String selectedId;

    public AssetFolderAdapter(OnFolderSelectedListener listener) {
        this.listener = listener;
    }

    public void setFolders(List<String[]> folders, int[] counts) {
        this.folders.clear();
        this.folders.addAll(folders);
        notifyDataSetChanged();
    }

    public void setSelected(String folderId) {
        selectedId = folderId;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asset_folder, parent, false);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String[] folder = folders.get(position);
        holder.name.setText(folder[1]);
        holder.name.setSelected(folder[0] != null
                && folder[0].equals(selectedId));
        holder.itemView.setOnClickListener(v ->
                listener.onFolderSelected(folder[0], folder[1]));
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class Holder extends RecyclerView.ViewHolder {
        final TextView name;

        Holder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.text_asset_folder_name);
        }
    }
}
