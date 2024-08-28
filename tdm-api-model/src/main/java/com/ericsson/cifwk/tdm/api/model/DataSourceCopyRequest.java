package com.ericsson.cifwk.tdm.api.model;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 * Date: 09/05/2016
 */

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceCopyRequest implements HasContextId {

    @NotNull
    private String dataSourceId;

    @NotNull
    private String version;

    private boolean baseVersion;

    private String newName;

    private String newContextId;

    private String newGroup;

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    public String getNewContextId() {
        return newContextId;
    }

    public void setNewContextId(String newContextId) {
        this.newContextId = newContextId;
    }

    public String getNewGroup() {
        return newGroup;
    }

    public void setNewGroup(String newGroup) {
        this.newGroup = newGroup;
    }

    @Override
    public String getContextId() {
        return getNewContextId();
    }

    public boolean isBaseVersion() {
        return baseVersion;
    }

    public void setBaseVersion(final boolean baseVersion) {
        this.baseVersion = baseVersion;
    }
}
