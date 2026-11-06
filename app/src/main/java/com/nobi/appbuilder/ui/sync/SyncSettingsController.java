package com.nobi.appbuilder.ui.sync;

import android.app.AlertDialog;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.sync.SynchronizationPolicy;

/** Sync preferences dialog: auto-sync + conflict policy. */
public class SyncSettingsController {

    private final Context context;
    private final SynchronizationPolicy policy;

    public SyncSettingsController(Context context,
                                  SynchronizationPolicy policy) {
        this.context = context;
        this.policy = policy;
    }

    public void showDialog() {
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * context.getResources()
                .getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, pad);
        layout.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        CheckBox autoSync = new CheckBox(context);
        autoSync.setText(R.string.sync_auto_sync);
        autoSync.setChecked(policy.isAutoSync());
        layout.addView(autoSync);

        TextView policyLabel = new TextView(context);
        policyLabel.setText(R.string.sync_conflict_policy);
        policyLabel.setPadding(0, pad, 0, 0);
        layout.addView(policyLabel);
        Spinner policySpinner = new Spinner(context);
        String[] options = {"Ask every time", "Keep visual editor version",
                "Keep XML code version"};
        policySpinner.setAdapter(new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_dropdown_item, options));
        String current = policy.getConflictPolicy();
        if (SynchronizationPolicy.POLICY_KEEP_VISUAL.equals(current)) {
            policySpinner.setSelection(1);
        } else if (SynchronizationPolicy.POLICY_KEEP_CODE.equals(current)) {
            policySpinner.setSelection(2);
        }
        layout.addView(policySpinner);

        CheckBox preserveUnknown = new CheckBox(context);
        preserveUnknown.setText(R.string.sync_preserve_unknown);
        preserveUnknown.setChecked(policy.isPreserveUnknownAttributes());
        layout.addView(preserveUnknown);

        new AlertDialog.Builder(context)
                .setTitle(R.string.sync_settings)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    policy.setAutoSync(autoSync.isChecked());
                    int selected = policySpinner.getSelectedItemPosition();
                    policy.setConflictPolicy(selected == 1
                            ? SynchronizationPolicy.POLICY_KEEP_VISUAL
                            : selected == 2
                            ? SynchronizationPolicy.POLICY_KEEP_CODE
                            : SynchronizationPolicy.POLICY_ASK);
                    policy.setPreserveUnknownAttributes(
                            preserveUnknown.isChecked());
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
