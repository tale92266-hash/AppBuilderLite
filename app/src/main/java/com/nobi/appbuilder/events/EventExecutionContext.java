package com.nobi.appbuilder.events;

import android.app.Activity;
import android.view.View;

import com.nobi.appbuilder.flow.FlowExecutionContext;
import com.nobi.appbuilder.preview.PreviewContext;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Runtime context for executing one event: who fired it, the preview
 * it runs in (if any), the flow run it belongs to (if any), and the
 * log + navigation trail it produces. No code is ever executed from
 * this context — it only carries data between the fixed built-ins.
 */
public class EventExecutionContext {

    private final String projectId;
    private final String screenId;
    private final String componentId;
    private View sourceView;
    private PreviewContext previewContext;
    private FlowExecutionContext flowContext;
    private final List<String> log = new ArrayList<>();
    private final List<String> navigationEvents = new ArrayList<>();
    private final Map<String, String> data = new LinkedHashMap<>();

    public EventExecutionContext(String projectId, String screenId,
                                 String componentId) {
        this.projectId = projectId;
        this.screenId = screenId;
        this.componentId = componentId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getScreenId() {
        return screenId;
    }

    public String getComponentId() {
        return componentId;
    }

    public View getSourceView() {
        return sourceView;
    }

    public void setSourceView(View sourceView) {
        this.sourceView = sourceView;
    }

    public PreviewContext getPreviewContext() {
        return previewContext;
    }

    public void setPreviewContext(PreviewContext previewContext) {
        this.previewContext = previewContext;
    }

    public FlowExecutionContext getFlowContext() {
        return flowContext;
    }

    public void setFlowContext(FlowExecutionContext flowContext) {
        this.flowContext = flowContext;
    }

    public Activity getActivity() {
        return previewContext == null ? null : previewContext.getActivity();
    }

    public void log(String entry) {
        if (entry != null) {
            log.add(entry);
            if (flowContext != null) {
                flowContext.log(entry);
            }
        }
    }

    public List<String> getLog() {
        return log;
    }

    public void recordNavigation(String targetScreenId) {
        if (targetScreenId != null) {
            navigationEvents.add(targetScreenId);
            if (flowContext != null) {
                flowContext.recordNavigation(targetScreenId);
            }
        }
    }

    public List<String> getNavigationEvents() {
        return navigationEvents;
    }

    public void putData(String key, String value) {
        if (key != null && value != null) {
            data.put(key, value);
        }
    }

    public String getData(String key) {
        return data.get(key);
    }

    public void setVariable(String name, String value) {
        if (flowContext != null) {
            flowContext.setVariable(name, value);
        } else if (name != null && value != null) {
            data.put("var:" + name, value);
        }
    }

    public String getVariable(String name) {
        if (flowContext != null) {
            return flowContext.getVariable(name);
        }
        return data.get("var:" + name);
    }
}
