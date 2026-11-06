package com.nobi.appbuilder.library;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Combines category + favorites + recent + supported filters and
 * sorts alphabetically or by recent use.
 */
public class ComponentFilterManager {

    public static final int SORT_NAME = 0;
    public static final int SORT_RECENT = 1;

    private ComponentCategory category = ComponentCategory.ALL;
    private boolean favoritesOnly;
    private boolean recentOnly;
    private boolean supportedOnly = true;
    private int sortMode = SORT_NAME;

    public ComponentCategory getCategory() {
        return category;
    }

    public void setCategory(ComponentCategory category) {
        this.category = category == null ? ComponentCategory.ALL : category;
    }

    public boolean isFavoritesOnly() {
        return favoritesOnly;
    }

    public void setFavoritesOnly(boolean favoritesOnly) {
        this.favoritesOnly = favoritesOnly;
    }

    public boolean isRecentOnly() {
        return recentOnly;
    }

    public void setRecentOnly(boolean recentOnly) {
        this.recentOnly = recentOnly;
    }

    public boolean isSupportedOnly() {
        return supportedOnly;
    }

    public void setSupportedOnly(boolean supportedOnly) {
        this.supportedOnly = supportedOnly;
    }

    public int getSortMode() {
        return sortMode;
    }

    public void setSortMode(int sortMode) {
        this.sortMode = sortMode;
    }

    public List<ComponentLibraryItem> apply(List<ComponentLibraryItem> items) {
        List<ComponentLibraryItem> out = new ArrayList<>();
        for (ComponentLibraryItem item : items) {
            if (category != ComponentCategory.ALL
                    && item.getCategory() != category) {
                continue;
            }
            if (favoritesOnly && !item.isFavorite()) {
                continue;
            }
            if (recentOnly && item.getLastUsedAt() <= 0) {
                continue;
            }
            if (supportedOnly && !item.isSupported()) {
                continue;
            }
            out.add(item);
        }
        if (sortMode == SORT_RECENT) {
            Collections.sort(out, new Comparator<ComponentLibraryItem>() {
                @Override
                public int compare(ComponentLibraryItem a,
                                   ComponentLibraryItem b) {
                    return Long.compare(b.getLastUsedAt(), a.getLastUsedAt());
                }
            });
        } else {
            Collections.sort(out, new Comparator<ComponentLibraryItem>() {
                @Override
                public int compare(ComponentLibraryItem a,
                                   ComponentLibraryItem b) {
                    return a.getDisplayName().compareToIgnoreCase(
                            b.getDisplayName());
                }
            });
        }
        return out;
    }
}
