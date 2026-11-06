package com.nobi.appbuilder.sync.history;

import android.content.Context;

import com.nobi.appbuilder.database.DatabaseHelper;
import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.repository.DocumentRepository;

/**
 * Recovers an interrupted synchronization by restoring the latest
 * snapshot's IR into the database. Returns false when there is
 * nothing to restore or the restore itself fails.
 */
public class SynchronizationRecoveryManager {

    private final Context appContext;
    private final String projectId;
    private final SynchronizationHistory history;

    public SynchronizationRecoveryManager(Context context, String projectId,
                                          SynchronizationHistory history) {
        this.appContext = context.getApplicationContext();
        this.projectId = projectId;
        this.history = history;
    }

    public boolean recover() {
        SynchronizationSnapshot snapshot = history.getLatest();
        if (snapshot == null || snapshot.getIrJson() == null) {
            return false;
        }
        try {
            IRDocument document = IRDeserializer.deserializeDocument(
                    snapshot.getIrJson());
            if (document == null) {
                return false;
            }
            DatabaseHelper dbHelper = new DatabaseHelper(appContext);
            try {
                new DocumentRepository(dbHelper).saveDocument(document);
            } finally {
                dbHelper.close();
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasRecoveryPoint() {
        return history.getLatest() != null;
    }
}
