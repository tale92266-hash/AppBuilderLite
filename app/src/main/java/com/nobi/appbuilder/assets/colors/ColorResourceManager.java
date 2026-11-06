package com.nobi.appbuilder.assets.colors;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.Gson;
import com.nobi.appbuilder.assets.resources.ResourceNamingManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.library.design.ColorToken;
import com.nobi.appbuilder.library.design.DesignSystemManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Colour + gradient resources. Colours live in the Phase 12 design
 * system (ColorTokens) — this manager reads/writes them there and
 * keeps gradients in color_resources. Saving a colour never
 * overwrites a design token without going through the same
 * validated path.
 */
public class ColorResourceManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;
    private final DesignSystemManager designSystemManager;
    private final Gson gson = new Gson();

    public ColorResourceManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
        this.designSystemManager = new DesignSystemManager(context, dbHelper);
    }

    public List<ColorResourceModel> getColors(String projectId) {
        List<ColorResourceModel> out = new ArrayList<>();
        for (ColorToken token : designSystemManager.getColors(projectId)) {
            out.add(new ColorResourceModel(
                    ResourceNamingManager.sanitize(token.getName()),
                    token.getHex()));
        }
        return out;
    }

    public boolean saveColor(String projectId, ColorResourceModel color) {
        if (color == null || !color.isValid()) {
            return false;
        }
        return designSystemManager.updateColor(projectId, color.getName(),
                color.getHex());
    }

    public boolean saveGradient(String projectId, GradientModel gradient) {
        if (!GradientValidator.validate(gradient).isEmpty()) {
            return false;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("project_id", projectId);
        values.put("name", ResourceNamingManager.sanitize(gradient.getName()));
        values.put("kind", "gradient");
        values.put("value", gson.toJson(gradient));
        values.put("updated_at", System.currentTimeMillis());
        long row = db.insertWithOnConflict(DatabaseHelper.TABLE_COLOR_RESOURCES,
                null, values, SQLiteDatabase.CONFLICT_REPLACE);
        return row >= 0;
    }

    public List<GradientModel> getGradients(String projectId) {
        List<GradientModel> out = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        try (Cursor cursor = db.query(DatabaseHelper.TABLE_COLOR_RESOURCES,
                null, "project_id = ? AND kind = ?",
                new String[]{projectId, "gradient"}, null, null,
                "name ASC")) {
            while (cursor.moveToNext()) {
                String json = cursor.getString(
                        cursor.getColumnIndexOrThrow("value"));
                try {
                    GradientModel gradient = gson.fromJson(json,
                            GradientModel.class);
                    if (gradient != null) {
                        out.add(gradient);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return out;
    }

    /** colours.xml entries for the generated project. */
    public String colorsXml(String projectId) {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n");
        for (ColorResourceModel color : getColors(projectId)) {
            xml.append("    <color name=\"").append(color.getName())
                    .append("\">").append(color.getHex()).append("</color>\n");
        }
        xml.append("</resources>\n");
        return xml.toString();
    }
}
