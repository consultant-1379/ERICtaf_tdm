package com.ericsson.cifwk.tdm.model;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.google.common.collect.Lists;

public class DataSourceIdentityEntity {

    public final class Attributes {
        public static final String NAME = "name";
        public static final String VERSION = "version";
        public static final String APPROVAL_STATUS = "approvalStatus";
        public static final String APPROVER = "approver";
        public static final String REVIEWERS = "reviewers";
        public static final String REVIEW_REQUESTER = "reviewRequester";
        public static final String COMMENT = "comment";
        public static final String GROUP = "group";
        public static final String CONTEXT = "context";
        public static final String CONTEXT_ID = "contextId";
        public static final String PROPERTIES = "properties";
        public static final String CREATED_BY = "createdBy";
        public static final String CREATE_TIME = "createTime";


        private Attributes() {
        }
    }

    @MongoId
    @MongoObjectId
    private String id;

    private Version version = Version.INITIAL_VERSION;

    private String name;

    private ApprovalStatus approvalStatus;

    private List<String> reviewers = Lists.newArrayList();

    private String approver;

    private String comment;

    private String reviewRequester;

    private String group;

    private String context;

    private String contextId;

    private Map<String, Object> properties = newHashMap();

    private String testIdColumnName;

    private boolean deleted;

    private String createdBy;
    private String updatedBy;

    private Date createTime;
    private Date updateTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
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

    public void setReviewRequester(final String reviewRequester) {
        this.reviewRequester = reviewRequester;
    }

    public String getReviewRequester() {
        return reviewRequester;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
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

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
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

    public String getOwner() {
        if (isNullOrEmpty(updatedBy)) {
            return createdBy;
        }
        return updatedBy;
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

    public SortedMap<String, Object> createValueMap() {
        SortedMap<String, Object> values = newTreeMap();
        values.put(Attributes.NAME, getName());
        values.put(Attributes.APPROVAL_STATUS, getApprovalStatus());
        values.put(Attributes.REVIEWERS, getReviewers());
        values.put(Attributes.APPROVER, getApprover());
        values.put(Attributes.COMMENT, getComment());
        values.put(Attributes.REVIEW_REQUESTER, getReviewRequester());
        values.put(Attributes.GROUP, getGroup());
        values.put(Attributes.CONTEXT, getContext());
        values.put(Attributes.CONTEXT_ID, getContextId());
        values.put(Attributes.PROPERTIES, getProperties());
        values.put(Attributes.CREATED_BY, getCreatedBy());
        values.put(Attributes.CREATE_TIME, getCreateTime());
        return values;
    }

    public void populateFromValueMap(Map<String, Object> values) {
        setName(values.get(Attributes.NAME).toString());
        setApprovalStatus(ApprovalStatus.valueOf(values.get(Attributes.APPROVAL_STATUS).toString()));
        @SuppressWarnings("unchecked")
        final List<String> reviewers = (List<String>) values.get(Attributes.REVIEWERS);
        setReviewers(reviewers);
        setApprover(values.computeIfAbsent(Attributes.APPROVER, value -> "").toString());
        setComment(values.computeIfAbsent(Attributes.COMMENT, value -> "").toString());
        setReviewRequester(values.computeIfAbsent(Attributes.REVIEW_REQUESTER, value -> "").toString());
        setGroup(values.get(Attributes.GROUP).toString());
        setContext(values.get(Attributes.CONTEXT).toString());
        setContextId(values.get(Attributes.CONTEXT_ID).toString());
        @SuppressWarnings("unchecked")
        final Map<String, Object> properties = (Map<String, Object>) values.get(Attributes.PROPERTIES);
        setProperties(properties);
        setCreatedBy(values.get(Attributes.CREATED_BY).toString());
        setCreateTime((Date) values.get(Attributes.CREATE_TIME));
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("version", version)
                .add(Attributes.NAME, name)
                .add(Attributes.APPROVAL_STATUS, approvalStatus)
                .add(Attributes.APPROVER, approver)
                .add(Attributes.COMMENT, comment)
                .add(Attributes.REVIEW_REQUESTER, reviewRequester)
                .add(Attributes.GROUP, group)
                .add(Attributes.CONTEXT, context)
                .add(Attributes.CONTEXT_ID, contextId)
                .add(Attributes.PROPERTIES, properties)
                .toString();
    }
}
