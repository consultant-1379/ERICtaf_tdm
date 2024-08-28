package com.ericsson.cifwk.tdm.netsim.agent;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.handlers.netsim.NetSimCommandHandler;
import com.ericsson.cifwk.taf.handlers.netsim.domain.NetworkElement;
import com.ericsson.cifwk.taf.handlers.netsim.domain.Simulation;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimDataSourceCreator;
import com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimService;
import com.ericsson.cifwk.tdm.netsim.agent.output.DataSourceOutputAdapter;
import com.ericsson.cifwk.tdm.netsim.agent.output.TextOutputAdapter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.tdm.netsim.agent.netsim.NetsimColumns.*;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


/**
 * Created by ekonsla on 11/03/2016.
 */
public class NetsimAgentTest {

    private NetsimService netsimServiceMock = spy(new NetsimService());

    private NetsimDataSourceCreator netsimDataSourceCreator = new NetsimDataSourceCreator(netsimServiceMock);

    private DataSourceOutputAdapter dataSourceOutputAdapterMock = spy(new TextOutputAdapter());

    private NetsimAgent netsimAgent;

    private static final String DATASOURCE_NAME = "netsim";
    private static final String GROUP_NAME = "TAF";

    @Before
    public void setUp() {
        netsimDataSourceCreator.setNetsimService(netsimServiceMock);

        netsimAgent = new NetsimAgent();
        netsimAgent.netsimDataSourceCreator = netsimDataSourceCreator;
        netsimAgent.dataSourceOutputAdapter = dataSourceOutputAdapterMock;

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

        doReturn(simulations).when(netsimServiceMock).getAllSimulations(any(NetSimCommandHandler.class));
        doReturn(networkElements).when(netsimServiceMock).getNEsFromSimulation(any(NetSimCommandHandler.class), any(String.class));
        doReturn(netSimCommandHandler).when(netsimServiceMock).getNetSimCommandHandler(any(Host.class));
        doReturn(hosts).when(netsimServiceMock).getHosts();
    }

    @Test
    public void checkNetsimAgent() throws IOException {

        netsimAgent.execute();

        verify(dataSourceOutputAdapterMock, times(1)).output(argThat(new ArgumentMatcher<DataSource>() {
            @Override
            public boolean matches(Object o) {
                if (!(o instanceof DataSource)){
                    return false;
                }

                DataSourceIdentity identity = ((DataSource) o).getIdentity();
                Map<String, Object> values = ((DataSource) o).getRecords().get(0).getValues();

                boolean identityEquality = identity.getName().equals(DATASOURCE_NAME) && identity.getGroup().equals(GROUP_NAME);
                boolean valuesEquality = values.get(SIMULATION_NAME).equals("netsim") &&
                                         values.get(NETWORK_ELEMENT_NAME).equals("network_element") &&
                                         values.get(NETWORK_ELEMENT_IP).equals("192.168.1.1") &&
                                         values.get(NETSIM_HOST).equals("192.168.1.2") &&
                                         values.get(MIM_VERSION).equals("mim");

                return identityEquality && valuesEquality;
            }
        }));
    }
}
