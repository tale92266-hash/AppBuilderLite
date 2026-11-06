package com.nobi.appbuilder.assets;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Name/type search and category filtering over assets. */
public final class AssetSearchManager {

    private AssetSearchManager() {
    }

    public static List<AssetModel> search(List<AssetModel> assets,
                                          String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(assets);
        }
        String needle = query.trim().toLowerCase(Locale.US);
        List<AssetModel> out = new ArrayList<>();
        for (AssetModel asset : assets) {
            String haystack = (asset.getName() + " "
                    + asset.getResourceName() + " "
                    + asset.getType().getDisplayName())
                    .toLowerCase(Locale.US);
            if (haystack.contains(needle)) {
                out.add(asset);
            }
        }
        return out;
    }

    public static List<AssetModel> filterByCategory(
            List<AssetModel> assets, AssetCategory category) {
        if (category == null || category == AssetCategory.ALL) {
            return new ArrayList<>(assets);
        }
        List<AssetModel> out = new ArrayList<>();
        for (AssetModel asset : assets) {
            if (asset.getCategory() == category) {
                out.add(asset);
            }
        }
        return out;
    }

    public static List<AssetModel> filterByFolder(
            List<AssetModel> assets, String folderId) {
        List<AssetModel> out = new ArrayList<>();
        for (AssetModel asset : assets) {
            if (folderId == null) {
                if (asset.getFolderId() == null) {
                    out.add(asset);
                }
            } else if (folderId.equals(asset.getFolderId())) {
                out.add(asset);
            }
        }
        return out;
    }
}
