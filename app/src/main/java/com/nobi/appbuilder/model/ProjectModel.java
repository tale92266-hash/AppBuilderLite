package com.nobi.appbuilder.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Project metadata (stored relationally in the projects table) plus its
 * screens (stored in the screens table, carried here for JSON import/export).
 * JSON is the single source of truth for a whole project document:
 * schemaVersion lets Phase 2 migrations upgrade old documents.
 */
public class ProjectModel {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    private static final Pattern PACKAGE_PATTERN =
            Pattern.compile("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)+$");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private String id;
    private String appName;
    private String packageName;
    private int minSdk;
    private int schemaVersion;
    private long createdAt;
    private long updatedAt;
    private String thumbnailPath;
    private List<ScreenModel> screens;
    /** Not persisted: filled in by the dashboard from the screens table. */
    private transient int screenCount;

    public ProjectModel() {
        schemaVersion = CURRENT_SCHEMA_VERSION;
        screens = new ArrayList<>();
    }

    public ProjectModel(String id, String appName, String packageName, int minSdk) {
        this();
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.minSdk = minSdk;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getMinSdk() {
        return minSdk;
    }

    public void setMinSdk(int minSdk) {
        this.minSdk = minSdk;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public int getScreenCount() {
        return screenCount;
    }

    public void setScreenCount(int screenCount) {
        this.screenCount = screenCount;
    }

    public List<ScreenModel> getScreens() {
        return screens;
    }

    public void setScreens(List<ScreenModel> screens) {
        this.screens = screens == null ? new ArrayList<>() : screens;
    }

    /** First letters of the first two words, for the card thumbnail. */
    public String getInitials() {
        if (appName == null || appName.trim().isEmpty()) {
            return "?";
        }
        String[] words = appName.trim().split("\\s+");
        StringBuilder initials = new StringBuilder();
        for (int i = 0; i < Math.min(2, words.length); i++) {
            if (!words[i].isEmpty()) {
                initials.append(Character.toUpperCase(words[i].charAt(0)));
            }
        }
        return initials.toString();
    }

    public static boolean isValidPackageName(String packageName) {
        return packageName != null && PACKAGE_PATTERN.matcher(packageName.trim()).matches();
    }

    public static boolean isValidAppName(String appName) {
        return appName != null && !appName.trim().isEmpty() && appName.trim().length() <= 50;
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    /** Parses a project document; returns null when the JSON is invalid. */
    public static ProjectModel fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }
        try {
            ProjectModel project = GSON.fromJson(json, ProjectModel.class);
            if (project == null || !isValidAppName(project.appName)
                    || !isValidPackageName(project.packageName)) {
                return null;
            }
            if (project.screens == null) {
                project.screens = new ArrayList<>();
            }
            return project;
        } catch (Exception e) {
            return null;
        }
    }
}
