package com.ericsson.cifwk.tdm.api.model;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

import java.beans.Transient;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties
public class DataSourceIdentity {

    private String id;

    private String version;

    @NotEmpty
    private String name;

    private ApprovalStatus approvalStatus;

    private List<String> reviewers = newArrayList();

    private String approver;

    private String comment;

    private String reviewRequester;

    @NotEmpty
    private String group;

    private String testIdColumnName;

    @NotEmpty
    private String context;

    @NotEmpty
    private String contextId;

    private Map<String, Object> properties = newHashMap();

    private String createdBy;
    private String updatedBy;

    private Date createTime;
    private Date updateTime;

    private String label;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    @Transient
    public boolean isApproved() {
        return APPROVED.equals(approvalStatus);
    }

    public void setApprovalStatus(ApprovalStatus approvalStatus) {
        this.approvalStatus = approvalStatus;
    }

    public List<String> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<String> reviewers) {
        this.reviewers = reviewers;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getReviewRequester() {
        return reviewRequester;
    }

    public void setReviewRequester(final String reviewRequester) {
        this.reviewRequester = reviewRequester;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public String getTestIdColumnName() {
        return testIdColumnName;
    }

    public void setTestIdColumnName(String testIdColumnName) {
        this.testIdColumnName = testIdColumnName;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("version", version)
                .add("name", name)
                .add("approvalStatus", approvalStatus)
                .add("group", group)
                .add("context", context)
                .add("contextId", contextId)
                .add("properties", properties)
                .add("label", label)
                .toString();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
