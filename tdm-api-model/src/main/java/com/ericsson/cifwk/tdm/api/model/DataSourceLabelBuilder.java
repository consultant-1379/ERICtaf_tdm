package com.ericsson.cifwk.tdm.api.model;

public final class DataSourceLabelBuilder {

    private String id;
    private String name;
    private String version;
    private String dataSourceId;
    private String contextId;

    private DataSourceLabelBuilder() {
    }

    public static DataSourceLabelBuilder aDataSourceLabel() {
        return new DataSourceLabelBuilder();
    }

    public DataSourceLabelBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    public DataSourceLabelBuilder withName(final String name) {
        this.name = name;
        return this;
    }

    public DataSourceLabelBuilder withVersion(final String version) {
        this.version = version;
        return this;
    }

    public DataSourceLabelBuilder withDataSourceId(final String dataSourceId) {
        this.dataSourceId = dataSourceId;
        return this;
    }

    public DataSourceLabelBuilder withContextId(final String contextId) {
        this.contextId = contextId;
        return this;
    }

    public DataSourceLabel build() {
        DataSourceLabel label = new DataSourceLabel();
        label.setId(id);
        label.setName(name);
        label.setVersion(version);
        label.setDataSourceId(dataSourceId);
        label.setContextId(contextId);
        return label;
    }
}
