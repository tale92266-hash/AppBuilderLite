package com.nobi.appbuilder.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.model.ScreenModel;

import java.util.ArrayList;
import java.util.List;

/**
 * CRUD for screens. Designs are stored as JSON documents (design_json),
 * so the repository never needs to know the component schema — that is
 * the IR layer's job in Phase 2.
 */
public class ScreenRepository {

    private final DatabaseHelper dbHelper;

    public ScreenRepository(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public boolean insert(ScreenModel screen) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            insertInTransaction(db, screen);
            db.setTransactionSuccessful();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            db.endTransaction();
        }
    }

    /** Insert without starting a transaction (caller owns it). */
    void insertInTransaction(SQLiteDatabase db, ScreenModel screen) {
        ContentValues values = new ContentValues();
        values.put("id", screen.getId());
        values.put("project_id", screen.getProjectId());
        values.put("name", screen.getName());
        values.put("is_launcher", screen.isLauncher() ? 1 : 0);
        values.put("sort_order", screen.getSortOrder());
        values.put("design_json", screen.getDesignJson());
        values.put("screen_type", screen.getScreenType());
        values.put("orientation", screen.getOrientation());
        values.put("created_at", screen.getCreatedAt());
        values.put("updated_at", screen.getUpdatedAt());
        db.insertOrThrow(DatabaseHelper.TABLE_SCREENS, null, values);
    }

    public List<ScreenModel> getByProject(String projectId) {
        List<ScreenModel> screens = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SCREENS, null,
                "project_id = ?", new String[]{projectId}, null, null,
                "sort_order ASC")) {
            while (cursor.moveToNext()) {
                screens.add(fromCursor(cursor));
            }
        }
        return screens;
    }

    public ScreenModel getById(String screenId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_SCREENS, null,
                "id = ?", new String[]{screenId}, null, null, null)) {
            if (cursor.moveToFirst()) {
                return fromCursor(cursor);
            }
        }
        return null;
    }

    public boolean updateDesign(ScreenModel screen) {
        screen.setUpdatedAt(System.currentTimeMillis());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", screen.getName());
        values.put("is_launcher", screen.isLauncher() ? 1 : 0);
        values.put("sort_order", screen.getSortOrder());
        values.put("design_json", screen.getDesignJson());
        values.put("screen_type", screen.getScreenType());
        values.put("orientation", screen.getOrientation());
        values.put("updated_at", screen.getUpdatedAt());
        int rows = db.update(DatabaseHelper.TABLE_SCREENS, values,
                "id = ?", new String[]{screen.getId()});
        return rows > 0;
    }

    public boolean delete(String screenId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_SCREENS,
                "id = ?", new String[]{screenId}) > 0;
    }

    public boolean deleteByProject(String projectId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseHelper.TABLE_SCREENS,
                "project_id = ?", new String[]{projectId});
        return true;
    }

    private ScreenModel fromCursor(Cursor cursor) {
        ScreenModel screen = new ScreenModel();
        screen.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
        screen.setProjectId(cursor.getString(cursor.getColumnIndexOrThrow("project_id")));
        screen.setName(cursor.getString(cursor.getColumnIndexOrThrow("name")));
        screen.setLauncher(cursor.getInt(cursor.getColumnIndexOrThrow("is_launcher")) == 1);
        screen.setSortOrder(cursor.getInt(cursor.getColumnIndexOrThrow("sort_order")));
        screen.setDesignJson(cursor.getString(cursor.getColumnIndexOrThrow("design_json")));
        screen.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("created_at")));
        screen.setUpdatedAt(cursor.getLong(cursor.getColumnIndexOrThrow("updated_at")));
        int typeIndex = cursor.getColumnIndex("screen_type");
        if (typeIndex >= 0 && !cursor.isNull(typeIndex)) {
            screen.setScreenType(cursor.getString(typeIndex));
        }
        int orientationIndex = cursor.getColumnIndex("orientation");
        if (orientationIndex >= 0 && !cursor.isNull(orientationIndex)) {
            screen.setOrientation(cursor.getString(orientationIndex));
        }
        return screen;
    }
}
