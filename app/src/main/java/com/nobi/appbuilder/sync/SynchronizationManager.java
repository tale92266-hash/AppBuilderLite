package com.nobi.appbuilder.sync;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.nobi.appbuilder.codeeditor.EditorFileManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.repository.DocumentRepository;
import com.nobi.appbuilder.sync.conflict.ConflictManager;
import com.nobi.appbuilder.sync.conflict.ConflictRecord;
import com.nobi.appbuilder.sync.conflict.ConflictResolution;
import com.nobi.appbuilder.sync.conflict.ConflictResolutionPolicy;
import com.nobi.appbuilder.sync.conflict.ConflictResolver;
import com.nobi.appbuilder.sync.conflict.ConflictType;
import com.nobi.appbuilder.sync.history.ChangeOriginTracker;
import com.nobi.appbuilder.sync.history.SynchronizationHistory;
import com.nobi.appbuilder.sync.history.SynchronizationSnapshot;
import com.nobi.appbuilder.sync.mapper.IRToXMLMapper;
import com.nobi.appbuilder.sync.mapper.XMLToIRMapper;
import com.nobi.appbuilder.sync.parser.XMLLayoutParser;
import com.nobi.appbuilder.sync.parser.XMLParseResult;
import com.nobi.appbuilder.sync.parser.XMLResourceResolver;
import com.nobi.appbuilder.sync.reload.CodeEditorRefreshManager;
import com.nobi.appbuilder.sync.reload.PreviewRefreshManager;
import com.nobi.appbuilder.sync.reload.VisualEditorRefreshManager;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central sync facade. JSON IR is canonical: visual changes flow
 * IR -> XML, code changes flow XML -> IR. Every run is debounced,
 * locked per project, snapshotted before it mutates anything, and
 * never overwrites a manually edited file without a recorded
 * conflict. Generated writes never re-enter as manual edits: the
 * manager's own file writes go through EditorFileManager directly
 * (not through any editor save path) and every component it touches
 * is marked GENERATOR in the ChangeOriginTracker.
 */
public class SynchronizationManager {

    private final Context appContext;
    private final String projectId;
    private final SynchronizationPolicy policy;
    private final SynchronizationQueue queue = new SynchronizationQueue();
    private final SynchronizationScheduler scheduler =
            new SynchronizationScheduler();
    private final SynchronizationLockManager lockManager =
            new SynchronizationLockManager();
    private final ConflictManager conflictManager;
    private final SynchronizationHistory history;
    private final ChangeTracker changeTracker;
    private final ChangeOriginTracker originTracker =
            new ChangeOriginTracker();
    private final IRToXMLMapper irToXmlMapper = new IRToXMLMapper();
    private final XMLToIRMapper xmlToIrMapper = new XMLToIRMapper();
    private final SynchronizationState state = new SynchronizationState();
    private final List<SynchronizationListener> listeners =
            new CopyOnWriteArrayList<>();
    private final Set<String> unsupportedScreens = new HashSet<>();
    private final ExecutorService executor =
            Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SynchronizationManager(Context context, String projectId) {
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
        this.policy = new SynchronizationPolicy(context);
        this.conflictManager = new ConflictManager(context, projectId);
        this.history = new SynchronizationHistory(context, projectId);
        this.changeTracker = new ChangeTracker(context, projectId);
    }

    public SynchronizationPolicy getPolicy() {
        return policy;
    }

    public ConflictManager getConflictManager() {
        return conflictManager;
    }

    public SynchronizationHistory getHistory() {
        return history;
    }

    public ChangeTracker getChangeTracker() {
        return changeTracker;
    }

    public SynchronizationState getState() {
        return state;
    }

    public void addListener(SynchronizationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SynchronizationListener listener) {
        listeners.remove(listener);
    }

    public File getWorkspaceDir() {
        return new File(new File(appContext.getFilesDir(),
                "code-workspaces"), projectId);
    }

    // ---- Entry points (called by the editors) --------------------

    public void onVisualChange(String screenId) {
        if (screenId == null) {
            return;
        }
        queue.enqueue(new SynchronizationEvent(
                SynchronizationEvent.KIND_VISUAL_CHANGE, projectId,
                screenId, null, ChangeOrigin.VISUAL_EDITOR));
        setState(SynchronizationState.Status.VISUAL_PENDING,
                "Visual changes pending");
        scheduleRun();
    }

    public void onCodeChange(String screenId, String filePath) {
        if (screenId == null) {
            return;
        }
        queue.enqueue(new SynchronizationEvent(
                SynchronizationEvent.KIND_CODE_CHANGE, projectId,
                screenId, null, ChangeOrigin.CODE_EDITOR));
        setState(SynchronizationState.Status.CODE_PENDING,
                "Code changes pending");
        scheduleRun();
    }

