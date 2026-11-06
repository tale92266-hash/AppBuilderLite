package com.nobi.appbuilder.sync.mapper;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.ScreenModel;
import com.nobi.appbuilder.sync.ChangeOrigin;
import com.nobi.appbuilder.sync.ChangeSet;
import com.nobi.appbuilder.sync.diff.DiffOperation;
import com.nobi.appbuilder.sync.diff.DiffResult;
import com.nobi.appbuilder.sync.diff.ScreenDiffEngine;
import com.nobi.appbuilder.sync.parser.XMLHierarchyParser;
import com.nobi.appbuilder.sync.parser.XMLParseResult;
import com.nobi.appbuilder.sync.parser.XMLResourceResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * Code-to-visual direction: parsed layout XML -> JSON IR. Only
 * supported attributes and the 12 registry component types are
 * applied. Unknown attributes and unsupported elements are reported
 * as warnings and the original XML is left untouched — the IR never
 * claims to represent what it cannot.
 */
public class XMLToIRMapper {

    /** Result of applying one parsed layout to its screen. */
    public static class ApplyResult {
        public final ChangeSet changeSet;
        public final List<String> warnings = new ArrayList<>();
        public final List<String> unsupported = new ArrayList<>();

        public ApplyResult(ChangeSet changeSet) {
            this.changeSet = changeSet;
        }
    }

    public ApplyResult apply(IRDocument document, ScreenModel screen,
                             XMLParseResult parsed,
                             XMLResourceResolver resolver) {
        ChangeSet changeSet = new ChangeSet(screen.getId(),
                ChangeOrigin.CODE_EDITOR);
        ApplyResult result = new ApplyResult(changeSet);
        if (parsed == null || !parsed.isSuccess()) {
            result.warnings.add("XML could not be parsed");
            return result;
        }
        List<String> duplicates = ComponentSyncMapper
                .findDuplicateIds(parsed.getRoot());
        for (String duplicate : duplicates) {
            result.warnings.add("Duplicate android:id: " + duplicate);
        }
        ComponentModel before = screen.getRootComponent();
        XMLHierarchyParser hierarchyParser = new XMLHierarchyParser();
        ComponentModel after = hierarchyParser.parseTree(parsed.getRoot(),
                before);
        result.unsupported.addAll(hierarchyParser.getUnsupported());
        if (after == null) {
            result.warnings.add("No supported root element in XML");
            return result;
        }
        if (before != null) {
            DiffResult diff = new ScreenDiffEngine().diff(before, after);
            for (DiffOperation operation : diff.getOperations()) {
                changeSet.add(new com.nobi.appbuilder.sync.ChangeOperation(
                        toChangeType(operation.getType()),
                        operation.getComponentId(),
                        operation.getPropertyKey(),
                        operation.getOldValue(),
                        operation.getNewValue()));
            }
        } else {
            changeSet.add(com.nobi.appbuilder.sync.ChangeOperation
                    .add(after.getId()));
        }
        screen.setRootComponent(after);
        return result;
    }

    private com.nobi.appbuilder.sync.ChangeOperation.Type toChangeType(
            DiffOperation.Type type) {
        switch (type) {
            case ADD:
                return com.nobi.appbuilder.sync.ChangeOperation.Type
                        .ADD_COMPONENT;
            case REMOVE:
                return com.nobi.appbuilder.sync.ChangeOperation.Type
                        .REMOVE_COMPONENT;
            case MOVE:
                return com.nobi.appbuilder.sync.ChangeOperation.Type
                        .MOVE_COMPONENT;
            default:
                return com.nobi.appbuilder.sync.ChangeOperation.Type
                        .UPDATE_PROPERTY;
        }
    }
}
