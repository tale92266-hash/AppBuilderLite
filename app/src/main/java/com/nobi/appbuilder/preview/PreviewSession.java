package com.nobi.appbuilder.preview;

import com.nobi.appbuilder.preview.device.PreviewDeviceProfile;
import com.nobi.appbuilder.preview.theme.PreviewThemeManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * One preview run: which project and screen it started on, the
 * chosen theme / device / orientation, the navigation history and
 * the temporary input values typed into preview EditTexts.
 *
 * Nothing here is written back to the design JSON. Input values live
 * only for the life of the session and are dropped when preview
 * closes, so previewing can never silently change a project.
 */
public class PreviewSession {

    public static final String ORIENTATION_PORTRAIT = "portrait";
    public static final String ORIENTATION_LANDSCAPE = "landscape";

    private final String projectId;
    private final String initialScreenId;
    private String currentScreenId;
    private int themeMode = PreviewThemeManager.MODE_CUSTOM;
    private PreviewDeviceProfile deviceProfile;
    private String orientation = ORIENTATION_PORTRAIT;

    private final Deque<String> history = new ArrayDeque<>();
    private final Map<String, String> inputValues = new HashMap<>();

    public PreviewSession(String projectId, String initialScreenId,
                          PreviewDeviceProfile deviceProfile) {
        this.projectId = projectId;
        this.initialScreenId = initialScreenId;
        this.currentScreenId = initialScreenId;
        this.deviceProfile = deviceProfile;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getInitialScreenId() {
        return initialScreenId;
    }

    public String getCurrentScreenId() {
        return currentScreenId;
    }

    public void setCurrentScreenId(String currentScreenId) {
        this.currentScreenId = currentScreenId;
    }

    public int getThemeMode() {
        return themeMode;
    }

    public void setThemeMode(int themeMode) {
        this.themeMode = themeMode;
    }

    public PreviewDeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    public void setDeviceProfile(PreviewDeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(String orientation) {
        if (ORIENTATION_LANDSCAPE.equals(orientation)) {
            this.orientation = ORIENTATION_LANDSCAPE;
        } else {
            this.orientation = ORIENTATION_PORTRAIT;
        }
    }

    public boolean isLandscape() {
        return ORIENTATION_LANDSCAPE.equals(orientation);
    }

    public void toggleOrientation() {
        setOrientation(isLandscape() ? ORIENTATION_PORTRAIT : ORIENTATION_LANDSCAPE);
    }

    /** Pushes the screen we are leaving so Back can return to it. */
    public void pushHistory(String screenId) {
        if (screenId != null) {
            history.push(screenId);
        }
    }

    public String popHistory() {
        return history.isEmpty() ? null : history.pop();
    }

    public boolean canGoBack() {
        return !history.isEmpty();
    }

    public void clearHistory() {
        history.clear();
    }

    public List<String> getHistorySnapshot() {
        return new ArrayList<>(history);
    }

    public void restoreHistory(List<String> snapshot) {
        history.clear();
        if (snapshot != null) {
            for (int i = snapshot.size() - 1; i >= 0; i--) {
                history.push(snapshot.get(i));
            }
        }
    }

    /** Temporary EditText content, keyed by component id. */
    public void putInputValue(String componentId, String value) {
        if (componentId != null) {
            inputValues.put(componentId, value == null ? "" : value);
        }
    }

    public String getInputValue(String componentId) {
        return inputValues.get(componentId);
    }
}
