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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.nobi.appbuilder.R;
import com.nobi.appbuilder.components.ComponentFactory;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.editor.CanvasState;
import com.nobi.appbuilder.editor.ComponentHitTester;
import com.nobi.appbuilder.editor.DesignCanvasView;
import com.nobi.appbuilder.editor.EditorInteractionListener;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The visual editor screen. Wires the canvas engine, library, hierarchy
 * and toolbar together; owns loading, undo/redo snapshots, debounced
 * autosave and the JSON document lifecycle. Undo/redo here is a
 * snapshot stack (Phase 5 replaces it with the Command Pattern).
 */
public class EditorActivity extends AppCompatActivity implements EditorInteractionListener {

    public static final String EXTRA_PROJECT_ID = "project_id";
    public static final String EXTRA_SCREEN_ID = "screen_id";

    private static final int MAX_UNDO_STEPS = 50;
    private static final long AUTOSAVE_DELAY_MS = 800;

    private DesignCanvasView canvasView;
    private EditorToolbarController toolbarController;
    private HierarchyController hierarchyController;
    private EditorPanelController panelController;
    private TextView zoomLabel;
    private View libraryPanel;

    private DatabaseHelper dbHelper;
    private DocumentRepository documentRepository;
    private ExecutorService executor;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Handler autosaveHandler = new Handler(Looper.getMainLooper());

    private IRDocument document;
    private ProjectModel project;
    private ScreenModel screen;
    private boolean loaded;

    private final Deque<String> undoStack = new ArrayDeque<>();
    private final Deque<String> redoStack = new ArrayDeque<>();

    private final Runnable autosaveRunnable = () -> saveDocument(false);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        dbHelper = new DatabaseHelper(this);
        documentRepository = new DocumentRepository(dbHelper);
        executor = Executors.newSingleThreadExecutor();

        canvasView = findViewById(R.id.canvas_view);
        canvasView.setListener(this);
        zoomLabel = findViewById(R.id.text_zoom);
        libraryPanel = findViewById(R.id.library_panel);

