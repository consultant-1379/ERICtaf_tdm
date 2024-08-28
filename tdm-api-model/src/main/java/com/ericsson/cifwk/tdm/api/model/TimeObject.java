package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gerald Glennon
 *         Date: 06/10/2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class TimeObject {

    private String month;

    private String year;

    public TimeObject(@JsonProperty("month") String month,
                      @JsonProperty("year") String year) {
        this.month = month;
        this.year = year;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
