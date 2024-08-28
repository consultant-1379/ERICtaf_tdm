package com.ericsson.cifwk.tdm.model;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;
import java.util.Objects;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;

/**
 * Created by egergle on 30/08/2017.
 */
public class DataSourceMetricEntity {

    @MongoId
    @MongoObjectId
    private String id;

    private String dataSourceName;

    private String dataSourceId;

    private String contextName;

    private Date createdAt;

    private String userAgent;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DataSourceMetricEntity userEntity = (DataSourceMetricEntity) other;
        return equal(id, userEntity.id) &&
                equal(createdAt, userEntity.createdAt) &&
                equal(dataSourceId, userEntity.dataSourceId);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("dataSourceName", dataSourceName)
                .add("dataSourceId", dataSourceId)
                .toString();
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public void setDataSourceName(String dataSourceName) {
        this.dataSourceName = dataSourceName;
    }

    public String getContextName() {
        return contextName;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
