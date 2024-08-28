package com.ericsson.cifwk.tdm.api.model;

import static com.google.common.base.MoreObjects.toStringHelper;

public class IntegrationsInfo {

    private final String tceAppUrl;

    public IntegrationsInfo(String tceAppUrl) {
        this.tceAppUrl = tceAppUrl;
    }

    public String getTceAppUrl() {
        return tceAppUrl;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("tceAppUrl", tceAppUrl)
                .toString();
    }
}
