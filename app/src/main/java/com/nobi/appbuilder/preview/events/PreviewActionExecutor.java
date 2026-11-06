package com.nobi.appbuilder.preview.events;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.nobi.appbuilder.model.EventModel;
import com.nobi.appbuilder.preview.PreviewContext;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Executes the small fixed set of preview actions. Only these
 * predefined actions ever run: toast, navigate, changeText,
 * validateInput, finish and changeVisibility. Nothing from a project
 * document is ever executed as code or as a shell command.
 *
 * Targets: navigate uses the event's targetScreenId; changeText,
 * validateInput and changeVisibility act on the component the event
 * belongs to (the view that fired it).
 */
public class PreviewActionExecutor {

    public static final String ACTION_TOAST = "toast";
    public static final String ACTION_SHOW_TOAST = "showToast";
    public static final String ACTION_NAVIGATE = "navigate";
    public static final String ACTION_CHANGE_TEXT = "changeText";
    public static final String ACTION_VALIDATE_INPUT = "validateInput";
    public static final String ACTION_FINISH = "finish";
    public static final String ACTION_CHANGE_VISIBILITY = "changeVisibility";
    public static final String ACTION_NONE = "none";

    private static final Set<String> SUPPORTED = new HashSet<>(Arrays.asList(
            ACTION_TOAST, ACTION_SHOW_TOAST, ACTION_NAVIGATE,
            ACTION_CHANGE_TEXT, ACTION_VALIDATE_INPUT, ACTION_FINISH,
            ACTION_CHANGE_VISIBILITY, ACTION_NONE));

    public boolean isSupported(String action) {
        return action != null && SUPPORTED.contains(action);
    }

    public void execute(EventModel event, View source, PreviewContext context) {
        if (event == null || source == null || context == null) {
            return;
        }
        String action = event.getAction();
        if (!isSupported(action)) {
            return;
        }
        Activity activity = context.getActivity();
        switch (action) {
            case ACTION_TOAST:
            case ACTION_SHOW_TOAST: {
                if (activity == null) {
                    return;
                }
                String message = event.getMessage();
                Toast.makeText(activity,
                        message == null || message.isEmpty() ? "Done" : message,
                        Toast.LENGTH_SHORT).show();
                break;
            }
            case ACTION_NAVIGATE: {
                String target = event.getTargetScreenId();
                if (target == null || context.getNavigator() == null
                        || !context.getNavigator().openScreen(target)) {
                    if (activity != null) {
                        Toast.makeText(activity, "Screen not found",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case ACTION_CHANGE_TEXT: {
                if (source instanceof TextView && event.getMessage() != null) {
                    ((TextView) source).setText(event.getMessage());
                }
                break;
            }
            case ACTION_VALIDATE_INPUT: {
                if (activity == null) {
                    return;
                }
                if (source instanceof EditText) {
                    String text = ((EditText) source).getText().toString().trim();
                    if (text.isEmpty()) {
                        String message = event.getMessage();
                        Toast.makeText(activity,
                                message == null || message.isEmpty()
                                        ? "Please enter a value" : message,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "Valid",
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case ACTION_FINISH: {
                if (activity != null) {
                    activity.finish();
                }
                break;
            }
            case ACTION_CHANGE_VISIBILITY: {
                String wanted = event.getMessage();
                if ("gone".equalsIgnoreCase(wanted)) {
                    source.setVisibility(View.GONE);
                } else if ("invisible".equalsIgnoreCase(wanted)) {
                    source.setVisibility(View.INVISIBLE);
                } else if ("visible".equalsIgnoreCase(wanted)) {
                    source.setVisibility(View.VISIBLE);
                } else {
                    source.setVisibility(source.getVisibility() == View.VISIBLE
                            ? View.GONE : View.VISIBLE);
                }
                break;
            }
            case ACTION_NONE:
            default:
                break;
        }
    }
}
