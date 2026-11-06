package com.nobi.appbuilder.build;

/** What this device can genuinely do about compiling Android apps. */
public enum BuildCapability {
    /** Every required tool was found and validated. */
    LOCAL_BUILD_SUPPORTED,
    /** No local toolchain; export the ZIP and build in Android Studio. */
    EXTERNAL_BUILD_REQUIRED,
    /** Some tools found, but not a complete compatible set. */
    ENVIRONMENT_INCOMPLETE,
    /** This device cannot host a local build at all. */
    ENVIRONMENT_UNSUPPORTED
}
