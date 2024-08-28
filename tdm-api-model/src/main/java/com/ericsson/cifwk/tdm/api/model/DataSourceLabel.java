package com.ericsson.cifwk.tdm.api.model;

import javax.validation.constraints.NotNull;

/**
 * Created by egergle on 30/08/2017.
 */
public class DataSourceLabel {

    private String id;

    @NotNull
    private String name;

    @NotNull
    private String version;

    @NotNull
    private String dataSourceId;

    @NotNull
    private String contextId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setContextId(final String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }
}
