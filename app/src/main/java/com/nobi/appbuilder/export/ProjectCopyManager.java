package com.nobi.appbuilder.export;

import android.content.Context;

import com.nobi.appbuilder.generator.GenerationResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Copies the Phase 7 generation output plus the Android Studio
 * extras into the workspace: wrapper scripts + genuine wrapper jar
 * (from app assets), proguard rules, styles.xml, adaptive launcher
 * icon and the generated README. Nothing is silently discarded —
 * every skipped file is reported.
 */
public class ProjectCopyManager {

    private static final int BUFFER_SIZE = 8192;

    private final Context appContext;
    private final ProjectFileManager files;

    public ProjectCopyManager(Context context, ProjectFileManager files) {
        this.appContext = context.getApplicationContext();
        this.files = files;
    }

    /** Copies every generated file; returns skipped/problem paths. */
    public List<String> copyGeneratedFiles(GenerationResult result) {
        List<String> problems = new ArrayList<>();
        for (Map.Entry<String, String> entry
                : result.getTextFiles().entrySet()) {
            try {
                files.writeText(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                problems.add(entry.getKey() + ": " + e.getMessage());
            }
        }
        for (Map.Entry<String, byte[]> entry
                : result.getBinaryFiles().entrySet()) {
            try {
                files.writeBytes(entry.getKey(), entry.getValue());
            } catch (IOException e) {
                problems.add(entry.getKey() + ": " + e.getMessage());
            }
        }
        return problems;
    }

    /**
     * Copies the genuine Gradle wrapper from app assets. Returns null
     * when the jar asset is missing — the caller must report that
     * clearly instead of writing a fake jar.
     */
    public String copyWrapperFiles(boolean includeWrapper) {
        if (!includeWrapper) {
            return "Gradle wrapper excluded by export configuration";
        }
        try {
            copyAsset("export/gradlew", "gradlew");
            copyAsset("export/gradlew.bat", "gradlew.bat");
        } catch (IOException e) {
            return "Wrapper scripts could not be copied: " + e.getMessage();
        }
        try {
            copyAsset("export/gradle-wrapper.jar",
                    "gradle/wrapper/gradle-wrapper.jar");
        } catch (IOException e) {
            return "Gradle wrapper JAR is missing from the app assets — "
                    + "run `gradle wrapper` once in Android Studio to "
                    + "generate it; no fake JAR was created.";
        }
        return null;
    }

    /** Standard proguard rules file for the generated project. */
    public void copyProguardRules() throws IOException {
        files.writeText("app/proguard-rules.pro",
                "# Add project specific ProGuard rules here.\n"
                        + "# You can control the set of applied configuration files using the\n"
                        + "# proguardFiles setting in build.gradle.\n"
                        + "#\n"
                        + "# For more details, see\n"
                        + "#   http://developer.android.com/guide/developing/tools/proguard.html\n"
                        + "\n"
                        + "# If your project uses WebView with JS, uncomment the following\n"
                        + "# and specify the fully qualified class name to the JavaScript interface\n"
                        + "# class:\n"
                        + "#-keepclassmembers class fqcn.of.javascript.interface.for.webview {\n"
                        + "#   public *;\n"
                        + "#}\n");
    }

    /** styles.xml: one extra style derived from the generated AppTheme. */
    public void copyStylesXml() throws IOException {
        files.writeText("app/src/main/res/values/styles.xml",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <!-- No-action-bar variant of the generated AppTheme. -->\n"
                        + "    <style name=\"AppTheme.NoActionBar\" "
                        + "parent=\"AppTheme\">\n"
                        + "        <item name=\"windowActionBar\">false</item>\n"
                        + "        <item name=\"windowNoTitle\">true</item>\n"
                        + "    </style>\n"
                        + "</resources>\n");
    }

    /** Adaptive launcher icon so the @mipmap/ic_launcher reference resolves. */
    public void copyLauncherIcon() throws IOException {
        String iconXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                + "<adaptive-icon "
                + "xmlns:android=\"http://schemas.android.com/apk/res/android\">\n"
                + "    <background android:drawable=\"@color/ic_launcher_background\" />\n"
                + "    <foreground android:drawable=\"@drawable/ic_launcher_foreground\" />\n"
                + "</adaptive-icon>\n";
        files.writeText("app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml",
                iconXml);
        files.writeText(
                "app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml",
                iconXml);
        files.writeText(
                "app/src/main/res/drawable/ic_launcher_foreground.xml",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<vector xmlns:android=\"http://schemas.android.com/apk/res/android\"\n"
                        + "    android:width=\"108dp\"\n"
                        + "    android:height=\"108dp\"\n"
                        + "    android:viewportWidth=\"108\"\n"
                        + "    android:viewportHeight=\"108\">\n"
                        + "    <path\n"
                        + "        android:fillColor=\"#FFFFFF\"\n"
                        + "        android:pathData=\"M34,34h40v40H34z\" />\n"
                        + "</vector>\n");
        files.writeText("app/src/main/res/values/ic_launcher_colors.xml",
                "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
                        + "<resources>\n"
                        + "    <color name=\"ic_launcher_background\">#0B4499</color>\n"
                        + "</resources>\n");
    }

    public void copyReadme(String readmeContent) throws IOException {
        files.writeText("README.md", readmeContent);
    }

    private void copyAsset(String assetPath, String targetPath)
            throws IOException {
        try (InputStream in = appContext.getAssets().open(assetPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            java.io.ByteArrayOutputStream out =
                    new java.io.ByteArrayOutputStream();
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
            files.writeBytes(targetPath, out.toByteArray());
        }
    }
}
