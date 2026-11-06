package com.nobi.appbuilder.ui.editor;

import android.content.ClipData;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.autosave.AutosaveManager;
import com.nobi.appbuilder.autosave.ProjectBackupManager;
import com.nobi.appbuilder.autosave.RecoveryManager;
import com.nobi.appbuilder.clipboard.ClipboardData;
import com.nobi.appbuilder.clipboard.ComponentCloner;
import com.nobi.appbuilder.clipboard.EditorClipboard;
import com.nobi.appbuilder.clipboard.PasteController;
import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.editor.DesignCanvasView;
import com.nobi.appbuilder.editor.EditorInteractionListener;
import com.nobi.appbuilder.history.CommandManager;
import com.nobi.appbuilder.history.CommandTransaction;
import com.nobi.appbuilder.history.DuplicateComponentCommand;
import com.nobi.appbuilder.history.EditorCommand;
import com.nobi.appbuilder.history.GroupComponentsCommand;
import com.nobi.appbuilder.history.MoveComponentCommand;
import com.nobi.appbuilder.history.RenameComponentCommand;
import com.nobi.appbuilder.history.ReparentComponentCommand;
import com.nobi.appbuilder.history.ResizeComponentCommand;
import com.nobi.appbuilder.history.UpdatePropertyCommand;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.screens.ScreenManager;
import com.nobi.appbuilder.screens.ScreenNavigator;
import com.nobi.appbuilder.selection.GroupSelectionController;
import com.nobi.appbuilder.selection.MultiSelectionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The visual editor screen. Wires the canvas engine, library, hierarchy
 * and toolbar together; owns loading, the command-based undo/redo engine
 * (Phase 5), debounced autosave, crash recovery and the JSON document
 * lifecycle. Every editing operation runs as a reversible command.
 */
public class EditorActivity extends AppCompatActivity implements EditorInteractionListener {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";

    private DesignCanvasView canvasView;
    private EditorToolbarController toolbarController;
    private HierarchyPanelController hierarchyPanelController;
    private PropertiesPanelController propertiesPanelController;
    private SelectionToolbarController selectionToolbarController;
    private TextView zoomLabel;
    private View libraryPanel;

    private DatabaseHelper dbHelper;
    private DocumentRepository documentRepository;
    private ScreenRepository screenRepository;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private IRDocument document;
    private ProjectModel project;
    private ScreenModel screen;
    private boolean loaded;
    private long lastSavedAt;

    /** Phase 5 engines. */
    private CommandManager commandManager;
    private AutosaveManager autosaveManager;
    private RecoveryManager recoveryManager;
    private ProjectBackupManager backupManager;
    private ScreenManager screenManager;
    private ScreenNavigator screenNavigator;
    private EditorClipboard editorClipboard;
    private PasteController pasteController;
    private MultiSelectionManager multiSelectionManager;
    private GroupSelectionController groupSelectionController;
    private ScreenManagerDialog screenManagerDialog;
    private boolean explicitSavePending;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        dbHelper = new DatabaseHelper(this);
        documentRepository = new DocumentRepository(dbHelper);
        screenRepository = new ScreenRepository(dbHelper);
        executor = Executors.newSingleThreadExecutor();

