package com.nobi.appbuilder.ui.flow;

import android.view.View;

import com.nobi.appbuilder.R;

/**
 * Wires the flow toolbar row (layout_flow_toolbar.xml): Run, Validate,
 * Save and Debug buttons.
 */
public class FlowToolbarController {

    public interface FlowToolbarListener {
        void onRun();

        void onValidate();

        void onSave();

        void onDebug();
    }

    public FlowToolbarController(View root, FlowToolbarListener listener) {
        View run = root.findViewById(R.id.btn_flow_run);
        if (run != null) {
            run.setOnClickListener(v -> listener.onRun());
        }
        View validate = root.findViewById(R.id.btn_flow_validate);
        if (validate != null) {
            validate.setOnClickListener(v -> listener.onValidate());
        }
        View save = root.findViewById(R.id.btn_flow_save);
        if (save != null) {
            save.setOnClickListener(v -> listener.onSave());
        }
        View debug = root.findViewById(R.id.btn_flow_debug);
        if (debug != null) {
            debug.setOnClickListener(v -> listener.onDebug());
        }
    }
}
