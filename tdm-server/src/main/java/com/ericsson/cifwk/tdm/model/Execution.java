package com.ericsson.cifwk.tdm.model;

import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/02/2016
 */
public class Execution {

    @MongoId
    @MongoObjectId
    private String id;

    /**
     * Include mapping to test suite, test case in properties
     */
    private Map<String, Object> properties = new HashMap<>();


    private Date startTime;

    private Date endTime;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void finish() {
        this.endTime = new Date();
    }

    public void start() {
        this.startTime = new Date();
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
}
