package com.ericsson.cifwk.tdm.application.datasources;

public class VersionCondtion {

    private String major;

    private String minor;

    private String build;

    private String snapshot;

    public String getMinor() {
        return minor;
    }

    public void setMinor(final String minor) {
        this.minor = "'version.minor'" + minor;
    }

    public String getMajor() {
        return major;
    }

    public void setMajor(final String major) {
        this.major = "'version.major'" + major;
    }

    public String getBuild() {
        return build;
    }

    public void setBuild(final String build) {
        this.build = "'version.build'" + build;
    }

    public String getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(final String snapshot) {
        this.snapshot = "'version.snapshot'" + snapshot;
    }

    @Override
    public String toString() {
        if (this.snapshot != null) {
            return "{" + this.major + this.minor + this.build + this.snapshot + "}";
        }
        return "{" + this.major + this.minor + this.build + "}";
    }
}
