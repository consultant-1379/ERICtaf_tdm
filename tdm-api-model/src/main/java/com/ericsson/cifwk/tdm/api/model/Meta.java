package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Gerald
 *         Date: 07/03/2018
 *  Class used to add additional information to a request for datasource records
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Meta {
    private Map<String, Object> columnOrder = new HashMap<>();

    public Map<String, Object> getColumnOrder() {
        return columnOrder;
    }

    public void setColumnOrder(Map<String, Object> columnOrder) {
        this.columnOrder = columnOrder;
    }
}
