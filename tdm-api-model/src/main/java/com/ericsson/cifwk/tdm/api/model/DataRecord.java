package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.google.common.base.MoreObjects.toStringHelper;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/02/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataRecord {

    private String id;

    private String dataSourceId;

    private Map<String, Object> values;

    private Set<String> modifiedColumns;
    private Map<String, Object> oldValues;
    private boolean deleted;

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

    public Map<String, Object> getValues() {

        for (Iterator<Map.Entry<String, Object>> iter = values.entrySet().iterator(); iter.hasNext();) {
            Map.Entry<String, Object> entry = iter.next();
            if (entry.getValue() != null  && entry.getValue() == ("")) {
                entry.setValue(null);
            }
        }
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @JsonInclude(NON_NULL)
    public Set<String> getModifiedColumns() {
        return modifiedColumns;
    }

    public void setModifiedColumns(Set<String> modifiedColumns) {
        this.modifiedColumns = modifiedColumns;
    }

    public void setOldValues(final Map<String, Object> oldValues) {
        this.oldValues = oldValues;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("dataSourceId", dataSourceId)
                .add("values", values)
                .add("modifiedColumns", modifiedColumns)
                .add("oldValues", oldValues)
                .add("deleted", deleted)
                .toString();
    }

    public Map<String, Object> getOldValues() {
        return oldValues;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(final boolean deleted) {
        this.deleted = deleted;
    }

    public static final class DataRecordBuilder {

        private String id;
        private String dataSourceId;
        private Map<String, Object> values;

        private DataRecordBuilder() {
        }

        public static DataRecordBuilder aDataRecord() {
            return new DataRecordBuilder();
        }

        public DataRecordBuilder withId(String id) {
            this.id = id;
            return this;
        }

        public DataRecordBuilder withDataSourceId(String dataSourceId) {
            this.dataSourceId = dataSourceId;
            return this;
        }

        public DataRecordBuilder withValues(Map<String, Object> values) {
            this.values = values;
            return this;
        }

        public DataRecordBuilder withValue(String key, Object value) {
            values.put(key, value);
            return this;
        }

        public DataRecord build() {
            DataRecord dataRecord = new DataRecord();
            dataRecord.setId(id);
            dataRecord.setDataSourceId(dataSourceId);
            dataRecord.setValues(values);
            return dataRecord;
        }
    }
}
