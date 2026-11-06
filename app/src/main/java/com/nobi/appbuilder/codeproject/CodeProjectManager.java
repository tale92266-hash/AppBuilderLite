package com.nobi.appbuilder.codeproject;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nobi.appbuilder.database.AssetRepository;
import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.export.AndroidStudioProjectBuilder;
import com.nobi.appbuilder.export.ProjectFileManager;
import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.ProjectGenerator;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.repository.DocumentRepository;

import java.io.File;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns the editor's working copy of a generated project. The
 * workspace lives under the app's internal files dir. Regeneration
 * never silently overwrites a user-edited file: a sidecar of
 * generated-content hashes records what the generator last wrote,
 * and any file whose disk content differs from that hash is left
 * alone and reported as a conflict.
 */
public class CodeProjectManager {

    private static final String HASHES_FILE = ".generated-hashes.json";

    private final Context appContext;
    private final String projectId;

    public CodeProjectManager(Context context, String projectId) {
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
    }

    public File getWorkspaceDir() {
        File dir = new File(new File(appContext.getFilesDir(),
                "code-workspaces"), projectId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /** Generates (or refreshes) the workspace from the current IR.
     * Returns paths that were left untouched because the user edited
     * them — the conflict list for the UI. */
    public List<String> syncFromGeneration() {
        List<String> conflicts = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(appContext);
        try {
            IRDocument document = new DocumentRepository(dbHelper)
                    .loadDocument(projectId);
            if (document == null || document.getProject() == null) {
                conflicts.add("Project could not be loaded");
                return conflicts;
            }
            GenerationContext context = new GenerationContext(document,
                    document.getProject(), document.getProject()
                    .getPackageName(), document.getProject().getMinSdk(),
                    35, 1, "1.0",
                    new AssetRepository(dbHelper));
            GenerationResult generation =
                    new ProjectGenerator(context).generate();
            if (!generation.isSuccess()) {
                conflicts.add("Code generation failed");
                return conflicts;
            }
            File workspace = getWorkspaceDir();
            Map<String, String> recordedHashes = readHashes(workspace);
            Map<String, String> newHashes = new HashMap<>();
            ProjectFileManager fileManager =
                    new ProjectFileManager(workspace);
            // Write generated files through the builder into a temp
            // area first, then merge with conflict protection.
            File staging = new File(workspace, ".staging");
            deleteRecursive(staging);
            staging.mkdirs();
            ProjectPackagingConfig config = new ProjectPackagingConfig();
            config.setProjectName(document.getProject().getAppName());
            config.setAppName(document.getProject().getAppName());
            config.setPackageName(document.getProject().getPackageName());
            config.setMinSdk(document.getProject().getMinSdk());
            config.setTargetSdk(35);
            config.setVersionName("1.0");
            config.setVersionCode(1);
            List<String> problems = new ArrayList<>();
            new AndroidStudioProjectBuilder(appContext).build(staging,
                    config, generation, problems);
            for (String path : new ProjectFileManager(staging)
                    .getWrittenPaths()) {
                File stagedFile = new File(staging, path);
                if (stagedFile.isDirectory()) {
                    continue;
                }
                String generatedHash = sha256(readBytes(stagedFile));
                newHashes.put(path, generatedHash);
                File target = new File(workspace, path);
                if (target.exists()) {
                    String recorded = recordedHashes.get(path);
                    String current = sha256(readBytes(target));
                    if (recorded != null && !recorded.equals(current)) {
                        conflicts.add(path);
                        continue;
                    }
                }
                target.getParentFile().mkdirs();
                copyFile(stagedFile, target);
            }
            deleteRecursive(staging);
            writeHashes(workspace, newHashes);
            return conflicts;
        } catch (Exception e) {
            conflicts.add("Workspace sync failed: " + e.getMessage());
            return conflicts;
        } finally {
            dbHelper.close();
        }
    }

    private Map<String, String> readHashes(File workspace) {
        try {
            File file = new File(workspace, HASHES_FILE);
            if (!file.exists()) {
                return new HashMap<>();
            }
            String json = new String(
                    java.nio.file.Files.readAllBytes(file.toPath()),
                    java.nio.charset.StandardCharsets.UTF_8);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            Map<String, String> map = new Gson().fromJson(json, type);
            return map == null ? new HashMap<>() : map;
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    private void writeHashes(File workspace, Map<String, String> hashes) {
        try {
            java.nio.file.Files.write(
                    new File(workspace, HASHES_FILE).toPath(),
                    new Gson().toJson(hashes).getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception ignored) {
        }
    }

    private byte[] readBytes(File file) {
        try {
            return java.nio.file.Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private String sha256(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(bytes);
            StringBuilder out = new StringBuilder();
            for (byte b : hash) {
                out.append(String.format("%02x", b));
            }
            return out.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private void copyFile(File source, File dest) throws Exception {
        try (java.io.InputStream in = new java.io.FileInputStream(source);
             java.io.OutputStream out = new java.io.FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }

    private void deleteRecursive(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursive(child);
                }
            }
        }
        file.delete();
    }
}
