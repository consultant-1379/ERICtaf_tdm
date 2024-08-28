package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.api.model.LockType;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 15/02/2016
 */
public class Lock {

    @MongoId
    @MongoObjectId
    private String id;

    @NotNull
    private int timeoutSeconds;

    private Date createTime;

    private DataSourceExecution dataSourceExecution;

    private LockType type;

    private String jobId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LockType getType() {
        return type;
    }

    public void setType(LockType type) {
        this.type = type;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public DataSourceExecution getDataSourceExecution() {
        return dataSourceExecution;
    }

    public void setDataSourceExecution(DataSourceExecution dataSourceExecution) {
        this.dataSourceExecution = dataSourceExecution;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}


