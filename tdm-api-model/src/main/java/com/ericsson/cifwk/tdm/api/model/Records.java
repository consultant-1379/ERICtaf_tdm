package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * @author Gerald
 *         Date: 07/03/2018
 *
 * Object that is returned when feching records. This contains the list of data and its meta data
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Records {
    private List<DataRecord> data;

    private Meta meta;

    public Records() {
        //NOSONAR
    }

    public Records(List<DataRecord> records, Meta meta) {
        this.data = records;
        this.meta = meta;
    }

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<DataRecord> getData() {
        return data;
    }

    public void setData(List<DataRecord> data) {
        this.data = data;
    }
}
