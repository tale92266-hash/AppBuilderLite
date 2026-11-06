package com.nobi.appbuilder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.model.ProjectModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.utils.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * CRUD for projects. All methods are synchronous and must be called off
 * the main thread (DashboardActivity uses a single-thread ExecutorService).
 * Project creation inserts the project, its initial screen and a history
 * row in one transaction, so a half-created project can never exist.
 */
public class ProjectRepository {

    private final DatabaseHelper dbHelper;
    private final ScreenRepository screenRepository;

    public ProjectRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
        this.screenRepository = new ScreenRepository(dbHelper);
    }

    /** Creates the project and its initial screen atomically. */
    public boolean insert(ProjectModel project, ScreenModel initialScreen) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("id", project.getId());
            values.put("app_name", project.getAppName());
            values.put("package_name", project.getPackageName());
            values.put("min_sdk", project.getMinSdk());
            values.put("created_at", project.getCreatedAt());
            values.put("updated_at", project.getUpdatedAt());
            values.put("thumbnail_path", project.getThumbnailPath());
            values.put("schema_version", project.getSchemaVersion());
            if (db.insertOrThrow(DatabaseHelper.TABLE_PROJECTS, null, values) < 0) {
                return false;
            }
            if (initialScreen != null) {
                screenRepository.insertInTransaction(db, initialScreen);
            }
            DatabaseHelper.logHistory(db, project.getId(), "created",
                    project.getAppName());
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    public List<ProjectModel> getAll() {
        List<ProjectModel> projects = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null, null,
                null, null, null, "updated_at DESC")) {
            while (cursor.moveToNext()) {
                projects.add(fromCursor(cursor));
            }
        }
        return projects;
    }

    public List<ProjectModel> search(String query) {
        List<ProjectModel> projects = new ArrayList<>();
        if (query == null || query.trim().isEmpty()) {
            return getAll();
        }
        String like = "%" + query.trim() + "%";
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null,
                "app_name LIKE ? OR package_name LIKE ?",
                new String[]{like, like}, null, null, "updated_at DESC")) {
            while (cursor.moveToNext()) {
                projects.add(fromCursor(cursor));
            }
        }
        return projects;
    }

    public ProjectModel getById(String projectId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_PROJECTS, null,
                "id = ?", new String[]{projectId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                ProjectModel project = fromCursor(cursor);
                project.setScreens(screenRepository.getByProject(projectId));
                return project;
            }
        }
        return null;
    }

    public boolean update(ProjectModel project) {
        project.setUpdatedAt(System.currentTimeMillis());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("app_name", project.getAppName());
        values.put("package_name", project.getPackageName());
        values.put("min_sdk", project.getMinSdk());
        values.put("updated_at", project.getUpdatedAt());
        values.put("thumbnail_path", project.getThumbnailPath());
        int rows = db.update(DatabaseHelper.TABLE_PROJECTS, values,
                "id = ?", new String[]{project.getId()});
        return rows > 0;
    }

    /** Deletes the project; screens, assets and settings cascade. */
    public boolean delete(String projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(DatabaseHelper.TABLE_PROJECTS,
                "id = ?", new String[]{projectId});
        return rows > 0;
    }

    /** Deep copy: new ids, " Copy" suffix, all screens copied. */
    public ProjectModel duplicate(String projectId) {
        ProjectModel source = getById(projectId);
        if (source == null) {
            return null;
        }
        ProjectModel copy = new ProjectModel(
                IDGenerator.newProjectId(),
                source.getAppName() + " Copy",
                source.getPackageName(),
                source.getMinSdk());
        copy.setSchemaVersion(source.getSchemaVersion());
        ScreenModel firstScreen = null;
        List<ScreenModel> copiedScreens = new ArrayList<>();
        for (ScreenModel screen : source.getScreens()) {
            ScreenModel screenCopy = new ScreenModel(
                    IDGenerator.newScreenId(), copy.getId(), screen.getName(),
                    screen.isLauncher(), screen.getSortOrder());
            screenCopy.setDesignJson(screen.getDesignJson());
            copiedScreens.add(screenCopy);
            if (firstScreen == null) {
                firstScreen = screenCopy;
            }
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put("id", copy.getId());
            values.put("app_name", copy.getAppName());
            values.put("package_name", copy.getPackageName());
            values.put("min_sdk", copy.getMinSdk());
            values.put("created_at", copy.getCreatedAt());
            values.put("updated_at", copy.getUpdatedAt());
            values.put("schema_version", copy.getSchemaVersion());
            db.insertOrThrow(DatabaseHelper.TABLE_PROJECTS, null, values);
            for (ScreenModel screenCopy : copiedScreens) {
                screenRepository.insertInTransaction(db, screenCopy);
            }
            DatabaseHelper.logHistory(db, copy.getId(), "duplicated",
                    "from " + source.getAppName());
            db.setTransactionSuccessful();
            copy.setScreens(copiedScreens);
            return copy;
        } catch (Exception e) {
            return null;
        } finally {
            db.endTransaction();
        }
    }

    public int countScreens(String projectId) {
        return screenRepository.getByProject(projectId).size();
    }

    private ProjectModel fromCursor(Cursor cursor) {
        ProjectModel project = new ProjectModel();
        project.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        project.setAppName(cursor.getString(cursor.getColumnIndexOrThrow("app_name")));
        project.setPackageName(cursor.getString(cursor.getColumnIndexOrThrow("package_name")));
        project.setMinSdk(cursor.getInt(cursor.getColumnIndexOrThrow("min_sdk")));
        project.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        project.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        project.setThumbnailPath(cursor.getString(cursor.getColumnIndexOrThrow("thumbnail_path")));
        project.setSchemaVersion(cursor.getInt(cursor.getColumnIndexOrThrow("schema_version")));
        return project;
    }
}
