package com.ericsson.cifwk.tdm.api.model;

public class ApplicationInfo {
    private String version;
    private String name;
    private String artifactId;

    public ApplicationInfo() {
        //NO SONAR
    }

    public ApplicationInfo(String version, String name, String artifactId) {
        this.version = version;
        this.name = name;
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }
}
