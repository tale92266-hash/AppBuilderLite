package com.nobi.appbuilder.sync.diff;

import com.nobi.appbuilder.ir.IRDocument;
import com.nobi.appbuilder.model.ScreenModel;

import java.util.LinkedHashMap;
import java.util.Map;

/** Diffs two whole project documents, screen by screen. */
public class ProjectDiffEngine {

    /** Screen id -> diff for every screen that changed. */
    public Map<String, DiffResult> diff(IRDocument oldDocument,
                                        IRDocument newDocument) {
        Map<String, DiffResult> out = new LinkedHashMap<>();
        if (oldDocument == null || newDocument == null) {
            return out;
        }
        ScreenDiffEngine screenDiff = new ScreenDiffEngine();
        for (ScreenModel newScreen : newDocument.getScreens()) {
            ScreenModel oldScreen =
                    oldDocument.getScreen(newScreen.getId());
            DiffResult result = screenDiff.diff(
                    oldScreen == null ? null : oldScreen.getRootComponent(),
                    newScreen.getRootComponent());
            if (result.hasChanges()) {
                out.put(newScreen.getId(), result);
            }
        }
        for (ScreenModel oldScreen : oldDocument.getScreens()) {
            if (newDocument.getScreen(oldScreen.getId()) == null) {
                DiffResult result = new DiffResult();
                result.add(DiffOperation.remove(oldScreen.getId()));
                out.put(oldScreen.getId(), result);
            }
        }
        return out;
    }
}
