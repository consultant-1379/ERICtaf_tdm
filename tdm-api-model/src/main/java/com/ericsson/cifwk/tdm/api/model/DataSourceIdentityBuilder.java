package com.ericsson.cifwk.tdm.api.model;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public final class DataSourceIdentityBuilder {

    private String id;
    private String version;
    private String name;
    private ApprovalStatus approvalStatus;
    private List<String> reviewers = newArrayList();
    private String approver;
    private String group;
    private String testIdColumnName;
    private String context;
    private String contextId;
    private Map<String, Object> properties = newHashMap();
    private String createdBy;
    private String updatedBy;
    private Date createTime;
    private Date updateTime;

    private DataSourceIdentityBuilder() {
    }

    public static DataSourceIdentityBuilder aDataSourceIdentity() {
        return new DataSourceIdentityBuilder();
    }

    public DataSourceIdentityBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public DataSourceIdentityBuilder withVersion(String version) {
        this.version = version;
        return this;
    }

    public DataSourceIdentityBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public DataSourceIdentityBuilder withApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
        return this;
    }

    public DataSourceIdentityBuilder withReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
        return this;
    }

    public DataSourceIdentityBuilder withReviewer(String reviewer) {
        reviewers.add(reviewer);
        return this;
    }


    public DataSourceIdentityBuilder withApprover(String approver) {
        this.approver = approver;
        return this;
    }

    public DataSourceIdentityBuilder withGroup(String group) {
        this.group = group;
        return this;
    }

    public DataSourceIdentityBuilder withTestIdColumnName(String testIdColumnName) {
        this.testIdColumnName = testIdColumnName;
        return this;
    }

    public DataSourceIdentityBuilder withContext(String context) {
        this.context = context;
        return this;
    }

    public DataSourceIdentityBuilder withContextId(String contextId) {
        this.contextId = contextId;
        return this;
    }

    public DataSourceIdentityBuilder withProperties(Map<String, Object> properties) {
        this.properties = properties;
        return this;
    }

    public DataSourceIdentityBuilder withProperty(String key, Object value) {
        properties.put(key, value);
        return this;
    }

    public DataSourceIdentityBuilder withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    public DataSourceIdentityBuilder withUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
        return this;
    }

    public DataSourceIdentityBuilder withCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public DataSourceIdentityBuilder withUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

    public DataSourceIdentity build() {
        DataSourceIdentity dataSourceIdentity = new DataSourceIdentity();
        dataSourceIdentity.setId(id);
        dataSourceIdentity.setVersion(version);
        dataSourceIdentity.setName(name);
        dataSourceIdentity.setApprovalStatus(approvalStatus);
        dataSourceIdentity.setReviewers(reviewers);
        dataSourceIdentity.setApprover(approver);
        dataSourceIdentity.setGroup(group);
        dataSourceIdentity.setTestIdColumnName(testIdColumnName);
        dataSourceIdentity.setContext(context);
        dataSourceIdentity.setContextId(contextId);
        dataSourceIdentity.setProperties(properties);
        dataSourceIdentity.setCreatedBy(createdBy);
        dataSourceIdentity.setUpdatedBy(updatedBy);
        dataSourceIdentity.setCreateTime(createTime);
        dataSourceIdentity.setUpdateTime(updateTime);
        return dataSourceIdentity;
    }
}
