package com.nobi.appbuilder.ui.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;

/**
 * The screen manager dialog (Phase 5): lists the project's screens
 * with type and launcher marker; tap a row to open it, the overflow
 * menu duplicates / renames / deletes / sets the starting screen.
 */
public class ScreenManagerDialog {

    public interface ScreenDialogListener {
        void onScreenSelected(ScreenModel screen);

        void onAddScreen();

        void onDuplicateScreen(ScreenModel screen);

        void onRenameScreen(ScreenModel screen);

        void onDeleteScreen(ScreenModel screen);

        void onSetLauncherScreen(ScreenModel screen);
    }

    private final Context context;
    private final ScreenDialogListener listener;
    private final List<ScreenModel> screens = new ArrayList<>();
    private final ScreenAdapter adapter = new ScreenAdapter();

    public ScreenManagerDialog(Context context, ScreenDialogListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void show(List<ScreenModel> screens, String currentScreenId) {
        this.screens.clear();
        this.screens.addAll(screens);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_screens, null);
        RecyclerView list = view.findViewById(R.id.list_screens);
        list.setLayoutManager(new LinearLayoutManager(context));
        list.setAdapter(adapter);
        view.findViewById(R.id.button_add_screen)
                .setOnClickListener(v -> listener.onAddScreen());
        adapter.setCurrentScreenId(currentScreenId);
        new AlertDialog.Builder(context)
                .setView(view)
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    public void update(List<ScreenModel> screens, String currentScreenId) {
        this.screens.clear();
        this.screens.addAll(screens);
        adapter.setCurrentScreenId(currentScreenId);
        adapter.notifyDataSetChanged();
    }

    private class ScreenAdapter extends RecyclerView.Adapter<ScreenAdapter.Holder> {

        private String currentScreenId;

        void setCurrentScreenId(String currentScreenId) {
            this.currentScreenId = currentScreenId;
        }

        @NonNull
        @Override
        public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_screen, parent, false);
            return new Holder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull Holder holder, int position) {
            ScreenModel screen = screens.get(position);
            holder.name.setText(screen.getName());
            holder.type.setText(typeLabel(screen.getScreenType()));
            holder.launcher.setVisibility(screen.isLauncher()
                    ? View.VISIBLE : View.GONE);
            holder.itemView.setAlpha(screen.getId().equals(currentScreenId)
                    ? 1.0f : 0.75f);
            holder.itemView.setOnClickListener(v -> listener.onScreenSelected(screen));
            holder.more.setOnClickListener(v -> showScreenMenu(holder.more, screen));
        }

        private String typeLabel(String type) {
            switch (type) {
                case ScreenModel.TYPE_DIALOG:
                    return context.getString(R.string.screens_type_dialog);
                case ScreenModel.TYPE_BOTTOM_SHEET:
                    return context.getString(R.string.screens_type_bottom_sheet);
                case ScreenModel.TYPE_CUSTOM:
                    return context.getString(R.string.screens_type_custom);
                case ScreenModel.TYPE_ACTIVITY:
                default:
                    return context.getString(R.string.screens_type_activity);
            }
        }

        private void showScreenMenu(View anchor, ScreenModel screen) {
            PopupMenu menu = new PopupMenu(context, anchor);
            menu.getMenu().add(0, 1, 0, R.string.action_duplicate);
            menu.getMenu().add(0, 2, 0, R.string.action_rename);
            menu.getMenu().add(0, 3, 0, R.string.action_delete);
            menu.getMenu().add(0, 4, 0, R.string.screens_set_launcher);
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case 1:
                        listener.onDuplicateScreen(screen);
                        return true;
                    case 2:
                        listener.onRenameScreen(screen);
                        return true;
                    case 3:
                        listener.onDeleteScreen(screen);
                        return true;
                    case 4:
                        listener.onSetLauncherScreen(screen);
                        return true;
                    default:
                        return false;
                }
            });
            menu.show();
        }

        @Override
        public int getItemCount() {
            return screens.size();
        }

        class Holder extends RecyclerView.ViewHolder {
            final TextView name;
            final TextView type;
            final TextView launcher;
            final ImageButton more;

            Holder(View itemView) {
                super(itemView);
                name = itemView.findViewById(R.id.text_screen_name);
                type = itemView.findViewById(R.id.text_screen_type);
                launcher = itemView.findViewById(R.id.text_screen_launcher);
                more = itemView.findViewById(R.id.button_screen_more);
            }
        }
    }
}
