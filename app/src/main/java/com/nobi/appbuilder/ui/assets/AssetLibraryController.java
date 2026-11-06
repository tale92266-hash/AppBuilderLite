package com.nobi.appbuilder.ui.assets;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.assets.AssetCategory;
import com.nobi.appbuilder.assets.AssetModel;
import com.nobi.appbuilder.assets.AssetSearchManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Binds the asset manager screen: search, folder filter, grid,
 * empty state. Filtering is in-memory and instant.
 */
public class AssetLibraryController {

    public interface LibraryListener {
        void onOpen(AssetModel asset);

        void onDelete(AssetModel asset);
    }

    private final View root;
    private final AssetGridAdapter gridAdapter;
    private final List<AssetModel> allAssets = new ArrayList<>();
    private String query = "";
    private String folderId;
    private AssetCategory category = AssetCategory.ALL;

    public AssetLibraryController(View root, LibraryListener listener) {
        this.root = root;
        gridAdapter = new AssetGridAdapter(
                new AssetGridAdapter.OnAssetActionListener() {
                    @Override
                    public void onOpen(AssetModel asset) {
                        listener.onOpen(asset);
                    }

                    @Override
                    public void onDelete(AssetModel asset) {
                        listener.onDelete(asset);
                    }
                });
        RecyclerView grid = root.findViewById(R.id.recycler_assets);
        grid.setLayoutManager(new GridLayoutManager(root.getContext(), 2));
        grid.setAdapter(gridAdapter);
        EditText search = root.findViewById(R.id.input_asset_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                query = s.toString();
                refresh();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    public void setAssets(List<AssetModel> assets) {
        allAssets.clear();
        allAssets.addAll(assets);
        refresh();
    }

    public void setFolder(String folderId) {
        this.folderId = folderId;
        refresh();
    }

    public void setCategory(AssetCategory category) {
        this.category = category == null ? AssetCategory.ALL : category;
        refresh();
    }

    public void refresh() {
        List<AssetModel> filtered =
                AssetSearchManager.search(allAssets, query);
        filtered = AssetSearchManager.filterByCategory(filtered, category);
        if (folderId != null) {
            filtered = AssetSearchManager.filterByFolder(filtered, folderId);
        }
        gridAdapter.setAssets(filtered);
        TextView empty = root.findViewById(R.id.text_asset_empty);
        empty.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
