package com.ericsson.cifwk.tdm.model;

import static com.ericsson.cifwk.tdm.model.Version.INITIAL_VERSION;

public final class DataSourceLabelEntityBuilder {

    private String id;

    private String name;

    private String version;

    private String dataSourceId;

    private String contextId;

    private DataSourceLabelEntityBuilder() {
    }

    public static DataSourceLabelEntityBuilder aDataSourceLabelEntity() {
        return new DataSourceLabelEntityBuilder();
    }

    public DataSourceLabelEntityBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DataSourceLabelEntityBuilder withInitialVersion() {
        return withVersion(INITIAL_VERSION.toString());
    }

    public DataSourceLabelEntityBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public DataSourceLabelEntityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DataSourceLabelEntityBuilder withDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
        return this;
    }

    public DataSourceLabelEntityBuilder withContextId(final String contextId) {
        this.contextId = contextId;
        return this;
    }

    public DataSourceLabelEntity build() {
        DataSourceLabelEntity dataSourceLabelEntity = new DataSourceLabelEntity();
        dataSourceLabelEntity.setId(id);
        dataSourceLabelEntity.setVersion(version);
        dataSourceLabelEntity.setName(name);
        dataSourceLabelEntity.setDataSourceId(dataSourceId);
        dataSourceLabelEntity.setContextId(contextId);
        return dataSourceLabelEntity;
    }
}
