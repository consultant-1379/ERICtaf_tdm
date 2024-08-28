package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Gerald Glennon
 *         Date: 06/10/2017
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticsObject {

    private String label;

    private int value;

    public StatisticsObject() {
        // needed to parse json
    }

    public StatisticsObject(String label, int value) {
        this.label = label;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