    public void onExternalChange(String filePath) {
        String screenId = screenIdForLayoutFile(
                new File(filePath).getName());
        if (screenId == null) {
            return;
        }
        queue.enqueue(new SynchronizationEvent(
                SynchronizationEvent.KIND_EXTERNAL_CHANGE, projectId,
                screenId, null, ChangeOrigin.EXTERNAL_FILE));
        scheduleRun();
    }

    /** Screen id for a layout file name, or null when it is not one. */
    public String screenIdForLayoutFile(String fileName) {
        IRDocument document = loadDocument();
        if (document == null) {
            return null;
        }
        for (ScreenModel screen : document.getScreens()) {
            String layoutFile = irToXmlMapper.layoutPathFor(document,
                    screen);
            if (layoutFile != null
                    && layoutFile.endsWith("/" + fileName)) {
                return screen.getId();
            }
        }
        return null;
    }

    public void synchronizeNow() {
        if (!policy.isAutoSync()) {
            return;
        }
        if (!lockManager.tryLock(projectId)) {
            scheduler.schedule(this::synchronizeNow, 1000);
            return;
        }
        executor.execute(this::runSync);
    }

    private void scheduleRun() {
        if (!policy.isAutoSync()) {
            return;
        }
        scheduler.schedule(this::synchronizeNow, policy.getDebounceMs());
    }

    // ---- The run ---------------------------------------------------

    private void runSync() {
        long started = System.currentTimeMillis();
        List<SynchronizationError> errors = new ArrayList<>();
        int applied = 0;
        int conflicts = 0;
        try {
            setState(SynchronizationState.Status.SYNCING, "Syncing");
            SynchronizationEvent event;
            while ((event = queue.poll()) != null) {
                if (SynchronizationEvent.KIND_VISUAL_CHANGE
                        .equals(event.getKind())) {
                    applied += visualToCode(event.getScreenId(), errors);
                } else {
                    applied += codeToVisual(event.getScreenId(), errors);
                }
            }
            conflicts = conflictManager.unresolvedCount();
            if (!errors.isEmpty()) {
                setState(state.getStatus() == SynchronizationState.Status
                        .INVALID_XML
                        ? SynchronizationState.Status.INVALID_XML
                        : SynchronizationState.Status.MANUAL_REQUIRED,
                        errors.get(0).getMessage());
            } else if (conflicts > 0) {
                setState(SynchronizationState.Status.CONFLICT,
                        conflicts + " conflict(s) need a decision");
            } else if (state.getStatus()
                    == SynchronizationState.Status.UNSUPPORTED) {
                // Kept: unsupported XML stays code-only.
            } else {
                setState(SynchronizationState.Status.SYNCHRONIZED,
                        "Synchronized");
                state.setLastSyncTimeMs(System.currentTimeMillis());
            }
            notifyCompleted(SynchronizationResult.success(applied,
                    System.currentTimeMillis() - started));
        } catch (Exception e) {
            errors.add(new SynchronizationError("sync_failed",
                    e.getMessage(), null, 0));
            setState(SynchronizationState.Status.MANUAL_REQUIRED,
                    "Sync failed: " + e.getMessage());
            notifyCompleted(SynchronizationResult.failure(errors,
                    System.currentTimeMillis() - started));
        } finally {
            lockManager.unlock(projectId);
        }
    }

