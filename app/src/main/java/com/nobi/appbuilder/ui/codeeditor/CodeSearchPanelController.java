package com.nobi.appbuilder.ui.codeeditor;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.nobi.appbuilder.R;
import com.nobi.appbuilder.codeeditor.search.FindReplaceController;

/** Find & replace bar above the bottom panel. */
public class CodeSearchPanelController {

    public interface SearchListener {
        void onMatchChanged();
        void onReplaceCurrent();
        void onReplaceAll();
    }

    private final FindReplaceController findReplace;
    private final EditText findInput;
    private final EditText replaceInput;
    private final TextView countView;
    private final SearchListener listener;
    private String lastText = "";

    public CodeSearchPanelController(View root,
                                     FindReplaceController findReplace,
                                     SearchListener listener) {
        this.findReplace = findReplace;
        this.listener = listener;
        findInput = root.findViewById(R.id.input_code_find);
        replaceInput = root.findViewById(R.id.input_code_replace);
        countView = root.findViewById(R.id.text_code_match_count);
        CheckBox caseBox = root.findViewById(R.id.check_code_case);
        findInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                refreshQuery();
                listener.onMatchChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        caseBox.setOnCheckedChangeListener((button, checked) -> {
            findReplace.getOptions().setCaseSensitive(checked);
            refreshQuery();
            listener.onMatchChanged();
        });
        root.findViewById(R.id.btn_code_find_prev).setOnClickListener(
                v -> {
                    findReplace.previous(lastText);
                    updateCount();
                    listener.onMatchChanged();
                });
        root.findViewById(R.id.btn_code_find_next).setOnClickListener(
                v -> {
                    findReplace.next(lastText);
                    updateCount();
                    listener.onMatchChanged();
                });
        root.findViewById(R.id.btn_code_replace).setOnClickListener(
                v -> listener.onReplaceCurrent());
        root.findViewById(R.id.btn_code_replace_all).setOnClickListener(
                v -> listener.onReplaceAll());
        root.findViewById(R.id.btn_code_search_close).setOnClickListener(
                v -> root.setVisibility(View.GONE));
    }

    public String getFindText() {
        return findInput.getText().toString();
    }

    public String getReplaceText() {
        return replaceInput.getText().toString();
    }

    public FindReplaceController getFindReplace() {
        return findReplace;
    }

    public void updateCount() {
        int total = findReplace.getMatchCount();
        countView.setText(total == 0 ? "No matches"
                : (findReplace.getCurrentIndex() + 1) + " of " + total);
    }

    public void setDocumentText(String text) {
        lastText = text == null ? "" : text;
        refreshQuery();
    }

    private void refreshQuery() {
        findReplace.setQuery(findInput.getText().toString(), lastText);
        updateCount();
    }
}
