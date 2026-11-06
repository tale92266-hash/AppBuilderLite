package com.nobi.appbuilder.sync.conflict;

import android.content.Context;

import com.nobi.appbuilder.sync.SynchronizationManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Applies a conflict resolution. KEEP_VISUAL regenerates the XML
 * from the IR; KEEP_CODE re-parses the XML into the IR; BACKUP_MANUAL
 * saves both versions under .sync-backup/ and leaves the decision to
 * the user; REVIEW and CANCEL change nothing.
 */
public class ConflictResolver {

    private final SynchronizationManager manager;
    private final Context appContext;
    private final String projectId;

    public ConflictResolver(SynchronizationManager manager,
                            Context context, String projectId) {
        this.manager = manager;
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
    }

    /** Applies the resolution; returns false when it could not run. */
    public boolean resolve(ConflictRecord record,
                           ConflictResolution resolution) {
        switch (resolution) {
            case KEEP_VISUAL:
                return manager.regenerateXmlForScreen(record.getScreenId());
            case KEEP_CODE:
                return manager.reparseXmlForScreen(record.getScreenId());
            case BACKUP_MANUAL:
                return backupBothVersions(record);
            case REVIEW:
            case CANCEL:
            default:
                return true;
        }
    }

    private boolean backupBothVersions(ConflictRecord record) {
        try {
            File workspace = new File(new File(
                    appContext.getFilesDir(), "code-workspaces"),
                    projectId);
            File backupDir = new File(workspace, ".sync-backup");
            backupDir.mkdirs();
            String layoutName = manager.layoutFileNameFor(
                    record.getScreenId());
            if (layoutName == null) {
                return false;
            }
            File xmlFile = new File(workspace,
                    "app/src/main/res/layout/" + layoutName);
            if (xmlFile.exists()) {
                copyFile(xmlFile, new File(backupDir,
                        layoutName + ".code-version.xml"));
            }
            String generated = manager.generateXmlForScreen(
                    record.getScreenId());
            if (generated != null) {
                try (FileOutputStream out = new FileOutputStream(
                        new File(backupDir,
                                layoutName + ".visual-version.xml"))) {
                    out.write(generated.getBytes(
                            java.nio.charset.StandardCharsets.UTF_8));
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void copyFile(File source, File dest) throws IOException {
        try (FileInputStream in = new FileInputStream(source);
             FileOutputStream out = new FileOutputStream(dest)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }
    }
}
