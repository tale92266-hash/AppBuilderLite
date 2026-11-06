package com.nobi.appbuilder.generator.gradle;

import com.nobi.appbuilder.generator.GenerationContext;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.templates.ProjectTemplate;

/**
 * Build configuration for the generated project, pinned to the same
 * AGP / Gradle / SDK combination AppBuilder Lite itself builds with.
 * The wrapper JAR cannot be synthesized, so only
 * gradle-wrapper.properties is generated and the report warns that
 * `gradle wrapper` must be run once (or the jar copied in).
 */
public class GradleGenerator {

    private final GenerationContext context;

    public GradleGenerator(GenerationContext context) {
        this.context = context;
    }

    public void generateInto(GenerationResult result) {
        String appName = context.getProject().getAppName();
        String rootName = appName.replaceAll("[^A-Za-z0-9]", "");
        if (rootName.isEmpty()) {
            rootName = "GeneratedApp";
        }
        result.addTextFile("settings.gradle",
                "pluginManagement {\n"
                        + "    repositories {\n"
                        + "        google()\n"
                        + "        mavenCentral()\n"
                        + "        gradlePluginPortal()\n"
                        + "    }\n"
                        + "}\n"
                        + "dependencyResolutionManagement {\n"
                        + "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)\n"
                        + "    repositories {\n"
                        + "        google()\n"
                        + "        mavenCentral()\n"
                        + "    }\n"
                        + "}\n"
                        + "rootProject.name = \"" + rootName + "\"\n"
                        + "include ':app'\n");
        result.addTextFile("build.gradle",
                "// Top-level build file\n"
                        + "plugins {\n"
                        + "    id 'com.android.application' version '"
                        + ProjectTemplate.AGP_VERSION + "' apply false\n"
                        + "}\n");
        String pkg = context.getPackageName();
        result.addTextFile("app/build.gradle",
                "plugins {\n"
                        + "    id 'com.android.application'\n"
                        + "}\n"
                        + "\n"
                        + "android {\n"
                        + "    namespace '" + pkg + "'\n"
                        + "    compileSdk " + ProjectTemplate.COMPILE_SDK + "\n"
                        + "\n"
                        + "    defaultConfig {\n"
                        + "        applicationId \"" + pkg + "\"\n"
                        + "        minSdk " + context.getMinSdk() + "\n"
                        + "        targetSdk " + context.getTargetSdk() + "\n"
                        + "        versionCode " + context.getVersionCode() + "\n"
                        + "        versionName \"" + context.getVersionName()
                        + "\"\n"
                        + "    }\n"
                        + "\n"
                        + "    buildTypes {\n"
                        + "        release {\n"
                        + "            minifyEnabled false\n"
                        + "        }\n"
                        + "    }\n"
                        + "    compileOptions {\n"
                        + "        sourceCompatibility JavaVersion.VERSION_17\n"
                        + "        targetCompatibility JavaVersion.VERSION_17\n"
                        + "    }\n"
                        + "}\n"
                        + "\n"
                        + "dependencies {\n"
                        + "    implementation '" + ProjectTemplate.DEP_APPCOMPAT + "'\n"
                        + "    implementation '" + ProjectTemplate.DEP_MATERIAL + "'\n"
                        + "    implementation '" + ProjectTemplate.DEP_CONSTRAINTLAYOUT + "'\n"
                        + "    implementation '" + ProjectTemplate.DEP_ACTIVITY + "'\n"
                        + "    implementation '" + ProjectTemplate.DEP_CORE + "'\n"
                        + "}\n");
        result.addTextFile("gradle.properties",
                "org.gradle.jvmargs=-Xmx2048m\n"
                        + "android.useAndroidX=true\n"
                        + "android.nonTransitiveRClass=true\n");
        result.addTextFile("gradle/wrapper/gradle-wrapper.properties",
                "distributionBase=GRADLE_USER_HOME\n"
                        + "distributionPath=wrapper/dists\n"
                        + "distributionUrl=https\\://services.gradle.org/distributions/"
                        + "gradle-" + ProjectTemplate.GRADLE_VERSION
                        + "-bin.zip\n"
                        + "zipStoreBase=GRADLE_USER_HOME\n"
                        + "zipStorePath=wrapper/dists\n");
        context.addWarning("gradle", "gradle/wrapper/gradle-wrapper.properties",
                null, "gradle-wrapper.jar is not generated; run "
                        + "`gradle wrapper` once (or copy a wrapper jar) "
                        + "before building the exported project.");
    }
}
