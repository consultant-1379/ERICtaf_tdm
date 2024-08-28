package com.ericsson.cifwk.tdm.api.model;

public final class DataSourceCopyRequestBuilder {

    private String dataSourceId;
    private String version;
    private String newName;
    private String newContextId;
    private String newGroup;
    private boolean baseVersion;

    private DataSourceCopyRequestBuilder() {
    }

    public static DataSourceCopyRequestBuilder aDataSourceCopyRequest() {
        return new DataSourceCopyRequestBuilder();
    }

    public DataSourceCopyRequestBuilder withDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
        return this;
    }

    public DataSourceCopyRequestBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public DataSourceCopyRequestBuilder withNewName(String newName) {
        this.newName = newName;
        return this;
    }

    public DataSourceCopyRequestBuilder withNewContextId(String newContextId) {
        this.newContextId = newContextId;
        return this;
    }

    public DataSourceCopyRequestBuilder withNewGroup(String newGroup) {
        this.newGroup = newGroup;
        return this;
    }

    public DataSourceCopyRequestBuilder withBaseVersion(Boolean baseVersion) {
        this.baseVersion = baseVersion;
        return this;
    }

    public DataSourceCopyRequest build() {
        DataSourceCopyRequest dataSourceCopyRequest = new DataSourceCopyRequest();
        dataSourceCopyRequest.setDataSourceId(dataSourceId);
        dataSourceCopyRequest.setVersion(version);
        dataSourceCopyRequest.setNewName(newName);
        dataSourceCopyRequest.setNewContextId(newContextId);
        dataSourceCopyRequest.setNewGroup(newGroup);
        dataSourceCopyRequest.setBaseVersion(baseVersion);
        return dataSourceCopyRequest;
    }
}
