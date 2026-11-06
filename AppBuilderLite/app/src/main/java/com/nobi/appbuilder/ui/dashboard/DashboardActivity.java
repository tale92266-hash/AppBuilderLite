package com.nobi.appbuilder.ui.dashboard;

import android.content.Intent;
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
import com.nobi.appbuilder.autosave.ProjectBackupManager;
import com.nobi.appbuilder.autosave.RecoveryManager;
import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.project.ProjectExportManager;
import com.nobi.appbuilder.project.ProjectImportManager;
import com.nobi.appbuilder.project.ProjectManager;
import com.nobi.appbuilder.project.ProjectSettingsManager;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.ui.editor.EditorActivity;
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
    private static final int MENU_EXPORT = 5;
    private static final int MENU_SETTINGS = 6;

    private DatabaseHelper dbHelper;
    private ProjectRepository projectRepository;
    private ScreenRepository screenRepository;
    private ExecutorService executor;
    private Handler mainHandler;

    /** Phase 5 project engines. */
    private ProjectManager projectManager;
    private ProjectImportManager importManager;
    private ProjectExportManager exportManager;
    private ProjectSettingsManager settingsManager;
    private ProjectModel pendingExportProject;
    private int sortMode = ProjectManager.SORT_RECENT;

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

    private final ActivityResultLauncher<String> exportLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument(
                    "application/json"), uri -> {
                if (uri != null && pendingExportProject != null) {
                    writeExport(uri, pendingExportProject);
                    pendingExportProject = null;
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

        RecoveryManager recoveryManager = new RecoveryManager(dbHelper, executor);
        ProjectBackupManager backupManager = new ProjectBackupManager(dbHelper, executor);
        projectManager = new ProjectManager(projectRepository,
                recoveryManager, backupManager);
        importManager = new ProjectImportManager(new DocumentRepository(dbHelper));
        exportManager = new ProjectExportManager(new DocumentRepository(dbHelper));
        settingsManager = new ProjectSettingsManager(dbHelper, projectRepository);
        sortMode = getSharedPreferences("appbuilder_prefs", MODE_PRIVATE)
                .getInt("dashboard_sort", ProjectManager.SORT_RECENT);

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
        menu.findItem(R.id.action_sort_recent).setChecked(
                sortMode == ProjectManager.SORT_RECENT);
        menu.findItem(R.id.action_sort_name).setChecked(
                sortMode == ProjectManager.SORT_NAME);
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
        } else if (id == R.id.action_sort_recent) {
            setSortMode(ProjectManager.SORT_RECENT);
            return true;
        } else if (id == R.id.action_sort_name) {
            setSortMode(ProjectManager.SORT_NAME);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setSortMode(int mode) {
        sortMode = mode;
        getSharedPreferences("appbuilder_prefs", MODE_PRIVATE)
                .edit().putInt("dashboard_sort", mode).apply();
        invalidateOptionsMenu();
        loadProjects();
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
                List<ProjectModel> projects = projectManager.getProjects(sortMode);
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
            ComponentModel root = ComponentFactory.createRootContainer();
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
                .setPositiveButton(R.string.action_open_editor,
                        (d, w) -> openEditor(project))
                .setNegativeButton(R.string.action_delete,
                        (d, w) -> confirmDelete(project))
                .setNeutralButton(R.string.action_cancel, null)
                .show();
    }

    /** Opens the project in the Phase 3 visual editor (launcher screen). */
    private void openEditor(ProjectModel project) {
        executor.execute(() -> {
            IRDocument document = new DocumentRepository(dbHelper)
                    .loadDocument(project.getId());
            ScreenModel target = null;
            if (document != null) {
                for (ScreenModel screen : document.getScreens()) {
                    if (screen.isLauncher()) {
                        target = screen;
                        break;
                    }
                }
                if (target == null && !document.getScreens().isEmpty()) {
                    target = document.getScreens().get(0);
                }
            }
            final ScreenModel screen = target;
            mainHandler.post(() -> {
                if (screen == null) {
                    Toast.makeText(this, R.string.msg_editor_no_screen,
                            Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intent = new Intent(this, EditorActivity.class);
                intent.putExtra(EditorActivity.EXTRA_PROJECT_ID, project.getId());
                intent.putExtra(EditorActivity.EXTRA_SCREEN_ID, screen.getId());
                startActivity(intent);
            });
        });
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
                        projectManager.deleteProject(project.getId());
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
            ProjectModel copy = projectManager.duplicateProject(project.getId());
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
            String json = null;
            try {
                json = FileUtils.readStreamToString(
                        getContentResolver().openInputStream(uri));
            } catch (Exception ignored) {
            }
            ProjectImportManager.ImportResult result =
                    importManager.importFromJson(json);
            mainHandler.post(() -> {
                if (result.project != null) {
                    Toast.makeText(this, R.string.msg_project_imported,
                            Toast.LENGTH_SHORT).show();
                    loadProjects();
                } else {
                    Toast.makeText(this, result.error == null
                                    ? getString(R.string.msg_import_failed)
                                    : result.error,
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ------------------------------------------------------------------
    // Export and project settings (Phase 5)
    // ------------------------------------------------------------------

    private void exportProject(ProjectModel project) {
        pendingExportProject = project;
        exportLauncher.launch(
                ProjectExportManager.fileNameFor(project.getAppName()));
    }

    private void writeExport(Uri uri, ProjectModel project) {
        executor.execute(() -> {
            String json = exportManager.exportToJson(project.getId());
            boolean ok = false;
            if (json != null) {
                try (java.io.OutputStream out =
                             getContentResolver().openOutputStream(uri)) {
                    if (out != null) {
                        out.write(json.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8));
                        ok = true;
                    }
                } catch (Exception ignored) {
                }
            }
            boolean success = ok;
            mainHandler.post(() -> Toast.makeText(this,
                    success ? R.string.msg_exported : R.string.msg_export_failed,
                    Toast.LENGTH_SHORT).show());
        });
    }

    private void showProjectSettingsDialog(ProjectModel project) {
        executor.execute(() -> {
            List<ScreenModel> screens = screenRepository.getByProject(project.getId());
            String versionName = settingsManager.getSetting(project.getId(),
                    ProjectSettingsManager.KEY_VERSION_NAME, "1.0");
            String versionCode = settingsManager.getSetting(project.getId(),
                    ProjectSettingsManager.KEY_VERSION_CODE, "1");
            String targetSdk = settingsManager.getSetting(project.getId(),
                    ProjectSettingsManager.KEY_TARGET_SDK, "35");
            String orientation = settingsManager.getSetting(project.getId(),
                    ProjectSettingsManager.KEY_DEFAULT_ORIENTATION, "portrait");
            String startingScreen = settingsManager.getSetting(project.getId(),
                    ProjectSettingsManager.KEY_STARTING_SCREEN, "");
            mainHandler.post(() -> showProjectSettingsForm(project, screens,
                    versionName, versionCode, targetSdk, orientation, startingScreen));
        });
    }

    private void showProjectSettingsForm(ProjectModel project,
                                         List<ScreenModel> screens,
                                         String versionName, String versionCode,
                                         String targetSdk, String orientation,
                                         String startingScreen) {
        View view = LayoutInflater.from(this)
                .inflate(R.layout.dialog_project_settings, null);
        TextInputEditText editAppName = view.findViewById(R.id.input_settings_app_name);
        TextInputEditText editPackage = view.findViewById(R.id.input_settings_package);
        TextInputEditText editVersionName =
                view.findViewById(R.id.input_settings_version_name);
        TextInputEditText editVersionCode =
                view.findViewById(R.id.input_settings_version_code);
        TextInputEditText editMinSdk = view.findViewById(R.id.input_settings_min_sdk);
        TextInputEditText editTargetSdk =
                view.findViewById(R.id.input_settings_target_sdk);
        Spinner spinnerOrientation =
                view.findViewById(R.id.spinner_settings_orientation);
        Spinner spinnerStartingScreen =
                view.findViewById(R.id.spinner_settings_starting_screen);

        editAppName.setText(project.getAppName());
        editPackage.setText(project.getPackageName());
        editVersionName.setText(versionName);
        editVersionCode.setText(versionCode);
        editMinSdk.setText(String.valueOf(project.getMinSdk()));
        editTargetSdk.setText(targetSdk);

        ArrayAdapter<String> orientationAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                new String[]{"portrait", "landscape"});
        orientationAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerOrientation.setAdapter(orientationAdapter);
        spinnerOrientation.setSelection("landscape".equals(orientation) ? 1 : 0);

        List<String> screenNames = new ArrayList<>();
        List<String> screenIds = new ArrayList<>();
        for (ScreenModel screen : screens) {
            screenNames.add(screen.getName());
            screenIds.add(screen.getId());
        }
        ArrayAdapter<String> screenAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, screenNames);
        screenAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        spinnerStartingScreen.setAdapter(screenAdapter);
        int startingIndex = screenIds.indexOf(startingScreen);
        if (startingIndex < 0) {
            for (int i = 0; i < screens.size(); i++) {
                if (screens.get(i).isLauncher()) {
                    startingIndex = i;
                    break;
                }
            }
        }
        if (startingIndex >= 0) {
            spinnerStartingScreen.setSelection(startingIndex);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .setPositiveButton(R.string.action_save, null)
                .setNegativeButton(R.string.action_cancel, null)
                .create();
        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positive.setOnClickListener(v -> {
                String appName = editAppName.getText().toString();
                String packageName = editPackage.getText().toString();
                String vName = editVersionName.getText().toString();
                String vCode = editVersionCode.getText().toString();
                int minSdk;
                int target;
                try {
                    minSdk = Integer.parseInt(editMinSdk.getText().toString().trim());
                    target = Integer.parseInt(editTargetSdk.getText().toString().trim());
                } catch (Exception e) {
                    Toast.makeText(this, R.string.msg_settings_invalid,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                int selectedScreen = spinnerStartingScreen.getSelectedItemPosition();
                String startId = screenIds.isEmpty() || selectedScreen < 0 ? ""
                        : screenIds.get(selectedScreen);
                String error = settingsManager.validate(appName, packageName,
                        vName, vCode, minSdk, target, startId, screenIds);
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                    return;
                }
                String orientationValue = spinnerOrientation.getSelectedItemPosition() == 1
                        ? "landscape" : "portrait";
                executor.execute(() -> {
                    boolean ok = settingsManager.apply(project.getId(), appName,
                            packageName, vName, vCode, minSdk, target, "",
                            "", orientationValue, startId);
                    if (ok && !startId.isEmpty()) {
                        new com.nobi.appbuilder.screens.ScreenManager(
                                screenRepository).setLauncherScreen(startId);
                    }
                    mainHandler.post(() -> {
                        if (ok) {
                            Toast.makeText(this, R.string.msg_settings_saved,
                                    Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                            loadProjects();
                        } else {
                            Toast.makeText(this, R.string.msg_settings_invalid,
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                });
            });
        });
        dialog.show();
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
            menu.getMenu().add(0, MENU_EXPORT, 0, R.string.action_export);
            menu.getMenu().add(0, MENU_SETTINGS, 0, R.string.action_project_settings);
            menu.getMenu().add(0, MENU_DELETE, 0, R.string.action_delete);
            menu.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                if (id == MENU_OPEN) {
                    openEditor(project);
                } else if (id == MENU_RENAME) {
                    showRenameDialog(project);
                } else if (id == MENU_DUPLICATE) {
                    duplicateProject(project);
                } else if (id == MENU_EXPORT) {
                    exportProject(project);
                } else if (id == MENU_SETTINGS) {
                    showProjectSettingsDialog(project);
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
