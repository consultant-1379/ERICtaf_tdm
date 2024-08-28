package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gerald Glennon
 *         Date: 10/10/2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceMetricsObject {

    private String name;

    private int total;

    private String context;

    private String userAgent;

    public DataSourceMetricsObject(@JsonProperty("_id") DataSourceObject dataSourceObject,
                                   @JsonProperty("total") int value) {
        this.name = dataSourceObject.getName();
        this.context = dataSourceObject.getContext();
        this.userAgent = dataSourceObject.getUserAgent();
        this.total = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}
