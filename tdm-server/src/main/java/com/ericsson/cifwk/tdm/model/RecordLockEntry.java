package com.ericsson.cifwk.tdm.model;

import java.util.Calendar;
import java.util.Date;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 18/02/2016
 */
public class RecordLockEntry {

    @MongoId
    @MongoObjectId
    private String id;

    private Date expireAt;
    private DataRecordEntity dataRecord;
    private String lockId;

    public RecordLockEntry() {
        //NO SONAR
    }

    public RecordLockEntry(DataRecordEntity dataRecord, String lockId, int timeoutSeconds) {
        this.dataRecord = dataRecord;
        this.lockId = lockId;

        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.SECOND, timeoutSeconds);
        this.expireAt = instance.getTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Date expireAt) {
        this.expireAt = expireAt;
    }

    public DataRecordEntity getDataRecord() {
        return dataRecord;
    }

    public void setDataRecord(DataRecordEntity dataRecord) {
        this.dataRecord = dataRecord;
    }

    public String getLockId() {
        return lockId;
    }

    public void setLockId(String lockId) {
        this.lockId = lockId;
    }
}
