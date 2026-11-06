package com.nobi.appbuilder.preview.events;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.nobi.appbuilder.model.ComponentModel;
import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.PreviewContext;

/**
 * Wires a component's EventModels onto its real Android View:
 * click, long click and text-changed. Only actions the executor
 * supports are attached; anything else was already flagged by the
 * validator and is ignored here. EditText input is also mirrored
 * into the session's temporary input values.
 */
public class PreviewEventDispatcher {

    private final PreviewActionExecutor executor;

    public PreviewEventDispatcher(PreviewActionExecutor executor) {
        this.executor = executor;
    }

    public PreviewActionExecutor getExecutor() {
        return executor;
    }

    public void attach(View view, ComponentModel model, PreviewContext context) {
        if (view == null || model == null || context == null) {
            return;
        }
        for (EventModel event : model.getEvents()) {
            if (event == null || !executor.isSupported(event.getAction())) {
                continue;
            }
            String trigger = event.getEventType();
            if (EventModel.EVENT_CLICK.equals(trigger)) {
                view.setClickable(true);
                view.setOnClickListener(v -> executor.execute(event, v, context));
            } else if (EventModel.EVENT_LONG_CLICK.equals(trigger)) {
                view.setLongClickable(true);
                view.setOnLongClickListener(v -> {
                    executor.execute(event, v, context);
                    return true;
                });
            } else if (EventModel.EVENT_TEXT_CHANGED.equals(trigger)
                    && view instanceof EditText) {
                EditText editText = (EditText) view;
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        context.getSession().putInputValue(model.getId(),
                                s == null ? "" : s.toString());
                        executor.execute(event, editText, context);
                    }
                });
            }
        }
    }
}