    /** Visual -> code. Returns 1 when the XML was written. */
    private int visualToCode(String screenId,
                             List<SynchronizationError> errors) {
        if (unsupportedScreens.contains(screenId)) {
            setState(SynchronizationState.Status.UNSUPPORTED,
                    "Screen has unsupported XML — kept code-only");
            return 0;
        }
        IRDocument document = loadDocument();
        if (document == null) {
            errors.add(new SynchronizationError("load_failed",
                    "Project could not be loaded", null, 0));
            return 0;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            errors.add(new SynchronizationError("screen_missing",
                    "Screen not found: " + screenId, null, 0));
            return 0;
        }
        String xml = irToXmlMapper.generateXml(document, screen);
        if (xml == null) {
            errors.add(new SynchronizationError("xml_failed",
                    "XML generation failed for " + screenId, null, 0));
            return 0;
        }
        String layoutPath = irToXmlMapper.layoutPathFor(document, screen);
        File file = new File(getWorkspaceDir(), layoutPath);
        if (file.exists()) {
            String current = readFile(file);
            if (current != null && current.equals(xml)) {
                return 0;
            }
            if (current != null && changeTracker.isTracked(layoutPath)
                    && changeTracker.hasChanged(layoutPath, current)) {
                return handleVisualConflict(screenId, layoutPath, errors);
            }
        }
        history.add(new SynchronizationSnapshot(projectId, screenId,
                ChangeOrigin.VISUAL_EDITOR,
                IRSerializer.serializeDocument(document),
                "Before visual-to-code sync"));
        file.getParentFile().mkdirs();
        if (!new EditorFileManager(getWorkspaceDir()).save(file, xml)) {
            errors.add(new SynchronizationError("write_failed",
                    "Could not write " + layoutPath, layoutPath, 0));
            return 0;
        }
        changeTracker.record(layoutPath, xml);
        markTree(screen.getRootComponent(), ChangeOrigin.GENERATOR);
        CodeEditorRefreshManager.requestRefresh(projectId, screenId,
                ChangeOrigin.VISUAL_EDITOR);
        PreviewRefreshManager.requestRefresh(projectId, screenId,
                ChangeOrigin.VISUAL_EDITOR);
        return 1;
    }

    private int handleVisualConflict(String screenId, String layoutPath,
                                     List<SynchronizationError> errors) {
        ConflictResolution auto = new ConflictResolutionPolicy(policy)
                .autoResolution();
        if (auto == ConflictResolution.KEEP_VISUAL) {
            return forceWriteXml(screenId, errors);
        }
        if (auto == ConflictResolution.KEEP_CODE) {
            return codeToVisual(screenId, errors);
        }
        ConflictRecord record = conflictManager.record(screenId, null,
                ConflictType.VISUAL_VS_CODE, "visual editor version",
                "XML code version");
        setState(SynchronizationState.Status.CONFLICT,
                "Conflict on screen " + screenId);
        notifyConflict(record);
        return 0;
    }

    private int forceWriteXml(String screenId,
                              List<SynchronizationError> errors) {
        IRDocument document = loadDocument();
        if (document == null) {
            return 0;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            return 0;
        }
        String xml = irToXmlMapper.generateXml(document, screen);
        if (xml == null) {
            return 0;
        }
        String layoutPath = irToXmlMapper.layoutPathFor(document, screen);
        File file = new File(getWorkspaceDir(), layoutPath);
        file.getParentFile().mkdirs();
        if (!new EditorFileManager(getWorkspaceDir()).save(file, xml)) {
            errors.add(new SynchronizationError("write_failed",
                    "Could not write " + layoutPath, layoutPath, 0));
            return 0;
        }
        changeTracker.record(layoutPath, xml);
        unsupportedScreens.remove(screenId);
        return 1;
    }

    /** Code -> visual. Returns the number of applied changes. */
    private int codeToVisual(String screenId,
                             List<SynchronizationError> errors) {
        IRDocument document = loadDocument();
        if (document == null) {
            errors.add(new SynchronizationError("load_failed",
                    "Project could not be loaded", null, 0));
            return 0;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            errors.add(new SynchronizationError("screen_missing",
                    "Screen not found: " + screenId, null, 0));
            return 0;
        }
        String layoutPath = irToXmlMapper.layoutPathFor(document, screen);
        File file = new File(getWorkspaceDir(), layoutPath);
        if (!file.exists()) {
            errors.add(new SynchronizationError("file_missing",
                    "Layout file not found: " + layoutPath,
                    layoutPath, 0));
            return 0;
        }
        String content = readFile(file);
        if (content == null) {
            errors.add(new SynchronizationError("read_failed",
                    "Could not read " + layoutPath, layoutPath, 0));
            return 0;
        }
        XMLParseResult parsed = new XMLLayoutParser().parseText(content);
        if (!parsed.isSuccess()) {
            setState(SynchronizationState.Status.INVALID_XML,
                    parsed.getErrors().isEmpty() ? "Invalid XML"
                            : parsed.getErrors().get(0).toString());
            for (com.nobi.appbuilder.sync.parser.XMLParseError error
                    : parsed.getErrors()) {
                errors.add(new SynchronizationError("invalid_xml",
                        error.getMessage(), layoutPath, error.getLine()));
            }
            return 0;
        }
        SynchronizationTransaction transaction =
                new SynchronizationTransaction();
        transaction.begin(document);
        history.add(new SynchronizationSnapshot(projectId, screenId,
                ChangeOrigin.CODE_EDITOR,
                IRSerializer.serializeDocument(document),
                "Before code-to-visual sync"));
        XMLToIRMapper.ApplyResult result = xmlToIrMapper.apply(document,
                screen, parsed,
                new XMLResourceResolver(getWorkspaceDir()));
        if (!result.unsupported.isEmpty()) {
            // Unsupported constructs: keep the original XML as the
            // source of truth, do not apply a partial IR.
            unsupportedScreens.add(screenId);
            transaction.rollback();
            setState(SynchronizationState.Status.UNSUPPORTED,
                    "Unsupported XML structure in " + layoutPath);
            errors.add(new SynchronizationError("unsupported_xml",
                    result.unsupported.get(0), layoutPath, 0));
            return 0;
        }
        unsupportedScreens.remove(screenId);
        int applied = result.changeSet.size();
        if (applied > 0) {
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            try {
                if (!new DocumentRepository(dbHelper)
                        .saveDocument(document)) {
                    transaction.rollback();
                    errors.add(new SynchronizationError("save_failed",
                            "IR validation failed after XML apply",
                            null, 0));
                    return 0;
                }
            } finally {
                dbHelper.close();
            }
            transaction.commit();
            markTree(screen.getRootComponent(), ChangeOrigin.CODE_EDITOR);
        }
        changeTracker.record(layoutPath, content);
        if (applied > 0) {
            VisualEditorRefreshManager.requestRefresh(projectId, screenId,
                    ChangeOrigin.CODE_EDITOR);
            PreviewRefreshManager.requestRefresh(projectId, screenId,
                    ChangeOrigin.CODE_EDITOR);
        }
        return applied;
    }

