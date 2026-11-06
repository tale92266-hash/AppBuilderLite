package com.nobi.appbuilder.build.cloud;

/**
 * The GitHub Actions workflow the app installs into the user's
 * private build repo. It is the same shape as AppBuilder Lite's
 * own build workflow (JDK 17, the runner's preinstalled Android
 * SDK, Gradle wrapper from the exported project) — only the
 * project comes from the uploaded project.zip instead of the repo
 * itself. No android-actions/setup-android step: it installs the
 * obsolete "tools" package, which no longer exists in the SDK
 * repository ("Failed to find package 'tools'", exit 1) — the
 * runner's own sdkmanager installs exactly platform-35 +
 * build-tools 35.0.0 instead. The debug keystore is a
 * FIXED debug key (assets/cloud/cloud-debug.keystore, conventional
 * "android" passwords), committed to the private repo so every
 * cloud APK is signed with the same key and installs over the
 * previous one. It is a debug key, not a release key, and anyone
 * can generate one — it must never be used for release signing.
 */
public final class CloudBuildWorkflow {

    private CloudBuildWorkflow() {
    }

    public static final String WORKFLOW_FILE =
            "appbuilder-cloud-build.yml";
    public static final String WORKFLOW_PATH =
            ".github/workflows/" + WORKFLOW_FILE;
    public static final String KEYSTORE_PATH =
            ".cloud/cloud-debug.keystore";
    public static final String KEYSTORE_ASSET =
            "cloud/cloud-debug.keystore";
    public static final String PROJECT_ZIP_PATH = "project.zip";
    public static final String ARTIFACT_NAME = "cloud-apk";
    public static final String APK_NAME = "app-debug.apk";

    public static String yaml() {
        return "name: AppBuilder Cloud Build\n"
                + "\n"
                + "on:\n"
                + "  workflow_dispatch:\n"
                + "\n"
                + "jobs:\n"
                + "  build:\n"
                + "    runs-on: ubuntu-latest\n"
                + "    steps:\n"
                + "      - name: Checkout\n"
                + "        uses: actions/checkout@v4\n"
                + "\n"
                + "      - name: Set up JDK 17\n"
                + "        uses: actions/setup-java@v4\n"
                + "        with:\n"
                + "          distribution: temurin\n"
                + "          java-version: \"17\"\n"
                + "\n"
                + "      - name: Install Android SDK packages\n"
                + "        run: |\n"
                + "          SDK=\"${ANDROID_HOME:-/usr/local/lib/android/sdk}\"\n"
                + "          yes | \"$SDK/cmdline-tools/latest/bin/sdkmanager\""
                + " --licenses\n"
                + "          \"$SDK/cmdline-tools/latest/bin/sdkmanager\""
                + " \"platforms;android-35\" \"build-tools;35.0.0\""
                + " \"platform-tools\"\n"
                + "\n"
                + "      - name: Unzip project\n"
                + "        run: unzip -o project.zip -d project\n"
                + "\n"
                + "      - name: Restore fixed debug keystore\n"
                + "        run: |\n"
                + "          mkdir -p ~/.android\n"
                + "          cp .cloud/cloud-debug.keystore "
                + "~/.android/debug.keystore\n"
                + "\n"
                + "      - name: Grant execute permission for gradlew\n"
                + "        run: chmod +x project/gradlew\n"
                + "\n"
                + "      - name: Build debug APK\n"
                + "        working-directory: project\n"
                + "        run: ./gradlew :app:assembleDebug --stacktrace\n"
                + "\n"
                + "      - name: Upload APK\n"
                + "        uses: actions/upload-artifact@v4\n"
                + "        with:\n"
                + "          name: cloud-apk\n"
                + "          path: "
                + "project/app/build/outputs/apk/debug/app-debug.apk\n";
    }
}
