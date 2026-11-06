package com.nobi.appbuilder.assets;

import android.content.Context;

import com.nobi.appbuilder.assets.drawable.DrawableManager;
import com.nobi.appbuilder.assets.drawable.DrawableModel;
import com.nobi.appbuilder.assets.fonts.FontManager;
import com.nobi.appbuilder.assets.image.ImageResizer;
import com.nobi.appbuilder.assets.resources.ResourceNamingManager;
import com.nobi.appbuilder.assets.vector.VectorAssetManager;
import com.nobi.appbuilder.assets.vector.VectorAssetModel;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.utils.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Exports all project resources into a generated Android Studio
 * project tree: density variants for images, vectors + shape
 * drawables as XML in drawable/, fonts in font/. Problems are
 * collected and returned — nothing is silently dropped.
 */
public class AssetExportManager {

    private final Context appContext;
    private final DatabaseHelper dbHelper;

    public AssetExportManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.dbHelper = dbHelper;
    }

    public List<String> exportToProject(String projectId, File workspaceRoot) {
        List<String> problems = new ArrayList<>();
        File resDir = new File(workspaceRoot, "app/src/main/res");
        AssetRepository repository = new AssetRepository(dbHelper);
        for (AssetModel asset : repository.getByProject(projectId)) {
            File source = new File(asset.getPath());
            if (!source.exists()) {
                problems.add("Missing file for asset " + asset.getName());
                continue;
            }
            if (asset.getType() == AssetType.IMAGE) {
                String ext = ResourceNamingManager.extensionOf(asset.getName());
                if (ext.isEmpty()) {
                    ext = "png";
                }
                List<String> written = ImageResizer.generateDensityVariants(
                        source, resDir, asset.getResourceName(), ext);
                if (written.isEmpty()) {
                    problems.add("Could not export image " + asset.getName());
                }
            } else if (asset.getType() == AssetType.FONT) {
                File fontDir = new File(resDir, "font");
                if (!fontDir.mkdirs() && !fontDir.exists()) {
                    problems.add("Could not create font folder");
                    continue;
                }
                String ext = ResourceNamingManager.extensionOf(asset.getName());
                File target = new File(fontDir,
                        asset.getResourceName() + "." + ext);
                if (!copyFile(source, target)) {
                    problems.add("Could not export font " + asset.getName());
                }
            }
        }
        VectorAssetManager vectors = new VectorAssetManager(dbHelper);
        for (VectorAssetModel vector : vectors.getByProject(projectId)) {
            File drawableDir = new File(resDir, "drawable");
            if (!drawableDir.mkdirs() && !drawableDir.exists()) {
                problems.add("Could not create drawable folder");
                continue;
            }
            File target = new File(drawableDir,
                    ResourceNamingManager.sanitize(vector.getName()) + ".xml");
            if (!writeText(target, vectors.toXml(vector))) {
                problems.add("Could not export vector " + vector.getName());
            }
        }
        DrawableManager drawables = new DrawableManager(dbHelper);
        for (DrawableModel drawable : drawables.getByProject(projectId)) {
            File drawableDir = new File(resDir, "drawable");
            if (!drawableDir.mkdirs() && !drawableDir.exists()) {
                problems.add("Could not create drawable folder");
                continue;
            }
            File target = new File(drawableDir,
                    ResourceNamingManager.sanitize(drawable.getName()) + ".xml");
            if (!writeText(target, drawable.getXml())) {
                problems.add("Could not export drawable " + drawable.getName());
            }
        }
        return problems;
    }

    private boolean copyFile(File source, File target) {
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new FileOutputStream(target)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean writeText(File target, String text) {
        try (FileOutputStream out = new FileOutputStream(target)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public File getAssetsDir(String projectId) {
        return new File(FileUtils.getProjectDir(appContext, projectId), "assets");
    }
}
