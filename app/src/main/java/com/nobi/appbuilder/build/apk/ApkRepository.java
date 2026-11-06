package com.nobi.appbuilder.build.apk;

import android.content.Context;

import com.nobi.appbuilder.build.logs.BuildLogRepository;
import com.nobi.appbuilder.build.logs.BuildLogRepository.BuildRecord;
import com.nobi.appbuilder.database.DatabaseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** APKs a project has produced, from build history — files that
 * still exist on disk, with missing outputs reported, not hidden. */
public class ApkRepository {

    /** One known APK. */
    public static class ApkRecord {
        public File file;
        public String buildId;
        public String buildType;
        public long size;
        public long modifiedAt;
        public boolean exists;
    }

    private final Context appContext;

    public ApkRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public List<ApkRecord> getByProject(String projectId) {
        List<ApkRecord> out = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(appContext);
        try {
            for (BuildRecord record
                    : new BuildLogRepository(dbHelper).getByProject(projectId)) {
                if (record.apkPath == null) {
                    continue;
                }
                ApkRecord apk = new ApkRecord();
                apk.file = new File(record.apkPath);
                apk.buildId = record.buildId;
                apk.buildType = record.buildType;
                apk.size = record.apkSize;
                apk.exists = apk.file.exists();
                apk.modifiedAt = apk.exists ? apk.file.lastModified()
                        : record.endTime;
                out.add(apk);
            }
        } finally {
            dbHelper.close();
        }
        return out;
    }
}
