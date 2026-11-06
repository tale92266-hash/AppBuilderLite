package com.nobi.appbuilder.ui.dashboard;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.utils.FileUtils;
import com.nobi.appbuilder.utils.IDGenerator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Phase 1 dashboard: branding, create-project dialog, recent projects,
 * search, rename / duplicate / delete, JSON import (Storage Access
 * Framework) and settings (dark theme, autosave). All database work runs
 * on a background thread; the UI only ever touches the main thread.
 *
 * Tapping a project opens its details. The visual editor that opens a
 * project for editing arrives in Phase 3 and will be wired to the card
 * tap then; until that phase the details dialog is the honest entry point.
 */
public class DashboardActivity extends AppCompatActivity {

    private static final int MENU_OPEN = 1;
    private static final int MENU_RENAME = 2;
    private static final int MENU_DUPLICATE = 3;
    private static final int MENU_DELETE = 4;

    private DatabaseHelper dbHelper;
    private ProjectRepository projectRepository;
    private ScreenRepository screenRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    private RecyclerView recyclerProjects;
    private View emptyState;
    private TextView emptyTitle;
    private TextView emptyBody;
    private ProjectAdapter adapter;

    private final List<ProjectModel> allProjects = new ArrayList<>();
    private String currentQuery = "";

    private final ActivityResultLauncher<String> importLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(),
                    uri -> {
                        if (uri != null) {
                            importProject(uri);
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        dbHelper = new DatabaseHelper(this);
        projectRepository = new ProjectRepository(dbHelper);
        screenRepository = new ScreenRepository(dbHelper);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        recyclerProjects = findViewById(R.id.recycler_projects);
        recyclerProjects.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ProjectAdapter();
        recyclerProjects.setAdapter(adapter);

        emptyState = findViewById(R.id.empty_state);
        emptyTitle = emptyState.findViewById(R.id.empty_title);
        emptyBody = emptyState.findViewById(R.id.empty_body);

        FloatingActionButton fabCreate = findViewById(R.id.fab_create);
        fabCreate.setOnClickListener(v -> showCreateDialog());
        Button buttonEmptyCreate = findViewById(R.id.button_empty_create);
        buttonEmptyCreate.setOnClickListener(v -> showCreateDialog());

        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                currentQuery = newText == null ? "" : newText;
                applyFilter();
                return true;
            }
        });

        loadProjects();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_import) {
            importLauncher.launch("application/json");
            return true;
        } else if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
        dbHelper.close();
    }

    // ------------------------------------------------------------------
    // Loading and filtering
    // ------------------------------------------------------------------

    private void loadProjects() {
        executor.execute(() -> {
            try {
                List<ProjectModel> projects = projectRepository.getAll();
                for (ProjectModel project : projects) {
                    project.setScreenCount(projectRepository.countScreens(project.getId()));
                }
                mainHandler.post(() -> {
                    allProjects.clear();
                    allProjects.addAll(projects);
                    applyFilter();
                });
            } catch (Exception e) {
                mainHandler.post(() -> Toast.makeText(this,
                        R.string.msg_load_failed, Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void applyFilter() {
        List<ProjectModel> filtered = new ArrayList<>();
        String query = currentQuery.trim().toLowerCase(Locale.getDefault());
        for (ProjectModel project : allProjects) {
            if (query.isEmpty()
                    || project.getAppName().toLowerCase(Locale.getDefault()).contains(query)
                    || project.getPackageName().toLowerCase(Locale.getDefault()).contains(query)) {
                filtered.add(project);
            }
        }
        adapter.setProjects(filtered);
        updateEmptyState(filtered);
    }

    private void updateEmptyState(List<ProjectModel> filtered) {
        if (allProjects.isEmpty()) {
            emptyTitle.setText(R.string.no_projects_title);
            emptyBody.setText(R.string.no_projects_body);
            emptyState.setVisibility(View.VISIBLE);
            recyclerProjects.setVisibility(View.GONE);
        } else if (filtered.isEmpty()) {
            emptyTitle.setText(R.string.no_results_title);
            emptyBody.setText(R.string.no_results_body);
            emptyState.setVisibility(View.VISIBLE);
            recyclerProjects.setVisibility(View.GONE);
        } else {
            emptyState.setVisibility(View.GONE);
            recyclerProjects.setVisibility(View.VISIBLE);
        }
    }

    // ------------------------------------------------------------------
    // Create project
    // ------------------------------------------------------------------

    private void showCreateDialog() {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_create_project, null);
        TextInputLayout layoutAppName = view.findViewById(R.id.input_layout_app_name);
        TextInputLayout layoutPackage = view.findViewById(R.id.input_layout_package_name);
        TextInputLayout layoutScreen = view.findViewById(R.id.input_layout_screen_name);
        TextInputEditText editAppName = view.findViewById(R.id.edit_app_name);
        TextInputEditText editPackage = view.findViewById(R.id.edit_package_name);
        TextInputEditText editScreen = view.findViewById(R.id.edit_screen_name);
        Spinner spinnerMinSdk = view.findViewById(R.id.spinner_min_sdk);

        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(
                this, R.array.min_sdk_labels, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerMinSdk.setAdapter(spinnerAdapter);
        editScreen.setText(R.string.hint_initial_screen);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_create_title)
                .setView(view)
                .setPositiveButton(R.string.action_create, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String appName = textOf(editAppName).trim();
                String packageName = textOf(editPackage).trim();
                String screenName = textOf(editScreen).trim();
                int minSdk = getResources().getIntArray(R.array.min_sdk_values)
                        [spinnerMinSdk.getSelectedItemPosition()];
                boolean ok = true;
                if (!ProjectModel.isValidAppName(appName)) {
                    layoutAppName.setError(getString(R.string.error_app_name_empty));
                    ok = false;
                } else {
                    layoutAppName.setError(null);
                }
                if (!ProjectModel.isValidPackageName(packageName)) {
                    layoutPackage.setError(getString(R.string.error_package_invalid));
                    ok = false;
                } else {
                    layoutPackage.setError(null);
                }
                if (screenName.isEmpty()) {
                    layoutScreen.setError(getString(R.string.error_screen_empty));
                    ok = false;
                } else {
                    layoutScreen.setError(null);
                }
                if (!ok) {
                    return;
                }
                dialog.dismiss();
                createProject(appName, packageName, minSdk, screenName);
            });
        });
        dialog.show();
    }

    private void createProject(String appName, String packageName,
                               int minSdk, String screenName) {
        executor.execute(() -> {
            ProjectModel project = new ProjectModel(
                    IDGenerator.newProjectId(), appName, packageName, minSdk);
            ScreenModel screen = new ScreenModel(
                    IDGenerator.newScreenId(), project.getId(), screenName, true, 0);
            ComponentModel root = new ComponentModel(
                    IDGenerator.newComponentId(), "LinearLayout");
            root.setProperty("orientation", "vertical");
            screen.setRootComponent(root);
            boolean ok = projectRepository.insert(project, screen);
            mainHandler.post(() -> {
                if (ok) {
                    Toast.makeText(this, R.string.msg_project_created,
                            Toast.LENGTH_SHORT).show();
                    loadProjects();
                } else {
                    Toast.makeText(this, R.string.msg_load_failed,
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    // ------------------------------------------------------------------
    // Project details, rename, duplicate, delete
    // ------------------------------------------------------------------

    private void showDetailsDialog(ProjectModel project) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_project_info, null);
        ((TextView) view.findViewById(R.id.info_name)).setText(project.getAppName());
        ((TextView) view.findViewById(R.id.info_package)).setText(project.getPackageName());
        ((TextView) view.findViewById(R.id.info_screens)).setText(
                getString(R.string.label_screens) + ": " + project.getScreenCount());
        ((TextView) view.findViewById(R.id.info_min_sdk)).setText(
                getString(R.string.label_min_sdk_value) + ": " + project.getMinSdk());
        ((TextView) view.findViewById(R.id.info_created)).setText(
                getString(R.string.label_created) + ": " + formatDate(project.getCreatedAt()));
        ((TextView) view.findViewById(R.id.info_updated)).setText(
                getString(R.string.label_updated) + ": " + formatDate(project.getUpdatedAt()));
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_details_title)
                .setView(view)
                .setPositiveButton(R.string.action_rename,
                        (d, w) -> showRenameDialog(project))
                .setNegativeButton(R.string.action_delete,
                        (d, w) -> confirmDelete(project))
                .setNeutralButton(R.string.action_cancel, null)
                .show();
    }

    private void showRenameDialog(ProjectModel project) {
        EditText input = new EditText(this);
        input.setText(project.getAppName());
        input.setSingleLine(true);
        input.setPadding(48, 32, 48, 16);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_rename_title)
                .setView(input)
                .setPositiveButton(R.string.action_rename, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String newName = input.getText().toString().trim();
                if (!ProjectModel.isValidAppName(newName)) {
                    input.setError(getString(R.string.error_app_name_empty));
                    return;
                }
                dialog.dismiss();
                executor.execute(() -> {
                    project.setAppName(newName);
                    boolean ok = projectRepository.update(project);
                    mainHandler.post(() -> {
                        if (ok) {
                            Toast.makeText(this, R.string.msg_project_renamed,
                                    Toast.LENGTH_SHORT).show();
                            loadProjects();
                        }
                    });
                });
            });
        });
        dialog.show();
    }

    private void confirmDelete(ProjectModel project) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_delete_title)
                .setMessage(R.string.dialog_delete_body)
                .setPositiveButton(R.string.action_delete, (d, w) -> {
                    executor.execute(() -> {
                        projectRepository.delete(project.getId());
                        FileUtils.deleteRecursive(
                                FileUtils.getProjectDir(this, project.getId()));
                        mainHandler.post(() -> {
                            Toast.makeText(this, R.string.msg_project_deleted,
                                    Toast.LENGTH_SHORT).show();
                            loadProjects();
                        });
                    });
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void duplicateProject(ProjectModel project) {
        executor.execute(() -> {
            ProjectModel copy = projectRepository.duplicate(project.getId());
            mainHandler.post(() -> {
                if (copy != null) {
                    Toast.makeText(this, R.string.msg_project_duplicated,
                            Toast.LENGTH_SHORT).show();
                    loadProjects();
                }
            });
        });
    }

    // ------------------------------------------------------------------
    // Import (SAF JSON) and settings
    // ------------------------------------------------------------------

    private void importProject(Uri uri) {
        executor.execute(() -> {
            ProjectModel imported = null;
            try {
                String json = FileUtils.readStreamToString(
                        getContentResolver().openInputStream(uri));
                imported = ProjectModel.fromJson(json);
            } catch (Exception e) {
                imported = null;
            }
            final ProjectModel project = imported;
            mainHandler.post(() -> {
                if (project == null) {
                    Toast.makeText(this, R.string.msg_import_failed,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                saveImportedProject(project);
            });
        });
    }

    private void saveImportedProject(ProjectModel project) {
        executor.execute(() -> {
            if (projectRepository.getById(project.getId()) != null) {
                project.setId(IDGenerator.newProjectId());
            }
            List<ScreenModel> screens = new ArrayList<>(project.getScreens());
            ScreenModel first = screens.isEmpty() ? null : screens.remove(0);
            if (first != null) {
                first.setId(IDGenerator.newScreenId());
                first.setProjectId(project.getId());
            }
            boolean ok = projectRepository.insert(project, first);
            if (ok) {
                for (ScreenModel screen : screens) {
                    screen.setId(IDGenerator.newScreenId());
                    screen.setProjectId(project.getId());
                    screenRepository.insert(screen);
                }
            }
            mainHandler.post(() -> {
                if (ok) {
                    Toast.makeText(this, R.string.msg_project_imported,
                            Toast.LENGTH_SHORT).show();
                    loadProjects();
                } else {
                    Toast.makeText(this, R.string.msg_import_failed,
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void showSettingsDialog() {
        SharedPreferences prefs = getSharedPreferences("appbuilder_prefs", MODE_PRIVATE);
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_settings, null);
        CheckBox checkDark = view.findViewById(R.id.check_dark_theme);
        CheckBox checkAutosave = view.findViewById(R.id.check_autosave);
        checkDark.setChecked(prefs.getBoolean("dark_theme", false));
        checkAutosave.setChecked(prefs.getBoolean("autosave", true));
        new AlertDialog.Builder(this)
                .setTitle(R.string.settings)
                .setView(view)
                .setPositiveButton(android.R.string.ok, (d, w) -> {
                    boolean dark = checkDark.isChecked();
                    boolean wasDark = prefs.getBoolean("dark_theme", false);
                    prefs.edit()
                            .putBoolean("dark_theme", dark)
                            .putBoolean("autosave", checkAutosave.isChecked())
                            .apply();
                    if (dark != wasDark) {
                        AppCompatDelegate.setDefaultNightMode(dark
                                ? AppCompatDelegate.MODE_NIGHT_YES
                                : AppCompatDelegate.MODE_NIGHT_NO);
                        recreate();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private String textOf(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString();
    }

    private String formatDate(long timeMillis) {
        return new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                .format(new Date(timeMillis));
    }

    // ------------------------------------------------------------------
    // RecyclerView adapter
    // ------------------------------------------------------------------

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ViewHolder> {

        private final List<ProjectModel> projects = new ArrayList<>();

        void setProjects(List<ProjectModel> newProjects) {
            projects.clear();
            projects.addAll(newProjects);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_project, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ProjectModel project = projects.get(position);
            holder.textThumb.setText(project.getInitials());
            holder.textName.setText(project.getAppName());
            holder.textPackage.setText(project.getPackageName());
            holder.textMeta.setText(project.getScreenCount()
                    + " " + getString(R.string.label_screens).toLowerCase(Locale.getDefault())
                    + "  •  Min SDK " + project.getMinSdk()
                    + "  •  " + formatDate(project.getUpdatedAt()));
            holder.itemView.setOnClickListener(v -> showDetailsDialog(project));
            holder.buttonMore.setOnClickListener(v -> showProjectMenu(v, project));
        }

        @Override
        public int getItemCount() {
            return projects.size();
        }

        private void showProjectMenu(View anchor, ProjectModel project) {
            PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
            menu.getMenu().add(0, MENU_OPEN, 0, R.string.action_open);
            menu.getMenu().add(0, MENU_RENAME, 0, R.string.action_rename);
            menu.getMenu().add(0, MENU_DUPLICATE, 0, R.string.action_duplicate);
            menu.getMenu().add(0, MENU_DELETE, 0, R.string.action_delete);
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == MENU_OPEN) {
                    showDetailsDialog(project);
                } else if (id == MENU_RENAME) {
                    showRenameDialog(project);
                } else if (id == MENU_DUPLICATE) {
                    duplicateProject(project);
                } else if (id == MENU_DELETE) {
                    confirmDelete(project);
                }
                return true;
            });
            menu.show();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            final TextView textThumb;
            final TextView textName;
            final TextView textPackage;
            final TextView textMeta;
            final ImageButton buttonMore;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textThumb = itemView.findViewById(R.id.text_thumb);
                textName = itemView.findViewById(R.id.text_name);
                textPackage = itemView.findViewById(R.id.text_package);
                textMeta = itemView.findViewById(R.id.text_meta);
                buttonMore = itemView.findViewById(R.id.button_more);
            }
        }
    }
}
