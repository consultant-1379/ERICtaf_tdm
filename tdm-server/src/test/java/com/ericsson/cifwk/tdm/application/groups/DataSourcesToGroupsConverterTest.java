package com.ericsson.cifwk.tdm.application.groups;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.ericsson.cifwk.tdm.api.model.Node;
import com.ericsson.cifwk.tdm.infrastructure.mapping.DataSourcesToGroupsConverter;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Created by ekonsla on 05/04/2016.
 */
public class DataSourcesToGroupsConverterTest {

    DataSourcesToGroupsConverter converter;
    List<DataSourceIdentityEntity> dataSourceIdentities;

    @Before
    public void setUp() {
        converter = new DataSourcesToGroupsConverter();
        dataSourceIdentities = new ArrayList<>();

        DataSourceIdentityEntity dataSourceIdentity2 = new DataSourceIdentityEntity();
        dataSourceIdentity2.setId("66c5fdd29759e577fc685b73");
        dataSourceIdentity2.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity2.setName("DS1");
        dataSourceIdentity2.setGroup("com.ericsson");
        dataSourceIdentity2.setContext("ENM");
        dataSourceIdentity2.setCreatedBy("ekonsla");
        dataSourceIdentity2.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity2);

        DataSourceIdentityEntity dataSourceIdentity3 = new DataSourceIdentityEntity();
        dataSourceIdentity3.setId("76c5ddd29759e577fc68ab75");
        dataSourceIdentity3.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity3.setName("DS1");
        dataSourceIdentity3.setGroup("com.ericsson.taf");
        dataSourceIdentity3.setContext("ENM");
        dataSourceIdentity3.setCreatedBy("ekonsla");
        dataSourceIdentity3.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity3);

        DataSourceIdentityEntity dataSourceIdentity4 = new DataSourceIdentityEntity();
        dataSourceIdentity4.setId("46c5ddd29759e577fc68ab78");
        dataSourceIdentity4.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity4.setName("DS1");
        dataSourceIdentity4.setGroup("com.ericsson.tms");
        dataSourceIdentity4.setContext("ENM");
        dataSourceIdentity4.setCreatedBy("ekonsla");
        dataSourceIdentity4.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity4);

        DataSourceIdentityEntity dataSourceIdentity5 = new DataSourceIdentityEntity();
        dataSourceIdentity5.setId("86c5ddd29759e577fc68ab72");
        dataSourceIdentity5.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity5.setName("DS4");
        dataSourceIdentity5.setGroup("host.netsim.111");
        dataSourceIdentity5.setContext("ENM");
        dataSourceIdentity5.setCreatedBy("ekonsla");
        dataSourceIdentity5.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity5);

        DataSourceIdentityEntity dataSourceIdentity6 = new DataSourceIdentityEntity();
        dataSourceIdentity6.setId("86c5ddd29759e577fc68ab82");
        dataSourceIdentity6.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity6.setName("DS5");
        dataSourceIdentity6.setGroup("host.netsim.222");
        dataSourceIdentity6.setContext("ENM");
        dataSourceIdentity6.setCreatedBy("ekonsla");
        dataSourceIdentity6.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity6);

        DataSourceIdentityEntity dataSourceIdentity7 = new DataSourceIdentityEntity();
        dataSourceIdentity7.setId("86c5ddd29759e577fc99ab82");
        dataSourceIdentity7.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity7.setName("DS6");
        dataSourceIdentity7.setGroup("com.ericsson.taf");
        dataSourceIdentity7.setContext("ENM");
        dataSourceIdentity7.setCreatedBy("ekonsla");
        dataSourceIdentity7.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity7);
    }

    @Test
    public void shouldConvertToGroupsWithOnlySingleChildGroup(){
        List<DataSourceIdentityEntity> dataSourceIdentities = new ArrayList<>();

        DataSourceIdentityEntity dataSourceIdentity2 = new DataSourceIdentityEntity();
        dataSourceIdentity2.setId("66c5fdd29759e577fc685b73");
        dataSourceIdentity2.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity2.setName("DS1");
        dataSourceIdentity2.setGroup("com.ericsson");
        dataSourceIdentity2.setContext("ENM");
        dataSourceIdentity2.setCreatedBy("ekonsla");
        dataSourceIdentity2.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity2);

        DataSourceIdentityEntity dataSourceIdentity3 = new DataSourceIdentityEntity();
        dataSourceIdentity3.setId("76c5ddd29759e577fc68ab75");
        dataSourceIdentity3.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentity3.setName("DS2");
        dataSourceIdentity3.setGroup("com.ericsson.taf");
        dataSourceIdentity3.setContext("ENM");
        dataSourceIdentity3.setCreatedBy("ekonsla");
        dataSourceIdentity3.setCreateTime(new Date());
        dataSourceIdentities.add(dataSourceIdentity3);

        Type<List<Node>> listOfNodes = new TypeBuilder<List<Node>>(){}.build();
        List<Node> nodes = converter.convert(dataSourceIdentities, listOfNodes);

        assertThat(nodes)
                .hasSize(1)
                .extracting("group", "name", "groupName")
                .containsExactly(tuple(true, "com.ericsson", "com.ericsson"));
        assertThat(nodes.get(0).getChildren())
                .hasSize(2)
                .extracting("group", "name", "groupName")
                .containsExactly(tuple(false, "DS1", "com.ericsson"), tuple(true, "taf", "com.ericsson.taf"));
        assertThat(nodes.get(0).getChildren().get(1).getChildren())
                .hasSize(1)
                .extracting("group", "name", "groupName")
                .containsExactly(tuple(false, "DS2", "com.ericsson.taf"));
    }

    @Test
    public void shouldConvertToGroups() {
        Type<List<Node>> listOfNodes = new TypeBuilder<List<Node>>(){}.build();
        List<Node> nodes = converter.convert(dataSourceIdentities, listOfNodes);

        List<String> rootGroupNames = nodes.stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());


        assertThat(rootGroupNames).containsExactly("com.ericsson", "host.netsim");

        Node comErricsson = nodes.stream()
                .filter(g -> g.getName().equals("com.ericsson"))
                .findFirst()
                .get();

        List<String> comEricssonChildrenNames = comErricsson.getChildren().stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());

        assertThat(comEricssonChildrenNames).containsExactly("DS1", "taf", "tms");

        Node tms = comErricsson.getChildren().stream().filter(g -> g.getName().equals("tms")).findFirst().get();
        Node taf = comErricsson.getChildren().stream().filter(g -> g.getName().equals("taf")).findFirst().get();

        List<String> tafDatasourcesNames = taf.getChildren().stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());

        assertThat(tafDatasourcesNames).containsExactly("DS1", "DS6");
        assertThat(taf.getChildren().stream().allMatch(d -> !d.isGroup())).isTrue();

        List<String> tmsDatasourcesNames = tms.getChildren().stream()
                .map(g -> g.getName())
                .collect(Collectors.toList());

        assertThat(tmsDatasourcesNames).contains("DS1");
        assertThat(tmsDatasourcesNames).hasSize(1);
        assertThat(tms.getChildren().stream().allMatch(d -> !d.isGroup())).isTrue();
    }
}
