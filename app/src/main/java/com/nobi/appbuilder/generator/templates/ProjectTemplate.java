package com.nobi.appbuilder.generator.templates;

/**
 * The one place that pins the generated project's toolchain. These
 * are the exact versions AppBuilder Lite itself builds with, so the
 * combination is proven by our own CI instead of guessed.
 */
public final class ProjectTemplate {

    private ProjectTemplate() {
    }

    public static final String AGP_VERSION = "8.9.2";
    public static final String GRADLE_VERSION = "8.11.1";
    public static final int COMPILE_SDK = 35;
    public static final int DEFAULT_MIN_SDK = 26;
    public static final int DEFAULT_TARGET_SDK = 35;
    public static final String DEFAULT_VERSION_NAME = "1.0.0";
    public static final int DEFAULT_VERSION_CODE = 1;

    public static final String DEP_APPCOMPAT = "androidx.appcompat:appcompat:1.7.0";
    public static final String DEP_MATERIAL = "com.google.android.material:material:1.12.0";
    public static final String DEP_CONSTRAINTLAYOUT =
            "androidx.constraintlayout:constraintlayout:2.1.4";
    public static final String DEP_ACTIVITY = "androidx.activity:activity:1.9.3";
    public static final String DEP_CORE = "androidx.core:core:1.13.1";
}
