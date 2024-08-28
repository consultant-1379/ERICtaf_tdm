package com.ericsson.cifwk.tdm.netsim.agent.netsim;

import java.util.List;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NeGroup;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.taf.handlers.netsim.domain.SimulationGroup;
import com.ericsson.oss.testware.hostconfigurator.HostConfigurator;

/**
 * Created by ekonsla on 15/03/2016.
 */
public class NetsimService {

    public List<Host> getHosts() {
        return HostConfigurator.getAllNetsimHosts();
    }

    public NetSimCommandHandler getNetSimCommandHandler(Host host) {
        return NetSimCommandHandler.getInstance(host);
    }

    public List<Simulation> getAllSimulations(NetSimCommandHandler service) {
        SimulationGroup simulationGroup = service.getAllSimulations();
        return simulationGroup.getSimulations();
    }

    public List<NetworkElement> getNEsFromSimulation(NetSimCommandHandler service, String simulation) {
        NeGroup neGroup = service.getSimulationNEs(simulation);
        return neGroup.getNetworkElements();
    }
}
