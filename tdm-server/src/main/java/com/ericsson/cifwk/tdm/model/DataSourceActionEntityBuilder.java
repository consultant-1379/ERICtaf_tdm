package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;

import java.util.Date;
import java.util.SortedMap;

import static com.google.common.collect.Maps.newTreeMap;

public final class DataSourceActionEntityBuilder {

    private String id;
    private String parentId;
    private DataSourceActionType type;
    private SortedMap<String, Object> values = newTreeMap();
    private Integer order;
    private Version version;
    private String createdBy;
    private Date createTime;

    private DataSourceActionEntityBuilder() {
    }

    public static DataSourceActionEntityBuilder aDataSourceActionEntity() {
        return new DataSourceActionEntityBuilder();
    }

    public DataSourceActionEntityBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DataSourceActionEntityBuilder withParentId(String parentId) {
        this.parentId = parentId;
        return this;
    }

    public DataSourceActionEntityBuilder withType(DataSourceActionType type) {
        this.type = type;
        return this;
    }

    public DataSourceActionEntityBuilder withValues(SortedMap<String, Object> values) {
        this.values = values;
        return this;
    }

    public DataSourceActionEntityBuilder withValue(String key, Object value) {
        values.put(key, value);
        return this;
    }

    public DataSourceActionEntityBuilder withOrder(Integer order) {
        this.order = order;
        return this;
    }

    public DataSourceActionEntityBuilder withVersion(int major, int minor, int build) {
        return withVersion(new Version(major, minor, build));
    }

    public DataSourceActionEntityBuilder withVersion(Version version) {
        this.version = version;
        return this;
    }

    public DataSourceActionEntityBuilder withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public DataSourceActionEntityBuilder withCreateTimeNow() {
        return withCreateTime(new Date());
    }

    public DataSourceActionEntityBuilder withCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public DataSourceActionEntity build() {
        DataSourceActionEntity dataSourceActionEntity = new DataSourceActionEntity();
        dataSourceActionEntity.setId(id);
        dataSourceActionEntity.setParentId(parentId);
        dataSourceActionEntity.setType(type);
        dataSourceActionEntity.getValues().putAll(values);
        dataSourceActionEntity.setOrder(order);
        dataSourceActionEntity.setVersion(version);
        dataSourceActionEntity.setAuditData(createdBy, createTime);
        return dataSourceActionEntity;
    }
}
