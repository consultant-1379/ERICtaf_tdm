package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Gerald Glennon
 *         Date: 06/10/2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserMetricsObject {

    private String month;

    private String year;

    private int value;

    public UserMetricsObject(@JsonProperty("_id") TimeObject timeObject,
                             @JsonProperty("total") int value) {
        this.month = timeObject.getMonth();
        this.year = timeObject.getYear();
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
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
