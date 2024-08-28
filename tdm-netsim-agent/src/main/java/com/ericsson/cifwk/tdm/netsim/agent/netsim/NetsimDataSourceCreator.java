package com.ericsson.cifwk.tdm.netsim.agent.netsim;

import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.MIM_VERSION;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETSIM_HOST;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETWORK_ELEMENT_IP;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETWORK_ELEMENT_NAME;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.SIMULATION_NAME;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimConstants.DS_CONTEXT_ID;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimConstants.DS_GROUP;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimConstants.DS_NAME;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimException;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.taf.tools.cli.jsch.JSchCLIToolException;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;

/**
 * Created by ekonsla on 04/03/2016.
 */
public class NetsimDataSourceCreator {



    private static final Logger LOGGER = LoggerFactory.getLogger(NetsimDataSourceCreator.class);
    private NetsimService netsimService;

    public NetsimDataSourceCreator(NetsimService netsimService) {
        this.netsimService = netsimService;
    }

    public DataSource getNetsimData() {

        List<Host> hosts = netsimService.getHosts();

        List<DataRecord> records = createDataRecords(hosts);

        DataSourceIdentity dataSourceIdentity = new DataSourceIdentity();
        dataSourceIdentity.setName(DS_NAME);
        dataSourceIdentity.setGroup(DS_GROUP);
        dataSourceIdentity.setContextId(DS_CONTEXT_ID);

        DataSource dataSource = new DataSource();
        dataSource.setRecords(records);
        dataSource.setIdentity(dataSourceIdentity);

        return dataSource;
    }

    private List<DataRecord> createDataRecords(List<Host> hosts) {
        List<DataRecord> records = new ArrayList<>();

        for (Host host : hosts) {
            records.addAll(createDataRecordsForHost(host));
        }

        return records;
    }

    private List<DataRecord> createDataRecordsForHost(Host host) {

        LOGGER.info("Creating data records for host: {}", host);

        NetSimCommandHandler service = netsimService.getNetSimCommandHandler(host);

        List<DataRecord> records = new ArrayList<>();
        try {
            List<Simulation> simulations = netsimService.getAllSimulations(service);
            for (Simulation simulation : simulations) {
                records.addAll(createDataRecordsForSimulation(service, host, simulation));
            }
        } catch (JSchCLIToolException e) {
            LOGGER.error("Error getting Simulation from host: {}", host, e);
        }
        return records;
    }

    private List<DataRecord> createDataRecordsForSimulation(NetSimCommandHandler service, Host host,
            Simulation simulation) {

        LOGGER.debug("Simulation: {}", simulation);

        List<DataRecord> records = new ArrayList<>();
        try {
            List<NetworkElement> networkElements = netsimService.getNEsFromSimulation(service, simulation.getName());
            for (NetworkElement networkElement : networkElements) {
                DataRecord dataRecord = createDataRecord(host, simulation, networkElement);
                records.add(dataRecord);
            }
        } catch (NetSimException e) {
            LOGGER.error("Error getting NE from simulation: {}", simulation, e);
        }
        return records;
    }

    private static DataRecord createDataRecord(Host host, Simulation simulation, NetworkElement networkElement) {
        LOGGER.debug("Network Element: {}", networkElement);

        DataRecord dataRecord = new DataRecord();
        Map<String, Object> values = new HashMap<>();

        values.put(NETSIM_HOST, host.getIp());
        values.put(SIMULATION_NAME, simulation.getName());
        values.put(NETWORK_ELEMENT_NAME, networkElement.getName());
        values.put(NETWORK_ELEMENT_IP, networkElement.getIp());
        values.put(MIM_VERSION, networkElement.getMim());

        dataRecord.setValues(values);
        return dataRecord;
    }

    public void setNetsimService(NetsimService netsimService) {
        this.netsimService = netsimService;
    }
}
