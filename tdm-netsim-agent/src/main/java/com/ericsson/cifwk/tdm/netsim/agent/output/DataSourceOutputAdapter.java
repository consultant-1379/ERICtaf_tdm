package com.ericsson.cifwk.tdm.netsim.agent.output;

import com.ericsson.cifwk.tdm.api.model.DataSource;

/**
 * Created by ekonsla on 04/03/2016.
 */
public interface DataSourceOutputAdapter {

    /**
     * Outputs data source to target destination
     * @param dataSource
     */
    void output(DataSource dataSource);
}
