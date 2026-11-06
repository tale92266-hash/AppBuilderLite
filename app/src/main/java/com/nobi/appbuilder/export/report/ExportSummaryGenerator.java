package com.nobi.appbuilder.export.report;

import com.nobi.appbuilder.export.ProjectPackagingConfig;
import com.nobi.appbuilder.generator.GenerationResult;
import com.nobi.appbuilder.generator.templates.ProjectTemplate;

/**
 * Generates the README.md shipped inside every exported Android
 * Studio project: app facts, structure, build + import instructions,
 * and an honest note that the export itself was not build-tested.
 */
public class ExportSummaryGenerator {

    public static String generate(ProjectPackagingConfig config,
                                  GenerationResult result) {
        StringBuilder out = new StringBuilder();
        out.append("# ").append(config.getAppName()).append("\n\n");
        if (config.getDescription() != null
                && !config.getDescription().trim().isEmpty()) {
            out.append(config.getDescription().trim()).append("\n\n");
        }
        out.append("Generated with AppBuilder Lite.\n\n");
        out.append("## Project\n\n");
        out.append("- Application ID: `").append(config.getPackageName())
                .append("`\n");
        out.append("- Version: ").append(config.getVersionName())
                .append(" (").append(config.getVersionCode()).append(")\n");
        out.append("- Minimum SDK: ").append(config.getMinSdk()).append("\n");
        out.append("- Target SDK: ").append(config.getTargetSdk()).append("\n");
        out.append("- Compile SDK: ").append(ProjectTemplate.COMPILE_SDK)
                .append("\n");
        out.append("- Android Gradle Plugin: ")
                .append(ProjectTemplate.AGP_VERSION).append("\n");
        out.append("- Gradle: ").append(ProjectTemplate.GRADLE_VERSION)
                .append("\n");
        out.append("- Files: ").append(result.getAllPaths().size())
                .append(" (").append(result.getReport().getScreensGenerated())
                .append(" screens, ")
                .append(result.getReport().getComponentsGenerated())
                .append(" components)\n\n");
        out.append("## Structure\n\n");
        out.append("```\n");
        out.append(config.getProjectName()).append("/\n");
        out.append("├── settings.gradle\n");
        out.append("├── build.gradle\n");
        out.append("├── gradle.properties\n");
        out.append("├── gradlew\n");
        out.append("├── gradlew.bat\n");
        out.append("├── gradle/wrapper/\n");
        out.append("├── app/\n");
        out.append("│   ├── build.gradle\n");
        out.append("│   ├── proguard-rules.pro\n");
        out.append("│   └── src/main/\n");
        out.append("│       ├── AndroidManifest.xml\n");
        out.append("│       ├── java/<package>/\n");
        out.append("│       └── res/\n");
        out.append("└── README.md\n");
        out.append("```\n\n");
        out.append("## Build\n\n");
        out.append("Requires Android Studio (or a JDK 17 + Android SDK 35\n");
        out.append("setup) with the SDK platforms installed.\n\n");
        out.append("```\n");
        out.append("./gradlew assembleDebug\n");
        out.append("```\n\n");
        out.append("Windows:\n\n");
        out.append("```\n");
        out.append("gradlew.bat assembleDebug\n");
        out.append("```\n\n");
        out.append("If `gradle-wrapper.jar` is missing, run `gradle wrapper`\n");
        out.append("once inside the project to generate it.\n\n");
        out.append("## Import into Android Studio\n\n");
        out.append("1. Open Android Studio.\n");
        out.append("2. Choose **Open** and select this project folder.\n");
        out.append("3. Let Gradle sync finish.\n");
        out.append("4. Run the app on a device or emulator.\n\n");
        out.append("## Notes\n\n");
        out.append("- This project was generated from a visual design; the\n");
        out.append("  export was structurally validated but not compiled\n");
        out.append("  inside AppBuilder Lite, so run the build above to\n");
        out.append("  verify it in Android Studio.\n");
        return out.toString();
    }
}
