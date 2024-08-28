package com.ericsson.cifwk.tdm.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.SortedMap;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Maps.newTreeMap;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/02/2016
 */
public class DataRecordEntity {

    @MongoId
    @MongoObjectId
    private String id;

    private String dataSourceId;

    private SortedMap<String, Object> values = newTreeMap();

    private boolean deleted;
    private Version deletedIn;

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

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

    public SortedMap<String, Object> getValues() {
        return values;
    }

    public void setValues(SortedMap<String, Object> values) {
        this.values = values;
    }

    public void setValue(String key, String value) {
        values.put(key, value);
    }

    @JsonIgnore
    public boolean isPersisted() {
        return id != null;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("dataSourceId", dataSourceId)
                .add("values", values)
                .toString();
    }

    public void deletedIn(final Version version) {
        this.deletedIn = version;
    }

    public Version deletedIn() {
        return this.deletedIn;
    }

    public static final class DataRecordEntityBuilder {

        private String id;
        private String dataSourceId;
        private SortedMap<String, Object> values = newTreeMap();
        private boolean deleted;

        private DataRecordEntityBuilder() {
        }

        public static DataRecordEntityBuilder aDataRecordEntity() {
            return new DataRecordEntityBuilder();
        }

        public DataRecordEntityBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataRecordEntityBuilder withDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public DataRecordEntityBuilder withValues(SortedMap<String, Object> values) {
            this.values = values;
            return this;
        }

        public DataRecordEntityBuilder withValue(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public DataRecordEntityBuilder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public DataRecordEntity build() {
            DataRecordEntity dataRecordEntity = new DataRecordEntity();
            dataRecordEntity.setId(id);
            dataRecordEntity.setDataSourceId(dataSourceId);
            dataRecordEntity.setValues(values);
            dataRecordEntity.setDeleted(deleted);
            return dataRecordEntity;
        }
    }
}
