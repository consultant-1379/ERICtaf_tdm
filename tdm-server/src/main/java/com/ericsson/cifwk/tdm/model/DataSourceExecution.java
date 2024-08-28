package com.ericsson.cifwk.tdm.model;

import com.google.common.collect.Lists;
import org.hibernate.validator.constraints.NotEmpty;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 16/02/2016
 */
public class DataSourceExecution {
    @MongoId
    @MongoObjectId
    private String id;

    @NotEmpty
    private String dataSourceId;
    @NotNull
    private String version;
    @NotEmpty
    private String executionId;

    private String testwarePackage;

    private String flowOrTestStep;

    private List<DataRecordEntity> records = Lists.newArrayList();

    private List<String> predicates;

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

    public String getFlowOrTestStep() {
        return flowOrTestStep;
    }

    public void setFlowOrTestStep(String flowOrTestStep) {
        this.flowOrTestStep = flowOrTestStep;
    }

    public List<DataRecordEntity> getRecords() {
        return records;
    }

    public void setRecords(List<DataRecordEntity> records) {
        this.records = records;
    }

    public List<String> getPredicates() {
        return predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
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
