package com.nobi.appbuilder.build;

/** The ten build stages. Only stages supported by real build output
 * are shown with progress; anything else stays indeterminate. */
public enum BuildTask {
    CHECK_ENVIRONMENT("Checking build environment"),
    VALIDATE_PROJECT("Validating project"),
    PREPARE_WORKSPACE("Preparing build workspace"),
    RESOLVE_DEPENDENCIES("Resolving dependencies"),
    COMPILE_JAVA("Compiling Java sources"),
    PROCESS_RESOURCES("Processing Android resources"),
    MERGE_MANIFEST("Merging manifest"),
    PACKAGE_APK("Packaging APK"),
    VERIFY_APK("Verifying APK"),
    COMPLETED("Build completed");

    private final String label;

    BuildTask(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
