package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 16/02/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceExecution {

    private String id;

    @NotEmpty
    private String dataSourceId;
    @NotNull
    private String version;
    @NotEmpty
    private String executionId;

    private String testwarePackage;

    private Map<String, Object> parameters;

    private List<DataRecord> records = Lists.newArrayList();
    private List<String> predicates = Lists.newArrayList();
    private int limit = -1;

    private String columns;

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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getTestwarePackage() {
        return testwarePackage;
    }

    public void setTestwarePackage(String testwarePackage) {
        this.testwarePackage = testwarePackage;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    public List<DataRecord> getRecords() {
        return records;
    }

    public void setRecords(List<DataRecord> records) {
        this.records = records;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    public void addPredicate(String predicate) {
        this.predicates.add(predicate);
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getColumns() {
        return columns;
    }

    public void setColumns(String columns) {
        this.columns = columns;
    }
}
