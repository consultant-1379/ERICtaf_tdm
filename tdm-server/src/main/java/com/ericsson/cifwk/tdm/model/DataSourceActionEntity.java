package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.application.datasources.AppliedDataSourceActionAccumulator;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.google.common.base.Strings;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;
import java.util.SortedMap;

import static com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder.aDataSourceActionEntity;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Maps.newTreeMap;

public class DataSourceActionEntity {

    @MongoId
    @MongoObjectId
    private String id;

    private String parentId;

    private DataSourceActionType type;

    private SortedMap<String, Object> values = newTreeMap();

    private Integer order;
    private Version version;

    private String createdBy;
    private Date createTime;

    public static DataSourceActionEntity identityInitialCreate(String parentId,
                                                               SortedMap<String, Object> values,
                                                               Version version) {
        return aDataSourceActionEntity()
                .withParentId(parentId)
                .withType(DataSourceActionType.IDENTITY_INITIAL_CREATE)
                .withValues(values)
                .withOrder(0)
                .withVersion(version)
                .build();
    }

    public static DataSourceActionEntity recordAdd(DataRecordEntity dataRecord) {
        return aDataSourceActionEntity()
                .withParentId(dataRecord.getId())
                .withType(DataSourceActionType.RECORD_ADD)
                .withValues(dataRecord.getValues())
                .build();
    }

    public static DataSourceActionEntity recordDelete(final DataRecordEntity dataRecord,
            final DataSourceIdentityEntity dataSource, final int order) {
        return aDataSourceActionEntity()
                .withParentId(dataRecord.getId())
                .withType(DataSourceActionType.RECORD_DELETE)
                .withValue("deleted", true)
                .withVersion(dataSource.getVersion())
                .withOrder(order)
                .build();
    }

    public static DataSourceActionEntity recordEditSingleCell(final DataRecordEntity dataRecord,
            final DataSourceIdentityEntity dataSource, final String key, final String value, final int order) {
        return aDataSourceActionEntity()
                .withParentId(dataRecord.getId())
                .withType(DataSourceActionType.RECORD_VALUE_EDIT)
                .withValue(key, value)
                .withVersion(dataSource.getVersion())
                .withOrder(order)
                .build();
    }



    public static DataSourceActionEntity approvalStatus(DataSourceIdentityEntity dataSource,
                                                        SortedMap<String, Object> values) {
        return aDataSourceActionEntity()
                .withParentId(dataSource.getId())
                .withType(DataSourceActionType.IDENTITY_APPROVAL_STATUS)
                .withValues(values)
                .withVersion(dataSource.getVersion())
                .build();
    }

    public static DataSourceActionEntity columnOrder(String dataSourceId,
                                                        SortedMap<String, Object> values, Version version) {
        return aDataSourceActionEntity()
                .withParentId(dataSourceId)
                .withType(DataSourceActionType.COLUMN_ORDER_CHANGE)
                .withValues(values)
                .withVersion(version)
                .build();
    }

    public static DataSourceActionEntity actionEntity(DataSourceAction action) {
        return aDataSourceActionEntity()
                .withParentId(action.getId())
                .withType(DataSourceActionType.valueOf(action.getType()))
                .withVersion(new Version(action.getVersion()))
                .withValue(action.getKey(), Strings.nullToEmpty(action.getNewValue()))
                .build();
    }

    public static DataSourceActionEntity actionEntities(DataSourceAction action, SortedMap<String, Object> values) {
        return aDataSourceActionEntity()
                .withParentId(action.getId())
                .withType(DataSourceActionType.valueOf(action.getType()))
                .withVersion(new Version(action.getVersion()))
                .withValues(values)
                .build();
    }

    public static DataSourceActionEntity copyFrom(DataSourceActionEntity original) {
        return aDataSourceActionEntity()
                .withParentId(original.getParentId())
                .withType(original.getType())
                .withValues(original.getValues())
                .withOrder(original.getOrder())
                .withVersion(original.getVersion())
                .build();
    }

    public DataSourceActionType getType() {
        return type;
    }

    public void setType(DataSourceActionType type) {
        this.type = type;
    }

    public SortedMap<String, Object> getValues() {
        return values;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void apply(DataSourceIdentityEntity identity, SortedMap<String, DataRecordEntity> idRecordsMap,
                      AppliedDataSourceActionAccumulator accumulator) {
        this.type.apply(identity, idRecordsMap, this, accumulator);
    }

    public void setAuditData(String createdBy, Date createTime) {
        this.createdBy = createdBy;
        this.createTime = createTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("parentId", parentId)
                .add("type", type)
                .add("values", values)
                .add("order", order)
                .add("version", version)
                .toString();
    }
}
