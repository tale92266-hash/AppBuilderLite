package com.nobi.appbuilder.ui.sync;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.sync.history.SynchronizationSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Binds layout_sync_history.xml to the sync snapshot history. */
public class SyncHistoryController {

    private final TextView emptyView;
    private final HistoryAdapter adapter = new HistoryAdapter();

    public SyncHistoryController(View root) {
        emptyView = root.findViewById(R.id.text_sync_history_empty);
        RecyclerView recycler =
                root.findViewById(R.id.recycler_sync_history);
        recycler.setLayoutManager(new LinearLayoutManager(
                root.getContext()));
        recycler.setAdapter(adapter);
    }

    public void setSnapshots(List<SynchronizationSnapshot> snapshots) {
        adapter.setItems(snapshots);
        emptyView.setVisibility(
                snapshots.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private static class HistoryAdapter
            extends RecyclerView.Adapter<HistoryAdapter.Holder> {

        private List<SynchronizationSnapshot> items = new ArrayList<>();

        void setItems(List<SynchronizationSnapshot> items) {
            this.items = items == null ? new ArrayList<>() : items;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent,
                                         int viewType) {
            return new Holder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_sync_change, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            SynchronizationSnapshot snapshot = items.get(position);
            holder.title.setText(snapshot.getDescription());
            holder.detail.setText(new SimpleDateFormat(
                    "dd MMM yyyy, HH:mm", Locale.getDefault())
                    .format(new Date(snapshot.getTimestamp()))
                    + " · " + snapshot.getOrigin());
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class Holder extends RecyclerView.ViewHolder {
            final TextView title;
            final TextView detail;

            Holder(View itemView) {
                super(itemView);
                title = itemView.findViewById(R.id.text_sync_change_title);
                detail =
                        itemView.findViewById(R.id.text_sync_change_detail);
            }
        }
    }
}
