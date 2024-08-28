package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/02/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSource {

    @NotNull
    private DataSourceIdentity identity;

    @NotEmpty
    private List<DataRecord> records;

    public DataSourceIdentity getIdentity() {
        return identity;
    }

    public void setIdentity(DataSourceIdentity identity) {
        this.identity = identity;
    }

    public List<DataRecord> getRecords() {
        return records;
    }

    public void setRecords(List<DataRecord> records) {
        this.records = records;
    }
}