        toolbarController = new EditorToolbarController(
                findViewById(R.id.toolbar_editor),
                new EditorToolbarController.ToolbarListener() {
                    @Override
                    public void onBack() {
                        saveDocument(false);
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
                        saveDocument(true);
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

        hierarchyController = new HierarchyController(
                findViewById(R.id.hierarchy_recycler),
                canvasView.getState(),
                new HierarchyController.HierarchyListener() {
                    @Override
                    public void onHierarchySelect(String componentId) {
                        canvasView.invalidate();
                        updateSelectedInfo();
                    }

                    @Override
                    public void onHierarchyLongPress(ComponentModel model) {
                        showContextMenu(model);
                    }
                });

        panelController = new EditorPanelController(
                findViewById(R.id.hierarchy_header),
                findViewById(R.id.hierarchy_recycler),
                findViewById(R.id.hierarchy_chevron),
                findViewById(R.id.hierarchy_info));

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
                toolbarController.setTitle(project.getAppName() + " — " + screen.getName());
                canvasView.setScreen(screen, root);
                hierarchyController.refresh();
                updateSelectedInfo();
                updateZoomLabel();
                undoStack.clear();
                redoStack.clear();
                updateUndoRedoState();
            });
        });
    }

    // ------------------------------------------------------------------
    // EditorInteractionListener
    // ------------------------------------------------------------------

    @Override
    public void onMutationStarting() {
        pushUndoSnapshot();
    }

    @Override
    public void onDocumentChanged() {
        scheduleAutosave();
        updateSelectedInfo();
    }

    @Override
    public void onDocumentCommitted() {
        scheduleAutosave();
        hierarchyController.refresh();
        updateSelectedInfo();
        updateUndoRedoState();
    }

    @Override
    public void onSelectionChanged() {
        hierarchyController.syncSelection();
        updateSelectedInfo();
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
    // Undo / redo (snapshot stack)
    // ------------------------------------------------------------------

    private void pushUndoSnapshot() {
        if (!loaded) {
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root == null) {
            return;
        }
        undoStack.push(IRSerializer.serializeComponent(root));
        while (undoStack.size() > MAX_UNDO_STEPS) {
            undoStack.removeLast();
        }
        redoStack.clear();
        updateUndoRedoState();
    }

    private void doUndo() {
        if (undoStack.isEmpty()) {
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root != null) {
            redoStack.push(IRSerializer.serializeComponent(root));
        }
        restoreFromJson(undoStack.pop());
        updateUndoRedoState();
    }

    private void doRedo() {
        if (redoStack.isEmpty()) {
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root != null) {
            undoStack.push(IRSerializer.serializeComponent(root));
        }
        restoreFromJson(redoStack.pop());
        updateUndoRedoState();
    }

    private void restoreFromJson(String json) {
        try {
            ComponentModel restored = IRDeserializer.deserializeComponent(json);
            screen.setRootComponent(restored);
            canvasView.getState().setRoot(restored);
            canvasView.invalidate();
            hierarchyController.refresh();
            updateSelectedInfo();
            scheduleAutosave();
        } catch (Exception e) {
            Toast.makeText(this, R.string.msg_undo_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUndoRedoState() {
        toolbarController.setUndoEnabled(!undoStack.isEmpty());
        toolbarController.setRedoEnabled(!redoStack.isEmpty());
    }

    // ------------------------------------------------------------------
    // Save
    // ------------------------------------------------------------------

    private void scheduleAutosave() {
        autosaveHandler.removeCallbacks(autosaveRunnable);
        autosaveHandler.postDelayed(autosaveRunnable, AUTOSAVE_DELAY_MS);
    }

    private void saveDocument(boolean showToast) {
        if (!loaded || document == null || screen == null) {
            return;
        }
        ComponentModel root = canvasView.getState().getRoot();
        if (root != null) {
            screen.setRootComponent(root);
        }
        executor.execute(() -> {
            boolean ok = documentRepository.saveDocument(document);
            if (showToast) {
                mainHandler.post(() -> Toast.makeText(EditorActivity.this,
                        ok ? R.string.msg_saved : R.string.msg_save_failed,
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDocument(false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        autosaveHandler.removeCallbacks(autosaveRunnable);
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
                            canvasView.copySelected();
                            break;
                        case 2:
                            if (canvasView.pasteClipboard() == null) {
                                Toast.makeText(this, R.string.msg_nothing_to_paste,
                                        Toast.LENGTH_SHORT).show();
                            }
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
                    if (root != null && !newId.equals(model.getId())
                            && root.findById(newId) != null) {
                        Toast.makeText(this, R.string.msg_duplicate_id,
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    pushUndoSnapshot();
                    model.setId(newId);
                    canvasView.getState().select(newId);
                    canvasView.invalidate();
                    hierarchyController.refresh();
                    updateSelectedInfo();
                    scheduleAutosave();
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
            panelController.setSelectedInfo(getString(R.string.info_no_selection));
            return;
        }
        ComponentModel single = state.getSingleSelection();
        if (single == null) {
            panelController.setSelectedInfo(
                    getString(R.string.info_multi_selection, state.getSelectedIds().size()));
            return;
        }
        ComponentModel root = state.getRoot();
        RectF screenBounds = new RectF(0, 0,
                state.getDesignWidthDp(), state.getDesignHeightDp());
        RectF bounds = root == null ? null
                : ComponentHitTester.resolveBoundsFor(root, single.getId(), screenBounds);
        if (bounds == null) {
            panelController.setSelectedInfo(single.getType() + " — " + single.getId());
            return;
        }
        panelController.setSelectedInfo(getString(R.string.info_selected,
                single.getType(), single.getId(),
                Math.round(bounds.width()), Math.round(bounds.height()),
                Math.round(bounds.left), Math.round(bounds.top)));
    }

    private void updateZoomLabel() {
        zoomLabel.setText(getString(R.string.info_zoom,
                canvasView.getViewport().getZoomPercent()));
    }
}
