package com.nobi.appbuilder.assets.drawable;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.nobi.appbuilder.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Facade for drawable resources: create from the generators, save
 * as validated XML in SQLite, list, delete. The XML is written into
 * the generated project by the export step.
 */
public class DrawableManager {

    private final DatabaseHelper dbHelper;

    public DrawableManager(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public DrawableModel createShape(String projectId, String name,
                                     String shape, String solidColor,
                                     Integer strokeWidthDp, String strokeColor,
                                     Integer cornerRadiusDp) {
        String xml = ShapeDrawableGenerator.shapeXml(shape, solidColor,
                strokeWidthDp, strokeColor, cornerRadiusDp, null, null);
        return save(projectId, name, DrawableModel.KIND_SHAPE, xml);
    }

    public DrawableModel createGradient(String projectId, String name,
                                        String type, int angle,
                                        String startColor, String centerColor,
                                        String endColor) {
        String xml = GradientDrawableGenerator.gradientXml(type, angle,
                startColor, centerColor, endColor);
        return save(projectId, name, DrawableModel.KIND_GRADIENT, xml);
    }

    public DrawableModel createSelector(String projectId, String name,
                                        String pressed, String selected,
                                        String focused, String defaultDrawable) {
        String xml = SelectorDrawableGenerator.selectorXml(pressed, selected,
                focused, defaultDrawable);
        return save(projectId, name, DrawableModel.KIND_SELECTOR, xml);
    }

    public DrawableModel createRipple(String projectId, String name,
                                      String rippleColor,
                                      String contentDrawable) {
        String xml = RippleDrawableGenerator.rippleXml(rippleColor,
                contentDrawable);
        return save(projectId, name, DrawableModel.KIND_RIPPLE, xml);
    }

    public DrawableModel save(String projectId, String name, String kind,
                              String xml) {
        String root = DrawableModel.KIND_SELECTOR.equals(kind) ? "selector"
                : DrawableModel.KIND_RIPPLE.equals(kind) ? "ripple" : "shape";
        if (!DrawableValidator.validate(xml, root).isEmpty()) {
            return null;
        }
        DrawableModel model = new DrawableModel();
        model.setId(com.nobi.appbuilder.utils.IDGenerator.newId());
        model.setProjectId(projectId);
        model.setName(name);
        model.setKind(kind);
        model.setXml(xml);
        model.setCreatedAt(System.currentTimeMillis());
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("id", model.getId());
        values.put("project_id", projectId);
        values.put("name", name);
        values.put("kind", kind);
        values.put("xml", xml);
        values.put("created_at", model.getCreatedAt());
        if (db.insert(DatabaseHelper.TABLE_DRAWABLE_RESOURCES,
                null, values) < 0) {
            return null;
        }
        return model;
    }

    public List<DrawableModel> getByProject(String projectId) {
        List<DrawableModel> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_DRAWABLE_RESOURCES,
                null, "project_id = ?", new String[]{projectId}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                DrawableModel model = new DrawableModel();
                model.setId(cursor.getString(cursor.getColumnIndexOrThrow("id")));
                model.setProjectId(cursor.getString(
                        cursor.getColumnIndexOrThrow("project_id")));
                model.setName(cursor.getString(
                        cursor.getColumnIndexOrThrow("name")));
                model.setKind(cursor.getString(
                        cursor.getColumnIndexOrThrow("kind")));
                model.setXml(cursor.getString(
                        cursor.getColumnIndexOrThrow("xml")));
                model.setCreatedAt(cursor.getLong(
                        cursor.getColumnIndexOrThrow("created_at")));
                out.add(model);
            }
        }
        return out;
    }

    public boolean delete(String drawableId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(DatabaseHelper.TABLE_DRAWABLE_RESOURCES,
                "id = ?", new String[]{drawableId}) > 0;
    }
}
