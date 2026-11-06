package com.nobi.appbuilder.repository;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.database.ProjectRepository;
import com.nobi.appbuilder.database.ScreenRepository;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;
import com.nobi.appbuilder.ir.IRValidator;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.model.ThemeModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Document-level persistence on top of the Phase 1 database layer — no
 * table is duplicated. A document is saved as: project row (metadata),
 * one screens row per screen (design JSON), theme JSON in
 * project_settings under key "theme_json". Saving runs inside one
 * transaction on the helper's single connection; the repositories'
 * own transactions nest inside it (SQLiteDatabase counts them).
 */
public class DocumentRepository {

    private static final String SETTING_THEME = "theme_json";

    private final DatabaseHelper dbHelper;
    private final ProjectRepository projectRepository;
    private final ScreenRepository screenRepository;

    public DocumentRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.projectRepository = new ProjectRepository(dbHelper);
        this.screenRepository = new ScreenRepository(dbHelper);
    }

    /**
     * Saves a whole validated document: project row, every screen
     * (insert or update), screens removed from the document are deleted,
     * theme is stored, and a history row is written. Returns false when
     * the document is invalid or any step fails (then nothing commits).
     */
    public boolean saveDocument(IRDocument document) {
        if (document == null || document.getProject() == null) {
            return false;
        }
        if (!IRValidator.isValid(document)) {
            return false;
        }
        ProjectModel project = document.getProject();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            if (!projectRepository.update(project)) {
                return false;
            }
            Set<String> wantedIds = new HashSet<>();
            for (ScreenModel screen : document.getScreens()) {
                screen.setProjectId(project.getId());
                wantedIds.add(screen.getId());
                if (screenRepository.getById(screen.getId()) == null) {
                    if (!screenRepository.insert(screen)) {
                        return false;
                    }
                } else if (!screenRepository.updateDesign(screen)) {
                    return false;
                }
            }
            for (ScreenModel existing : screenRepository.getByProject(project.getId())) {
                if (!wantedIds.contains(existing.getId())) {
                    screenRepository.delete(existing.getId());
                }
            }
            saveTheme(project.getId(), document.getTheme());
            DatabaseHelper.logHistory(db, project.getId(), "document_saved",
                    project.getAppName());
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /** Loads a complete document, or null when the project does not exist. */
    public IRDocument loadDocument(String projectId) {
        ProjectModel project = projectRepository.getById(projectId);
        if (project == null) {
            return null;
        }
        IRDocument document = IRDocument.fromProject(project);
        document.setTheme(loadTheme(projectId));
        return document;
    }

    /** Loads a complete document as pretty JSON, or null when missing. */
    public String loadDocumentJson(String projectId) {
        IRDocument document = loadDocument(projectId);
        return document == null ? null : IRSerializer.serializeDocument(document);
    }

    /** Saves one screen's design JSON after validating it parses. */
    public boolean saveScreenJson(String screenId, String designJson) {
        if (screenId == null || designJson == null) {
            return false;
        }
        ComponentModel root;
        try {
            root = IRDeserializer.deserializeComponent(designJson);
        } catch (IllegalArgumentException e) {
            return false;
        }
        ScreenModel screen = screenRepository.getById(screenId);
        if (screen == null) {
            return false;
        }
        screen.setRootComponent(root);
        return screenRepository.updateDesign(screen);
    }

    /** Loads one screen's design JSON, or null when missing. */
    public String loadScreenJson(String screenId) {
        ScreenModel screen = screenRepository.getById(screenId);
        return screen == null ? null : screen.getDesignJson();
    }

    /** Updates one screen's design JSON (screen must already exist). */
    public boolean updateScreenJson(String screenId, String designJson) {
        return saveScreenJson(screenId, designJson);
    }

    /** Deletes one screen. */
    public boolean deleteScreen(String screenId) {
        return screenRepository.delete(screenId);
    }

    /** Persists a project's theme JSON into project_settings. */
    public void saveTheme(String projectId, ThemeModel theme) {
        if (projectId == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("setting_key", SETTING_THEME);
        values.put("setting_value", IRSerializer.serializeTheme(
                theme == null ? new ThemeModel() : theme));
        db.insertWithOnConflict(DatabaseHelper.TABLE_SETTINGS, null, values,
                SQLiteDatabase.CONFLICT_REPLACE);
    }

    /** Reads a project's theme JSON from project_settings. */
    public ThemeModel loadTheme(String projectId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SETTINGS,
                new String[]{"setting_value"},
                "project_id = ? AND setting_key = ?",
                new String[]{projectId, SETTING_THEME}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return IRDeserializer.deserializeTheme(
                        cursor.getString(cursor.getColumnIndexOrThrow("setting_value")));
            }
        } catch (Exception e) {
            return new ThemeModel();
        }
        return new ThemeModel();
    }

    public List<ScreenModel> getScreens(String projectId) {
        List<ScreenModel> screens = screenRepository.getByProject(projectId);
        return screens == null ? new ArrayList<ScreenModel>() : screens;
    }
}