    // ---- Conflict resolution entry points ------------------------

    public boolean regenerateXmlForScreen(String screenId) {
        List<SynchronizationError> errors = new ArrayList<>();
        return forceWriteXml(screenId, errors) > 0;
    }

    public boolean reparseXmlForScreen(String screenId) {
        List<SynchronizationError> errors = new ArrayList<>();
        unsupportedScreens.remove(screenId);
        return codeToVisual(screenId, errors) >= 0
                && errors.isEmpty();
    }

    public String generateXmlForScreen(String screenId) {
        IRDocument document = loadDocument();
        if (document == null) {
            return null;
        }
        ScreenModel screen = document.getScreen(screenId);
        return screen == null ? null
                : irToXmlMapper.generateXml(document, screen);
    }

    public String layoutFileNameFor(String screenId) {
        IRDocument document = loadDocument();
        if (document == null) {
            return null;
        }
        ScreenModel screen = document.getScreen(screenId);
        if (screen == null) {
            return null;
        }
        String layoutPath = irToXmlMapper.layoutPathFor(document, screen);
        int slash = layoutPath.lastIndexOf('/');
        return slash >= 0 ? layoutPath.substring(slash + 1) : layoutPath;
    }

    public boolean resolveConflict(ConflictRecord record,
                                   ConflictResolution resolution) {
        boolean ok = new ConflictResolver(this, appContext, projectId)
                .resolve(record, resolution);
        if (ok) {
            conflictManager.markResolved(record, resolution.name());
            synchronizeNow();
        }
        return ok;
    }

    // ---- Helpers ---------------------------------------------------

    private IRDocument loadDocument() {
        DatabaseHelper dbHelper = new DatabaseHelper(appContext);
        try {
            return new DocumentRepository(dbHelper).loadDocument(projectId);
        } finally {
            dbHelper.close();
        }
    }

    private String readFile(File file) {
        try {
            return new String(java.nio.file.Files.readAllBytes(
                    file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private void markTree(ComponentModel root, ChangeOrigin origin) {
        if (root == null) {
            return;
        }
        originTracker.mark(root.getId(), origin);
        if (root.getChildren() != null) {
            for (ComponentModel child : root.getChildren()) {
                markTree(child, origin);
            }
        }
    }

    private void setState(SynchronizationState.Status status,
                          String message) {
        state.setStatus(status);
        state.setMessage(message);
        state.setPendingChanges(queue.size());
        state.setConflictCount(conflictManager.unresolvedCount());
        mainHandler.post(() -> {
            for (SynchronizationListener listener : listeners) {
                listener.onSyncStateChanged(state);
            }
        });
    }

    private void notifyConflict(ConflictRecord record) {
        mainHandler.post(() -> {
            for (SynchronizationListener listener : listeners) {
                listener.onConflictDetected(record);
            }
        });
    }

    private void notifyCompleted(SynchronizationResult result) {
        mainHandler.post(() -> {
            for (SynchronizationListener listener : listeners) {
                listener.onSyncCompleted(result);
            }
        });
    }

    public void release() {
        scheduler.cancel();
        executor.shutdownNow();
    }
}
