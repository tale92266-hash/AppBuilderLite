package com.nobi.appbuilder.utils;

import android.content.Context;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Internal-storage file helpers. All project data lives in internal
 * storage; nothing here touches shared storage, so no permission is needed.
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static File getProjectsDir(Context context) {
        File dir = new File(context.getFilesDir(), "projects");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getProjectDir(Context context, String projectId) {
        File dir = new File(getProjectsDir(context), projectId);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getExportsDir(Context context) {
        File dir = new File(context.getExternalFilesDir(null), "exports");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static void writeTextFile(File file, String content) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (OutputStream out = new FileOutputStream(file)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String readTextFile(File file) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            return readStreamToString(in);
        }
    }

    public static String readStreamToString(InputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

    /** Deletes a file or directory recursively. Returns true on success. */
    public static boolean deleteRecursive(File file) {
        if (file == null || !file.exists()) {
            return true;
        }
        boolean ok = true;
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    ok = deleteRecursive(child) && ok;
                }
            }
        }
        return file.delete() && ok;
    }

    public static String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.1f KB", bytes / 1024.0);
        } else {
            return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
        }
    }
}
