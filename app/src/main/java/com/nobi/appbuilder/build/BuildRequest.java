package com.nobi.appbuilder.build;

/** One queued build: project + configuration + id. */
public class BuildRequest {

    private final String buildId;
    private final String projectId;
    private final String projectName;
    private final BuildConfiguration configuration;

    public BuildRequest(String buildId, String projectId, String projectName,
                        BuildConfiguration configuration) {
        this.buildId = buildId;
        this.projectId = projectId;
        this.projectName = projectName;
        this.configuration = configuration;
    }

    public String getBuildId() {
        return buildId;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public BuildConfiguration getConfiguration() {
        return configuration;
    }
}
