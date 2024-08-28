package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gerald Glennon
 *         Date: 06/10/2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DataSourceObject {

    private String dataSourceId;

    private String name;

    private String context;

    private String userAgent;

    public DataSourceObject(@JsonProperty("dataSourceId") String dataSourceId,
                            @JsonProperty("name") String name,
                            @JsonProperty("context") String context,
                            @JsonProperty("userAgent") String userAgent) {
        this.dataSourceId = dataSourceId;
        this.name = name;
        this.context = context;
        this.userAgent = userAgent;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