        commandManager = new CommandManager();
        commandManager.setListener(new CommandManager.HistoryListener() {
            @Override
            public void onHistoryChanged(boolean canUndo, boolean canRedo) {
                toolbarController.setUndoEnabled(canUndo);
                toolbarController.setRedoEnabled(canRedo);
            }

            @Override
            public void onCommandFailed(EditorCommand command, String reason) {
                Toast.makeText(EditorActivity.this, reason, Toast.LENGTH_SHORT).show();
            }
        });
        autosaveManager = new AutosaveManager(executor, () -> {
            saveDocumentSync();
            return true;
        });
        autosaveManager.setListener(state -> {
            switch (state) {
                case SAVING:
                    toolbarController.setSaveStatus(getString(R.string.save_status_saving));
                    break;
                case SAVED:
                    String time = new SimpleDateFormat("HH:mm", Locale.getDefault())
                            .format(new Date());
                    toolbarController.setSaveStatus(
                            getString(R.string.save_status_saved, time));
                    if (explicitSavePending) {
                        explicitSavePending = false;
                        Toast.makeText(EditorActivity.this, R.string.msg_saved,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
                case DIRTY:
                    toolbarController.setSaveStatus(
                            getString(R.string.save_status_unsaved));
                    break;
                case FAILED:
                    toolbarController.setSaveStatus(
                            getString(R.string.save_status_failed));
                    break;
                default:
                    break;
            }
        });
        recoveryManager = new RecoveryManager(dbHelper, executor);
        backupManager = new ProjectBackupManager(dbHelper, executor);
        screenManager = new ScreenManager(screenRepository);
        screenNavigator = new ScreenNavigator(screenRepository);
        editorClipboard = new EditorClipboard();
        pasteController = new PasteController();
        groupSelectionController = new GroupSelectionController();
        screenManagerDialog = new ScreenManagerDialog(this,
                new ScreenManagerDialog.ScreenDialogListener() {
                    @Override
                    public void onScreenSelected(ScreenModel selected) {
                        switchScreen(selected.getId());
                    }

                    @Override
                    public void onAddScreen() {
                        showAddScreenDialog();
                    }

                    @Override
                    public void onDuplicateScreen(ScreenModel target) {
                        duplicateScreen(target);
                    }

                    @Override
                    public void onRenameScreen(ScreenModel target) {
                        showRenameScreenDialog(target);
                    }

                    @Override
                    public void onDeleteScreen(ScreenModel target) {
                        deleteScreen(target);
                    }

                    @Override
                    public void onSetLauncherScreen(ScreenModel target) {
                        setLauncherScreen(target);
                    }
                });

        canvasView = findViewById(R.id.canvas_view);
        canvasView.setListener(this);
        canvasView.setCommandRunner(this::executeCommand);
        multiSelectionManager = new MultiSelectionManager(canvasView.getState());
        zoomLabel = findViewById(R.id.text_zoom);
        libraryPanel = findViewById(R.id.library_panel);

        toolbarController = new EditorToolbarController(
                findViewById(R.id.toolbar_editor),
                new EditorToolbarController.ToolbarListener() {
                    @Override
                    public void onBack() {
                        autosaveManager.flush();
                        finish();
                    }

                    @Override
                    public void onUndo() {
                        doUndo();
                    }

                    @Override
                    public void onRedo() {
                        doRedo();
                    }

                    @Override
                    public void onSave() {
                        explicitSavePending = true;
                        autosaveManager.saveNow();
                    }

                    @Override
                    public void onCopy() {
                        copySelection();
                    }

                    @Override
                    public void onPaste() {
                        pasteClipboard();
                    }

                    @Override
                    public void onDuplicate() {
                        canvasView.duplicateSelected();
                    }

                    @Override
                    public void onScreens() {
                        showScreensDialog();
                    }
                });

        selectionToolbarController = new SelectionToolbarController(
                findViewById(R.id.selection_toolbar),
                new SelectionToolbarController.SelectionToolbarListener() {
                    @Override
                    public void onGroup() {
                        showGroupTypePicker();
                    }

                    @Override
                    public void onUngroup() {
                        ungroupSelection();
                    }

                    @Override
                    public void onDelete() {
                        canvasView.deleteSelected();
                    }

                    @Override
                    public void onToggleLock() {
                        toggleLockSelection();
                    }

                    @Override
                    public void onVisibility() {
                        showVisibilityMenu();
                    }

                    @Override
                    public void onSelectAll() {
                        multiSelectionManager.selectAll(canvasView.getState().getRoot());
                        onSelectionChanged();
                    }
                });

        new ComponentPanelController(
                findViewById(R.id.library_recycler),
                findViewById(R.id.library_search),
                new ComponentPanelController.ComponentChosenListener() {
                    @Override
                    public void onComponentTap(String type) {
                        canvasView.armLibraryDrop(type);
                        Toast.makeText(EditorActivity.this,
                                getString(R.string.msg_tap_to_place, type),
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public boolean onComponentDragStart(View view, String type) {
                        ClipData data = ClipData.newPlainText("component-type", type);
                        view.startDragAndDrop(data, new View.DragShadowBuilder(view),
                                null, 0);
                        return true;
                    }
                });

        hierarchyPanelController = new HierarchyPanelController(
                findViewById(R.id.hierarchy_header),
                findViewById(R.id.hierarchy_recycler),
                findViewById(R.id.hierarchy_chevron),
                findViewById(R.id.hierarchy_info),
                findViewById(R.id.hierarchy_search),
                canvasView.getState(),
                new HierarchyPanelController.HierarchyPanelListener() {
                    @Override
                    public void onHierarchySelect(String componentId) {
                        canvasView.invalidate();
                        updateSelectedInfo();
                        syncPropertiesPanel();
                    }

                    @Override
                    public void onHierarchyLongPress(ComponentModel model) {
                        showContextMenu(model);
                    }

                    @Override
                    public void onMutationStarting() {
                        // History is command-based now; mutations push commands.
                    }

                    @Override
                    public void onDocumentCommitted() {
                        scheduleAutosave();
                        hierarchyPanelController.refresh();
                        updateSelectedInfo();
                        updateUndoRedoState();
                        canvasView.invalidate();
                    }

                    @Override
                    public void onTreeError(String message) {
                        Toast.makeText(EditorActivity.this, message,
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onReparentCommitted(ComponentModel model,
                                                    ComponentModel oldParent,
                                                    int oldIndex) {
                        ComponentModel root = canvasView.getState().getRoot();
                        ComponentModel newParent = ComponentHitTester.findParent(
                                root, model.getId());
                        if (newParent == null) {
                            return;
                        }
                        int newIndex = newParent.getChildren().indexOf(model);
                        ReparentComponentCommand command =
                                new ReparentComponentCommand(root, model, newParent, newIndex);
                        commandManager.execute(command);
                        afterMutation();
                    }
                });
        hierarchyPanelController.setCommandRunner(this::executeCommand);

        propertiesPanelController = new PropertiesPanelController(
                findViewById(R.id.properties_panel),
                findViewById(R.id.properties_recycler),
                findViewById(R.id.text_properties_title),
                findViewById(R.id.button_properties_reset),
                findViewById(R.id.button_properties_close),
                new PropertiesPanelController.PropertiesListener() {
                    @Override
                    public void onPropertyChangeStarting() {
                        // Property edits run as UpdatePropertyCommands now.
                    }

                    @Override
                    public void onPropertyChanged() {
                        canvasView.invalidate();
                        scheduleAutosave();
                        hierarchyPanelController.refresh();
                        updateUndoRedoState();
                    }

                    @Override
                    public void onPropertyValidationFailed(
                            com.nobi.appbuilder.properties.PropertyDefinition definition,
                            String error) {
                        Toast.makeText(EditorActivity.this, error,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        propertiesPanelController.setCommandRunner(this::executeCommand);

        MaterialButtonToggleGroup toolToggle = findViewById(R.id.toggle_tools);
        toolToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            if (checkedId == R.id.btn_tool_hand) {
                canvasView.useHandTool();
            } else {
                canvasView.useSelectionTool();
            }
        });
        toolToggle.check(R.id.btn_tool_select);

        Button libraryButton = findViewById(R.id.btn_library);
        libraryButton.setOnClickListener(v -> {
            int visibility = libraryPanel.getVisibility() == View.VISIBLE
                    ? View.GONE : View.VISIBLE;
            libraryPanel.setVisibility(visibility);
        });
        findViewById(R.id.btn_zoom_out).setOnClickListener(v -> canvasView.zoomOut());
        findViewById(R.id.btn_zoom_in).setOnClickListener(v -> canvasView.zoomIn());
        zoomLabel.setOnClickListener(v -> canvasView.fitToScreen());

        loadDocument();
    }

    private void loadDocument() {
        Intent intent = getIntent();
        String projectId = intent.getStringExtra(EXTRA_PROJECT_ID);
        String screenId = intent.getStringExtra(EXTRA_SCREEN_ID);
        if (projectId == null) {
            Toast.makeText(this, R.string.msg_editor_no_project, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        executor.execute(() -> {
            IRDocument loadedDoc = documentRepository.loadDocument(projectId);
            mainHandler.post(() -> {
                if (loadedDoc == null || loadedDoc.getProject() == null) {
                    Toast.makeText(this, R.string.msg_editor_no_project,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                document = loadedDoc;
                project = document.toProject();
                screen = null;
                for (ScreenModel candidate : document.getScreens()) {
                    if (candidate.getId().equals(screenId)) {
                        screen = candidate;
                        break;
                    }
                }
                if (screen == null && !document.getScreens().isEmpty()) {
                    screen = document.getScreens().get(0);
                }
                if (screen == null) {
                    Toast.makeText(this, R.string.msg_editor_no_screen,
                            Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                ComponentModel root = screen.getRootComponent();
                if (root == null) {
                    root = ComponentFactory.createRootContainer();
                    screen.setRootComponent(root);
                }
                loaded = true;
                lastSavedAt = project.getUpdatedAt();
                toolbarController.setTitle(project.getAppName() + " — " + screen.getName());
                canvasView.setScreen(screen, root);
                hierarchyPanelController.refresh();
                updateSelectedInfo();
                updateZoomLabel();
                commandManager.clear();
                updateUndoRedoState();
                recoveryManager.setLastSavedAt(lastSavedAt);
                recoveryManager.startAutoSnapshot(
                        new RecoveryManager.SnapshotSource() {
                            @Override
                            public String getProjectId() {
                                return project.getId();
                            }

                            @Override
                            public String getScreenId() {
                                return screen == null ? null : screen.getId();
                            }

                            @Override
                            public String getDocumentJson() {
                                return getCurrentDocumentJson();
                            }
                        });
                checkRecovery();
                List<String> navProblems =
                        screenNavigator.validateNavigationReferences(project.getId());
                if (!navProblems.isEmpty()) {
                    Toast.makeText(EditorActivity.this, navProblems.get(0),
                            Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    // ------------------------------------------------------------------
    // EditorInteractionListener
    // ------------------------------------------------------------------

    @Override
    public void onMutationStarting() {
        // History is command-based now; the engine's live mutations are
        // wrapped into one command when the gesture commits.
    }

    @Override
    public void onDocumentChanged() {
        scheduleAutosave();
        updateSelectedInfo();
    }

    @Override
    public void onDocumentCommitted() {
        scheduleAutosave();
        hierarchyPanelController.refresh();
        propertiesPanelController.refreshValues();
        updateSelectedInfo();
        updateUndoRedoState();
    }

    @Override
    public void onDragCommitted(List<ComponentModel> models,
                                Map<String, float[]> startPositions) {
        if (models == null || models.isEmpty() || startPositions == null) {
            return;
        }
        Map<String, float[]> endPositions = new HashMap<>();
        for (ComponentModel model : models) {
            endPositions.put(model.getId(),
                    new float[]{model.getX(), model.getY()});
        }
        executeCommand(new MoveComponentCommand(models, startPositions, endPositions));
    }

    @Override
    public void onResizeCommitted(ComponentModel target, RectF startBounds) {
        if (target == null || startBounds == null) {
            return;
        }
        executeCommand(new ResizeComponentCommand(target,
                Math.round(startBounds.left), Math.round(startBounds.top),
                Math.round(startBounds.width()), Math.round(startBounds.height()),
                target.getX(), target.getY(),
                target.getWidth(), target.getHeight()));
    }

    @Override
    public void onSelectionChanged() {
        hierarchyPanelController.syncSelection();
        updateSelectedInfo();
        syncPropertiesPanel();
        updateSelectionToolbar();
        updateClipboardButtons();
    }

    /** Shows the properties panel for a single selection, hides it otherwise. */
    private void syncPropertiesPanel() {
        CanvasState state = canvasView.getState();
        if (state == null || propertiesPanelController == null) {
            return;
        }
        ComponentModel single = state.getSingleSelection();
        if (single != null && state.getRoot() != null) {
            propertiesPanelController.show(single, state.getRoot());
        } else {
            propertiesPanelController.hide();
        }
    }

    @Override
    public void onRequestContextMenu(ComponentModel model, float screenX, float screenY) {
        showContextMenu(model);
    }

    @Override
    public void onEmptyCanvasTapped() {
    }

    @Override
    public void onZoomChanged(float zoom) {
        updateZoomLabel();
    }

    // ------------------------------------------------------------------
    // Command engine (Phase 5)
    // ------------------------------------------------------------------

    /** Runs a command, then refreshes the UI and schedules autosave. */
    private boolean executeCommand(EditorCommand command) {
        if (!loaded || command == null) {
            return false;
        }
        boolean ok = commandManager.execute(command);
        if (ok) {
            afterMutation();
        }
        return ok;
    }

    /** Shared refresh after any successful command or commit. The
     * properties panel is NOT touched here: while the user is typing
     * in an editor, rebuilding or re-reading its rows would move the
     * cursor and lose focus. */
    private void afterMutation() {
        canvasView.invalidate();
        hierarchyPanelController.refresh();
        updateSelectedInfo();
        updateUndoRedoState();
        updateSelectionToolbar();
        updateClipboardButtons();
        scheduleAutosave();
    }

    private void doUndo() {
        if (commandManager.undo()) {
            afterMutation();
            syncPropertiesPanel();
            propertiesPanelController.refreshValues();
        }
    }

    private void doRedo() {
        if (commandManager.redo()) {
            afterMutation();
            syncPropertiesPanel();
            propertiesPanelController.refreshValues();
        }
    }

    private void updateUndoRedoState() {
        toolbarController.setUndoEnabled(commandManager.canUndo());
        toolbarController.setRedoEnabled(commandManager.canRedo());
    }

    private void updateClipboardButtons() {
        CanvasState state = canvasView.getState();
        boolean hasSelection = state != null && state.hasSelection();
        toolbarController.setCopyEnabled(hasSelection);
        toolbarController.setDuplicateEnabled(hasSelection);
        toolbarController.setPasteEnabled(editorClipboard.hasContent());
    }

    // ------------------------------------------------------------------
    // Save / autosave / recovery (Phase 5)
    // ------------------------------------------------------------------

    private void scheduleAutosave() {
        autosaveManager.scheduleSave();
    }

    /** Synchronous document write; runs on the background executor. */
    private void saveDocumentSync() {
        if (!loaded || document == null || screen == null) {
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root != null) {
            screen.setRootComponent(root);
        }
        String previousJson = documentRepository.loadDocumentJson(project.getId());
        if (previousJson != null) {
            backupManager.backup(project.getId(), previousJson);
        }
        boolean ok = documentRepository.saveDocument(document);
        if (ok) {
            lastSavedAt = System.currentTimeMillis();
            recoveryManager.setLastSavedAt(lastSavedAt);
        }
    }

    private void saveDocument(boolean showToast) {
        if (!loaded) {
            return;
        }
        executor.execute(() -> {
            saveDocumentSync();
            if (showToast) {
                mainHandler.post(() -> Toast.makeText(EditorActivity.this,
                        R.string.msg_saved, Toast.LENGTH_SHORT).show());
            }
        });
    }

    /** Current document JSON (with the live canvas root synced in). */
    private String getCurrentDocumentJson() {
        if (!loaded || document == null || screen == null) {
            return null;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root != null) {
            screen.setRootComponent(root);
        }
        return IRSerializer.serializeDocument(document);
    }

    /** Offers to restore unsaved work when a snapshot is newer than the save. */
    private void checkRecovery() {
        recoveryManager.getSnapshot(project.getId(), snapshot -> {
            if (snapshot == null || !snapshot.hasUnrestoredWork(lastSavedAt)) {
                return;
            }
            new AlertDialog.Builder(this)
                    .setTitle(R.string.recovery_title)
                    .setMessage(R.string.recovery_message)
                    .setPositiveButton(R.string.recovery_restore, (dialog, which) -> {
                        try {
                            IRDocument restored = IRDeserializer.deserializeDocument(
                                    snapshot.getDocumentJson());
                            document = restored;
                            project = document.toProject();
                            screen = null;
                            for (ScreenModel candidate : document.getScreens()) {
                                if (candidate.getId().equals(screen.getId())) {
                                    screen = candidate;
                                    break;
                                }
                            }
                            if (screen == null && !document.getScreens().isEmpty()) {
                                screen = document.getScreens().get(0);
                            }
                            if (screen == null) {
                                Toast.makeText(this, R.string.msg_load_failed,
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            ComponentModel root = screen.getRootComponent();
                            if (root == null) {
                                root = ComponentFactory.createRootContainer();
                                screen.setRootComponent(root);
                            }
                            commandManager.clear();
                            canvasView.setScreen(screen, root);
                            hierarchyPanelController.refresh();
                            updateSelectedInfo();
                            toolbarController.setTitle(project.getAppName()
                                    + " — " + screen.getName());
                            saveDocument(false);
                            recoveryManager.deleteSnapshot(project.getId());
                        } catch (Exception e) {
                            Toast.makeText(this, R.string.msg_load_failed,
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.recovery_discard, (dialog, which) ->
                            recoveryManager.deleteSnapshot(project.getId()))
                    .show();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        autosaveManager.flush();
        if (project == null) {
            return;
        }
        String json = getCurrentDocumentJson();
        if (json != null) {
            recoveryManager.saveSnapshot(project.getId(),
                    screen == null ? null : screen.getId(), json, lastSavedAt);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autosaveManager.cancelPending();
        recoveryManager.stopAutoSnapshot();
        executor.shutdown();
        dbHelper.close();
    }

    // ------------------------------------------------------------------
    // Context menu / rename / info
    // ------------------------------------------------------------------

    private void showContextMenu(ComponentModel model) {
        canvasView.getState().select(model.getId());
        canvasView.invalidate();
        updateSelectedInfo();
        String[] items = {
                getString(R.string.ctx_duplicate),
                getString(R.string.ctx_copy),
                getString(R.string.ctx_paste),
                getString(R.string.ctx_delete),
                getString(R.string.ctx_bring_forward),
                getString(R.string.ctx_send_backward),
                getString(R.string.ctx_rename_id)
        };
        new AlertDialog.Builder(this)
                .setTitle(model.getType() + " — " + model.getId())
                .setItems(items, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            canvasView.duplicateSelected();
                            break;
                        case 1:
                            copySelection();
                            break;
                        case 2:
                            pasteClipboard();
                            break;
                        case 3:
                            canvasView.deleteSelected();
                            break;
                        case 4:
                            canvasView.reorder(model, true);
                            break;
                        case 5:
                            canvasView.reorder(model, false);
                            break;
                        case 6:
                            showRenameDialog(model);
                            break;
                        default:
                            break;
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showRenameDialog(ComponentModel model) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(model.getId());
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
                .setTitle(R.string.ctx_rename_id)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String newId = input.getText().toString().trim();
                    if (!newId.matches("[A-Za-z][A-Za-z0-9_]*")) {
                        Toast.makeText(this, R.string.msg_invalid_id,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    ComponentModel root = canvasView.getState().getRoot();
                    RenameComponentCommand command =
                            new RenameComponentCommand(root, model, newId);
                    if (!command.canExecute()) {
                        Toast.makeText(this,
                                command.getValidationError() == null
                                        ? getString(R.string.msg_duplicate_id)
                                        : command.getValidationError(),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (executeCommand(command)) {
                        canvasView.getState().select(newId);
                        afterMutation();
                        syncPropertiesPanel();
                        propertiesPanelController.refreshValues();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void updateSelectedInfo() {
        CanvasState state = canvasView.getState();
        if (state == null) {
            return;
        }
        if (!state.hasSelection()) {
            hierarchyPanelController.setSelectedInfo(
                    getString(R.string.info_no_selection));
            return;
        }
        ComponentModel single = state.getSingleSelection();
        if (single == null) {
            hierarchyPanelController.setSelectedInfo(
                    getString(R.string.info_multi_selection, state.getSelectedIds().size()));
            return;
        }
        ComponentModel root = state.getRoot();
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        RectF bounds = root == null ? null
                : ComponentHitTester.resolveBoundsFor(root, single.getId(), screenBounds);
        if (bounds == null) {
            hierarchyPanelController.setSelectedInfo(
                    single.getType() + " — " + single.getId());
            return;
        }
        hierarchyPanelController.setSelectedInfo(getString(R.string.info_selected,
                single.getType(), single.getId(),
                Math.round(bounds.width()), Math.round(bounds.height()),
                Math.round(bounds.left), Math.round(bounds.top)));
    }

    private void updateZoomLabel() {
        zoomLabel.setText(getString(R.string.info_zoom,
                canvasView.getViewport().getZoomPercent()));
    }

    // ------------------------------------------------------------------
    // Copy / paste / duplicate (Phase 5 clipboard)
    // ------------------------------------------------------------------

    private void copySelection() {
        List<ComponentModel> selected = canvasView.getSelectedModels();
        if (selected.isEmpty()) {
            return;
        }
        List<ComponentModel> clones = ComponentCloner.cloneList(selected);
        editorClipboard.set(new ClipboardData(clones,
                screen == null ? null : screen.getId()));
        canvasView.copySelected();
        updateClipboardButtons();
        Toast.makeText(this, R.string.msg_copied, Toast.LENGTH_SHORT).show();
    }

    private void pasteClipboard() {
        if (!editorClipboard.hasContent()) {
            Toast.makeText(this, R.string.msg_nothing_to_paste,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        PasteController.PasteResult result = pasteController.preparePaste(
                editorClipboard.get(), root, null, PasteController.PASTE_OFFSET_DP);
        if (result.error != null || result.pasted.isEmpty()) {
            Toast.makeText(this, result.error == null
                            ? getString(R.string.msg_paste_failed) : result.error,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        CommandTransaction transaction =
                new CommandTransaction("Paste " + result.pasted.size() + " components");
        for (EditorCommand command : result.commands) {
            transaction.add(command);
        }
        if (executeCommand(transaction)) {
            canvasView.getState().select(
                    result.pasted.get(result.pasted.size() - 1).getId());
            syncPropertiesPanel();
            updateSelectedInfo();
            updateSelectionToolbar();
            Toast.makeText(this, R.string.msg_pasted, Toast.LENGTH_SHORT).show();
        }
    }

    // ------------------------------------------------------------------
    // Selection toolbar: group / ungroup / lock / visibility
    // ------------------------------------------------------------------

    private void updateSelectionToolbar() {
        CanvasState state = canvasView.getState();
        if (state == null || !state.hasSelection()) {
            selectionToolbarController.hide();
            return;
        }
        List<ComponentModel> selected = state.getSelectedModels();
        boolean canGroup = selected.size() >= 2
                && groupSelectionController.canGroup(state.getRoot(), selected,
                        "FrameLayout") == null;
        boolean canUngroup = selected.size() == 1
                && ComponentHitTester.isContainer(selected.get(0))
                && !selected.get(0).getChildren().isEmpty();
        boolean allLocked = !selected.isEmpty();
        for (ComponentModel model : selected) {
            if (!model.isLocked()) {
                allLocked = false;
                break;
            }
        }
        selectionToolbarController.show(selected.size(), canGroup, canUngroup,
                allLocked);
    }

    private void showGroupTypePicker() {
        CanvasState state = canvasView.getState();
        List<ComponentModel> selected = state.getSelectedModels();
        String[] types = groupSelectionController.getContainerTypes();
        new AlertDialog.Builder(this)
                .setTitle(R.string.msg_group_type)
                .setItems(types, (dialog, which) -> {
                    EditorCommand command = groupSelectionController.groupCommand(
                            state.getRoot(), selected, types[which]);
                    if (command == null) {
                        String error = groupSelectionController.canGroup(
                                state.getRoot(), selected, types[which]);
                        Toast.makeText(this, error == null
                                        ? getString(R.string.msg_group_failed) : error,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (executeCommand(command)) {
                        GroupComponentsCommand group =
                                (GroupComponentsCommand) command;
                        canvasView.getState().select(group.getContainer().getId());
                        syncPropertiesPanel();
                        updateSelectedInfo();
                        updateSelectionToolbar();
                        Toast.makeText(this, R.string.msg_grouped,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void ungroupSelection() {
        CanvasState state = canvasView.getState();
        ComponentModel single = state.getSingleSelection();
        if (single == null) {
            return;
        }
        EditorCommand command = groupSelectionController.ungroupCommand(
                state.getRoot(), single);
        if (command == null) {
            Toast.makeText(this, R.string.msg_group_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        if (executeCommand(command)) {
            Toast.makeText(this, R.string.msg_ungrouped, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleLockSelection() {
        CanvasState state = canvasView.getState();
        List<ComponentModel> selected = state.getSelectedModels();
        if (selected.isEmpty()) {
            return;
        }
        boolean anyUnlocked = false;
        for (ComponentModel model : selected) {
            if (!model.isLocked()) {
                anyUnlocked = true;
                break;
            }
        }
        boolean targetLocked = anyUnlocked;
        CommandTransaction transaction = new CommandTransaction(
                targetLocked ? "Lock components" : "Unlock components");
        for (ComponentModel model : selected) {
            transaction.add(new UpdatePropertyCommand(model, state.getRoot(),
                    "locked", String.valueOf(model.isLocked()),
                    String.valueOf(targetLocked)));
        }
        executeCommand(transaction);
    }

    private void showVisibilityMenu() {
        View anchor = findViewById(R.id.selection_toolbar);
        PopupMenu menu = new PopupMenu(this, anchor);
        menu.getMenu().add(0, 1, 0, R.string.visibility_visible);
        menu.getMenu().add(0, 2, 0, R.string.visibility_invisible);
        menu.getMenu().add(0, 3, 0, R.string.visibility_gone);
        menu.setOnMenuItemClickListener(item -> {
            String value = item.getItemId() == 2 ? "invisible"
                    : item.getItemId() == 3 ? "gone" : "visible";
            CanvasState state = canvasView.getState();
            CommandTransaction transaction =
                    new CommandTransaction("Change visibility");
            for (ComponentModel model : state.getSelectedModels()) {
                transaction.add(new UpdatePropertyCommand(model, state.getRoot(),
                        "visibility", model.getProperty("visibility", "visible"),
                        value));
            }
            executeCommand(transaction);
            return true;
        });
        menu.show();
    }

    // ------------------------------------------------------------------
    // Screens (Phase 5)
    // ------------------------------------------------------------------

    private void showScreensDialog() {
        if (project == null) {
            return;
        }
        screenManagerDialog.show(screenManager.getScreens(project.getId()),
                screen == null ? null : screen.getId());
    }

    private void refreshScreensDialog() {
        if (project == null) {
            return;
        }
        screenManagerDialog.update(screenManager.getScreens(project.getId()),
                screen == null ? null : screen.getId());
    }

    private void showAddScreenDialog() {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint(R.string.screens_new_name);
        new AlertDialog.Builder(this)
                .setTitle(R.string.screens_add)
                .setView(input)
                .setPositiveButton(R.string.action_create, (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    String error = com.nobi.appbuilder.screens.ScreenValidator
                            .validateName(name,
                                    screenManager.getScreens(project.getId()), null);
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    ScreenModel created = screenManager.createScreen(
                            project.getId(), name, ScreenModel.TYPE_ACTIVITY,
                            ScreenModel.ORIENTATION_PORTRAIT);
                    if (created == null) {
                        Toast.makeText(this, R.string.msg_save_failed,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    document.getScreens().add(created);
                    refreshScreensDialog();
                    Toast.makeText(this, R.string.msg_screen_created,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void duplicateScreen(ScreenModel target) {
        String baseName = getString(R.string.screens_duplicate_name,
                target.getName());
        String copyName = baseName;
        List<ScreenModel> existing = screenManager.getScreens(project.getId());
        int suffix = 2;
        while (com.nobi.appbuilder.screens.ScreenValidator.validateName(
                copyName, existing, null) != null) {
            copyName = baseName + " (" + suffix + ")";
            suffix++;
        }
        ScreenModel copy = screenManager.duplicateScreen(target.getId(), copyName);
        if (copy == null) {
            Toast.makeText(this, R.string.msg_save_failed,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        document.getScreens().add(copy);
        refreshScreensDialog();
        Toast.makeText(this, R.string.msg_screen_duplicated,
                Toast.LENGTH_SHORT).show();
    }

    private void showRenameScreenDialog(ScreenModel target) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(target.getName());
        input.setSelection(input.getText().length());
        new AlertDialog.Builder(this)
                .setTitle(R.string.screens_rename)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String error = screenManager.renameScreen(target.getId(),
                            input.getText().toString().trim());
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    target.setName(input.getText().toString().trim());
                    if (screen != null && screen.getId().equals(target.getId())) {
                        toolbarController.setTitle(project.getAppName()
                                + " — " + screen.getName());
                    }
                    refreshScreensDialog();
                    Toast.makeText(this, R.string.msg_screen_renamed,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void deleteScreen(ScreenModel target) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.action_delete)
                .setMessage(target.getName())
                .setPositiveButton(R.string.action_delete, (dialog, which) -> {
                    String error = screenManager.deleteScreen(target.getId());
                    if (error != null) {
                        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                        return;
                    }
                    document.getScreens().remove(target);
                    if (screen != null && screen.getId().equals(target.getId())) {
                        List<ScreenModel> remaining =
                                screenManager.getScreens(project.getId());
                        if (!remaining.isEmpty()) {
                            switchScreen(remaining.get(0).getId());
                        }
                    }
                    refreshScreensDialog();
                    Toast.makeText(this, R.string.msg_screen_deleted,
                            Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void setLauncherScreen(ScreenModel target) {
        if (screenManager.setLauncherScreen(target.getId())) {
            for (ScreenModel candidate : document.getScreens()) {
                candidate.setLauncher(candidate.getId().equals(target.getId()));
            }
            refreshScreensDialog();
            Toast.makeText(this, R.string.msg_screen_launcher,
                    Toast.LENGTH_SHORT).show();
        }
    }

    /** Switches the open screen, saving the current root first. */
    private void switchScreen(String screenId) {
        if (screen != null && screen.getId().equals(screenId)) {
            return;
        }
        ScreenModel target = screenNavigator.openScreen(project.getId(), screenId);
        if (target == null) {
            Toast.makeText(this, R.string.msg_editor_no_screen,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        autosaveManager.flush();
        if (screen != null) {
            ComponentModel currentRoot = canvasView.getState().getRoot();
            if (currentRoot != null) {
                screen.setRootComponent(currentRoot);
            }
        }
        screen = target;
        ComponentModel root = screen.getRootComponent();
        if (root == null) {
            root = ComponentFactory.createRootContainer();
            screen.setRootComponent(root);
        }
        commandManager.clear();
        canvasView.getState().clearSelection();
        canvasView.setScreen(screen, root);
        hierarchyPanelController.refresh();
        propertiesPanelController.hide();
        updateSelectedInfo();
        updateSelectionToolbar();
        updateUndoRedoState();
        toolbarController.setTitle(project.getAppName() + " — " + screen.getName());
        refreshScreensDialog();
    }
}
