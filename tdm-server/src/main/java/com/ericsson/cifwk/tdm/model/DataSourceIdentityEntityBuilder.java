package com.ericsson.cifwk.tdm.model;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.model.Version.INITIAL_VERSION;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;

public final class DataSourceIdentityEntityBuilder {

    private String id;
    private Version version = INITIAL_VERSION;
    private String name;
    private ApprovalStatus approvalStatus = UNAPPROVED;
    private List<String> reviewers = newArrayList();
    private String approver;
    private String group;
    private String context;
    private String contextId;

    private Map<String, Object> properties = newLinkedHashMap();
    private String testIdColumnName;

    private boolean deleted;
    private String createdBy;
    private String updatedBy;
    private Date createTime;
    private Date updateTime;

    private DataSourceIdentityEntityBuilder() {
    }

    public static DataSourceIdentityEntityBuilder aDataSourceIdentityEntity() {
        return new DataSourceIdentityEntityBuilder();
    }

    public DataSourceIdentityEntityBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DataSourceIdentityEntityBuilder withInitialVersion() {
        return withVersion(INITIAL_VERSION);
    }

    public DataSourceIdentityEntityBuilder withVersion(int major, int minor, int build) {
        return withVersion(new Version(major, minor, build));
    }

    public DataSourceIdentityEntityBuilder withVersion(Version version) {
        this.version = version;
        return this;
    }

    public DataSourceIdentityEntityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DataSourceIdentityEntityBuilder withApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
        return this;
    }

    public DataSourceIdentityEntityBuilder withReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
        return this;
    }

    public DataSourceIdentityEntityBuilder withReviewer(String reviewer) {
        reviewers.add(reviewer);
        return this;
    }

    public DataSourceIdentityEntityBuilder withApprover(String approver) {
        this.approver = approver;
        return this;
    }

    public DataSourceIdentityEntityBuilder withGroup(String group) {
        this.group = group;
        return this;
    }

    public DataSourceIdentityEntityBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    public DataSourceIdentityEntityBuilder withContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public DataSourceIdentityEntityBuilder withProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public DataSourceIdentityEntityBuilder withProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public DataSourceIdentityEntityBuilder withTestIdColumnName(String testIdColumnName) {
        this.testIdColumnName = testIdColumnName;
        return this;
    }

    public DataSourceIdentityEntityBuilder withDeleted(boolean deleted) {
        this.deleted = deleted;
        return this;
    }

    public DataSourceIdentityEntityBuilder withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public DataSourceIdentityEntityBuilder withUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public DataSourceIdentityEntityBuilder withCreateTimeNow() {
        return withCreateTime(new Date());
    }

    public DataSourceIdentityEntityBuilder withCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public DataSourceIdentityEntityBuilder withUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public DataSourceIdentityEntity build() {
        DataSourceIdentityEntity dataSourceIdentityEntity = new DataSourceIdentityEntity();
        dataSourceIdentityEntity.setId(id);
        dataSourceIdentityEntity.setVersion(version);
        dataSourceIdentityEntity.setName(name);
        dataSourceIdentityEntity.setApprovalStatus(approvalStatus);
        dataSourceIdentityEntity.setReviewers(reviewers);
        dataSourceIdentityEntity.setApprover(approver);
        dataSourceIdentityEntity.setGroup(group);
        dataSourceIdentityEntity.setContext(context);
        dataSourceIdentityEntity.setContextId(contextId);

        dataSourceIdentityEntity.setProperties(properties);
        dataSourceIdentityEntity.setTestIdColumnName(testIdColumnName);

        dataSourceIdentityEntity.setDeleted(deleted);
        dataSourceIdentityEntity.setCreatedBy(createdBy);
        dataSourceIdentityEntity.setUpdatedBy(updatedBy);
        dataSourceIdentityEntity.setCreateTime(createTime);
        dataSourceIdentityEntity.setUpdateTime(updateTime);
        return dataSourceIdentityEntity;
    }
}
