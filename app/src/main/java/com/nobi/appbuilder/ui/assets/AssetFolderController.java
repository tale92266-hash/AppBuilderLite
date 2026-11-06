package com.nobi.appbuilder.ui.assets;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;

import java.util.List;

/** Binds the horizontal folder list (All + folders). */
public class AssetFolderController {

    public interface FolderListener {
        void onFolderSelected(String folderId, String folderName);
    }

    private final AssetFolderAdapter adapter;

    public AssetFolderController(View root, FolderListener listener) {
        adapter = new AssetFolderAdapter((folderId, folderName) ->
                listener.onFolderSelected(folderId, folderName));
        RecyclerView list = root.findViewById(R.id.recycler_asset_folders);
        list.setLayoutManager(new LinearLayoutManager(
                root.getContext(), LinearLayoutManager.HORIZONTAL, false));
        list.setAdapter(adapter);
    }

    public void setFolders(List<String[]> folders) {
        adapter.setFolders(folders, null);
    }

    public void setSelected(String folderId) {
        adapter.setSelected(folderId);
    }
}
