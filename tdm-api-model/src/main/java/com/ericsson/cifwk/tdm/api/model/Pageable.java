package com.ericsson.cifwk.tdm.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 11/02/2016
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Pageable {

    int page;
    int size;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
