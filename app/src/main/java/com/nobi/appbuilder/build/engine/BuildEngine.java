package com.nobi.appbuilder.build.engine;

import com.nobi.appbuilder.build.BuildCancellationManager;
import com.nobi.appbuilder.build.BuildEnvironmentInfo;
import com.nobi.appbuilder.build.BuildRequest;
import com.nobi.appbuilder.build.BuildResult;
import com.nobi.appbuilder.build.BuildTask;
import com.nobi.appbuilder.build.logs.BuildLogManager;

/** One build strategy: local Gradle or external ZIP export. */
public interface BuildEngine {

    interface EngineListener {
        void onTask(BuildTask task);

        void onProgress(int percent);
    }

    /** True when this engine can run in the detected environment. */
    boolean supports(BuildEnvironmentInfo environment);

    /** Runs the build to a terminal result. Never returns a SUCCESS
     * without a real verified APK (local) or a real verified ZIP
     * (external). */
    BuildResult execute(BuildRequest request, BuildLogManager logManager,
                        BuildCancellationManager cancellation,
                        EngineListener listener);
}
