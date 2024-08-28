package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 15/02/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Lock {

    private String id;

    private Date createTime;

    @NotNull
    private int timeoutSeconds; // seconds

    @NotNull
    private DataSourceExecution dataSourceExecution;

    private LockType type;

    private String jobId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public DataSourceExecution getDataSourceExecution() {
        return dataSourceExecution;
    }

    public void setDataSourceExecution(DataSourceExecution dataSourceExecution) {
        this.dataSourceExecution = dataSourceExecution;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}


