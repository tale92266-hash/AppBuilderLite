package com.nobi.appbuilder.assets;

import android.content.Context;
import android.net.Uri;

import com.nobi.appbuilder.assets.image.ImageMetadataReader;
import com.nobi.appbuilder.assets.resources.ResourceNamingManager;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.utils.FileUtils;
import com.nobi.appbuilder.utils.IDGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Imports files from SAF into the project's asset folder. The
 * original is always preserved; duplicates are detected by content
 * hash and reported instead of being imported twice.
 */
public class AssetImportManager {

    public static class ImportResult {
        public AssetModel asset;
        public String error;
        public boolean duplicate;
    }

    private final Context appContext;
    private final AssetRepository repository;

    public AssetImportManager(Context context, DatabaseHelper dbHelper) {
        this.appContext = context.getApplicationContext();
        this.repository = new AssetRepository(dbHelper);
    }

    public ImportResult importFromUri(String projectId, Uri uri,
                                      String displayName) {
        ImportResult result = new ImportResult();
        String fileName = displayName == null || displayName.isEmpty()
                ? "asset" : displayName;
        AssetType type = AssetType.forFileName(fileName);
        String extension = ResourceNamingManager.extensionOf(fileName);
        File assetsDir = new File(FileUtils.getProjectDir(appContext, projectId),
                "assets");
        if (!assetsDir.exists() && !assetsDir.mkdirs()) {
            result.error = "Could not create the assets folder";
            return result;
        }
        Set<String> taken = new HashSet<>();
        for (AssetModel existing : repository.getByProject(projectId)) {
            if (existing.getResourceName() != null) {
                taken.add(existing.getResourceName());
            }
        }
        String resourceName = ResourceNamingManager.uniqueName(
                ResourceNamingManager.sanitize(fileName), taken);
        File target = new File(assetsDir, extension.isEmpty()
                ? resourceName : resourceName + "." + extension);
        String hash;
        try {
            hash = copyWithHash(uri, target);
        } catch (IOException e) {
            result.error = "Could not read the file: " + e.getMessage();
            return result;
        }
        AssetModel duplicate = repository.getByHash(projectId, hash);
        if (duplicate != null) {
            target.delete();
            result.duplicate = true;
            result.asset = duplicate;
            return result;
        }
        java.util.List<String> problems =
                AssetValidationManager.validateFile(target, type);
        if (!problems.isEmpty()) {
            target.delete();
            result.error = problems.get(0);
            return result;
        }
        AssetModel model = new AssetModel();
        model.setId(IDGenerator.newId());
        model.setProjectId(projectId);
        model.setName(fileName);
        model.setType(type);
        model.setPath(target.getAbsolutePath());
        model.setSizeBytes(target.length());
        model.setCreatedAt(System.currentTimeMillis());
        model.setHash(hash);
        model.setResourceName(resourceName);
        if (!repository.insert(model)) {
            target.delete();
            result.error = "Could not save the asset";
            return result;
        }
        if (type == AssetType.IMAGE) {
            AssetMetadata metadata =
                    ImageMetadataReader.read(target, model.getId());
            if (metadata != null) {
                repository.saveMetadata(metadata);
            }
        }
        result.asset = model;
        return result;
    }

    private String copyWithHash(Uri uri, File target) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IOException("SHA-256 unavailable", e);
        }
        try (InputStream in = appContext.getContentResolver()
                .openInputStream(uri);
             OutputStream out = new FileOutputStream(target)) {
            if (in == null) {
                throw new IOException("Could not open the file");
            }
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
                out.write(buffer, 0, read);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : digest.digest()) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
