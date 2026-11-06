package com.nobi.appbuilder.actions;

/**
 * The twelve built-in actions and their categories. The ids are the
 * same strings the Phase 7 EventMappingRegistry uses, so preview,
 * generator and workflow all mean the same thing by one action.
 */
public final class ActionType {

    public static final String CATEGORY_NAVIGATION = "Navigation";
    public static final String CATEGORY_FEEDBACK = "Feedback";
    public static final String CATEGORY_UI = "UI";
    public static final String CATEGORY_DATA = "Data";
    public static final String CATEGORY_CONTROL = "Control";

    public static final String NAVIGATE = "navigate";
    public static final String SHOW_TOAST = "showToast";
    public static final String CHANGE_TEXT = "changeText";
    public static final String SET_VISIBILITY = "setVisibility";
    public static final String SET_ENABLED = "setEnabled";
    public static final String OPEN_URL = "openUrl";
    public static final String FINISH_SCREEN = "finishActivity";
    public static final String VALIDATE_FORM = "validateInput";
    public static final String SET_VARIABLE = "setVariable";
    public static final String SHOW_DIALOG = "showDialog";
    public static final String DELAY = "delay";
    public static final String CONDITIONAL = "conditional";

    public static final String[] ALL = {
            NAVIGATE, SHOW_TOAST, CHANGE_TEXT, SET_VISIBILITY, SET_ENABLED,
            OPEN_URL, FINISH_SCREEN, VALIDATE_FORM, SET_VARIABLE,
            SHOW_DIALOG, DELAY, CONDITIONAL
    };

    private ActionType() {
    }

    public static String categoryFor(String actionId) {
        if (actionId == null) {
            return CATEGORY_CONTROL;
        }
        switch (actionId) {
            case NAVIGATE:
            case FINISH_SCREEN:
            case OPEN_URL:
                return CATEGORY_NAVIGATION;
            case SHOW_TOAST:
            case SHOW_DIALOG:
                return CATEGORY_FEEDBACK;
            case CHANGE_TEXT:
            case SET_VISIBILITY:
            case SET_ENABLED:
            case VALIDATE_FORM:
                return CATEGORY_UI;
            case SET_VARIABLE:
                return CATEGORY_DATA;
            case DELAY:
            case CONDITIONAL:
            default:
                return CATEGORY_CONTROL;
        }
    }
}
