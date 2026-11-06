package com.nobi.appbuilder.export;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates the Android Studio directory tree inside a workspace.
 * Returns the key directories so callers never rebuild paths by hand.
 */
public class ProjectStructureBuilder {

    public static class Structure {
        public final File root;
        public final File appDir;
        public final File javaDir;
        public final File resDir;
        public final File layoutDir;
        public final File drawableDir;
        public final File valuesDir;
        public final File mipmapAnydpiDir;
        public final File xmlDir;
        public final File gradleWrapperDir;

        Structure(File root, File appDir, File javaDir, File resDir,
                  File layoutDir, File drawableDir, File valuesDir,
                  File mipmapAnydpiDir, File xmlDir, File gradleWrapperDir) {
            this.root = root;
            this.appDir = appDir;
            this.javaDir = javaDir;
            this.resDir = resDir;
            this.layoutDir = layoutDir;
            this.drawableDir = drawableDir;
            this.valuesDir = valuesDir;
            this.mipmapAnydpiDir = mipmapAnydpiDir;
            this.xmlDir = xmlDir;
            this.gradleWrapperDir = gradleWrapperDir;
        }
    }

    /** Creates every standard directory; returns null on failure. */
    public Structure createStructure(File workspaceRoot, String packageName) {
        File appDir = new File(workspaceRoot, "app");
        File mainDir = new File(appDir, "src/main");
        File javaDir = new File(new File(mainDir, "java"),
                packageName.replace('.', '/'));
        File resDir = new File(mainDir, "res");
        File layoutDir = new File(resDir, "layout");
        File drawableDir = new File(resDir, "drawable");
        File valuesDir = new File(resDir, "values");
        File mipmapAnydpiDir = new File(resDir, "mipmap-anydpi-v26");
        File xmlDir = new File(resDir, "xml");
        File gradleWrapperDir = new File(new File(workspaceRoot, "gradle"),
                "wrapper");
        File[] dirs = {appDir, mainDir, javaDir, resDir, layoutDir,
                drawableDir, valuesDir, mipmapAnydpiDir, xmlDir,
                gradleWrapperDir};
        for (File dir : dirs) {
            if (!dir.exists() && !dir.mkdirs()) {
                return null;
            }
        }
        return new Structure(workspaceRoot, appDir, javaDir, resDir,
                layoutDir, drawableDir, valuesDir, mipmapAnydpiDir, xmlDir,
                gradleWrapperDir);
    }

    /** Standard empty directories, for the ZIP entry list. */
    public Map<String, String> emptyDirectories() {
        Map<String, String> dirs = new LinkedHashMap<>();
        dirs.put("app/src/main/res/xml/", "res/xml");
        dirs.put("app/src/main/res/mipmap-hdpi/", "mipmap-hdpi");
        dirs.put("app/src/main/res/mipmap-mdpi/", "mipmap-mdpi");
        dirs.put("app/src/main/res/mipmap-xhdpi/", "mipmap-xhdpi");
        dirs.put("app/src/main/res/mipmap-xxhdpi/", "mipmap-xxhdpi");
        dirs.put("app/src/main/res/mipmap-xxxhdpi/", "mipmap-xxxhdpi");
        return dirs;
    }
}
