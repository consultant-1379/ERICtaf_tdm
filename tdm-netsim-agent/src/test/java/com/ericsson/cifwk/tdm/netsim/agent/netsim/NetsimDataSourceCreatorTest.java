package com.ericsson.cifwk.tdm.netsim.agent.netsim;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.MIM_VERSION;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETSIM_HOST;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETWORK_ELEMENT_IP;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.NETWORK_ELEMENT_NAME;
import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.SIMULATION_NAME;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by ekonsla on 11/03/2016.
 */
public class NetsimDataSourceCreatorTest {

    NetsimService netsimService = spy(new NetsimService());

    NetsimDataSourceCreator netsimDataSourceCreator;

    @Before
    public void setUp() {
        Simulation simulation = mock(Simulation.class);
        List<Simulation> simulations = new ArrayList<>();
        simulations.add(simulation);

        NetworkElement networkElement = mock(NetworkElement.class);
        List<NetworkElement> networkElements = new ArrayList<>();
        networkElements.add(networkElement);

        Host host = mock(Host.class);
        List<Host> hosts = new ArrayList<>();
        hosts.add(host);

        NetSimCommandHandler netSimCommandHandler = mock(NetSimCommandHandler.class);

        doReturn("netsim").when(simulation).getName();
        doReturn("network_element").when(networkElement).getName();
        doReturn("192.168.1.1").when(networkElement).getIp();
        doReturn("192.168.1.2").when(host).getIp();
        doReturn("mim").when(networkElement).getMim();

        doReturn(simulations).when(netsimService).getAllSimulations(any(NetSimCommandHandler.class));
        doReturn(networkElements).when(netsimService).getNEsFromSimulation(any(NetSimCommandHandler.class), any(String.class));
        doReturn(netSimCommandHandler).when(netsimService).getNetSimCommandHandler(any(Host.class));
        doReturn(hosts).when(netsimService).getHosts();

        netsimDataSourceCreator = new NetsimDataSourceCreator(netsimService);
    }

    @Test
    public void checkGetNetsimData() throws IOException {
        DataSource dataSource = netsimDataSourceCreator.getNetsimData();

        assertThat(dataSource.getRecords().size()).isGreaterThan(0);
        assertThat(dataSource.getIdentity().getName()).isEqualTo("netsim-ds");
        assertThat(dataSource.getIdentity().getGroup()).isEqualTo("com.netsim");
        assertThat(dataSource.getIdentity().getContextId()).isEqualTo("systemId-1");

        DataRecord record = dataSource.getRecords().get(0);
        Map<String, Object> values = record.getValues();

        assertThat(values.containsKey(NETSIM_HOST)).isTrue();
        assertThat(values.containsKey(SIMULATION_NAME)).isTrue();
        assertThat(values.containsKey(NETWORK_ELEMENT_NAME)).isTrue();
        assertThat(values.containsKey(NETWORK_ELEMENT_IP)).isTrue();
        assertThat(values.containsKey(MIM_VERSION)).isTrue();

    }
}
