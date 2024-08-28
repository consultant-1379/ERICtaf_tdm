package com.ericsson.cifwk.tdm.netsim.agent;

import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimDataSourceCreator;
import com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimService;
import com.ericsson.cifwk.tdm.netsim.agent.output.DataSourceOutputAdapter;
import com.ericsson.cifwk.tdm.netsim.agent.output.TDMOutputAdapter;

/**
 * Created by ekonsla on 04/03/2016.
 * <p>
 * accepts two system properties:
 * taf.clusterId - Id of cluster, that netsim data will be taken from
 * tdm.api.host - host of tdm api
 * <p>
 * e.g.
 * java -Dtaf.clusterId=334 -Dtdm.api.host=http://localhost:5682/api/ -jar tdm-netsim-agent.jar
 */
public class NetsimAgent {

    NetsimService netsimService = new NetsimService();

    NetsimDataSourceCreator netsimDataSourceCreator = new NetsimDataSourceCreator(netsimService);

    DataSourceOutputAdapter dataSourceOutputAdapter = new TDMOutputAdapter();

    public static void main(String[] args) {
        NetsimAgent netsimAgent = new NetsimAgent();
        netsimAgent.execute();
    }

    public void execute() {
        DataSource dataSource = netsimDataSourceCreator.getNetsimData();
        dataSourceOutputAdapter.output(dataSource);
    }
}
