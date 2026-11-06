package com.nobi.appbuilder.sync;

import com.nobi.appbuilder.ir.IRDeserializer;
import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.ir.IRSerializer;

/**
 * A sync transaction snapshots the whole IR as JSON before applying
 * a ChangeSet, so a failed or conflicting run can roll back to the
 * exact pre-sync document.
 */
public class SynchronizationTransaction {

    private String backupJson;
    private boolean active;

    public void begin(IRDocument document) {
        backupJson = IRSerializer.serializeDocument(document);
        active = true;
    }

    public void commit() {
        backupJson = null;
        active = false;
    }

    /** Restores the pre-transaction document, or null if none. */
    public IRDocument rollback() {
        if (!active || backupJson == null) {
            return null;
        }
        try {
            IRDocument restored =
                    IRDeserializer.deserializeDocument(backupJson);
            backupJson = null;
            active = false;
            return restored;
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isActive() {
        return active;
    }
}
