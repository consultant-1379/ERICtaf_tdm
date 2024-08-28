package com.ericsson.cifwk.tdm.model;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.base.Objects.equal;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import com.google.common.base.Objects;

/**
 * Created by egergle on 30/08/2017.
 */
public class DataSourceLabelEntity {

    @MongoId
    @MongoObjectId
    private String id;

    private String name;

    private String version;

    private String dataSourceId;

    private String contextId;

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

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContextId() {
        return contextId;
    }

    public void setContextId(final String contextId) {
        this.contextId = contextId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id, name, version, dataSourceId, contextId);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        DataSourceLabelEntity dataSourceLabelEntity = (DataSourceLabelEntity) other;
        return equal(id, dataSourceLabelEntity.id) &&
                equal(name, dataSourceLabelEntity.name) &&
                equal(dataSourceId, dataSourceLabelEntity.dataSourceId) &&
                equal(version, dataSourceLabelEntity.version) &&
                equal(contextId, dataSourceLabelEntity.contextId);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("dataSourceId", dataSourceId)
                .add("version", version)
                .add("contextId", contextId)
                .toString();
    }

}
