package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.Node;
import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareService;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import ma.glasnost.orika.BoundMapperFacade;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;

import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 05/05/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceGroupServiceTest {

    @InjectMocks @Spy
    DataSourceGroupService service;

    @Mock
    ContextService contextService;

    @Mock
    DataSourceRepository dataSourceRepository;

    @Mock
    private CIPortalTestwareService ciPortalTestwareService;

    @Test
    public void getGroupTreeByContextId_verifyFlow() {
        @SuppressWarnings("unchecked")
        BoundMapperFacade<List<DataSourceIdentityEntity>, List<Node>> mapperFacade = mock(BoundMapperFacade.class);
        List<DataSourceIdentityEntity> dataSources = dataSources("group");
        List<Node> nodes = newArrayList(new Node("node"));

        doReturn(dataSources).when(service).getDataSourcesByParentContextId(anyString());
        doReturn(mapperFacade).when(service).getDataSourceToNodeMapper();
        doReturn(nodes).when(mapperFacade).map(anyListOf(DataSourceIdentityEntity.class));

        Collection<Node> result = service.getGroupTreeByContextId("contextId");

        assertThat(result).isSameAs(nodes);
        verify(service).getDataSourcesByParentContextId("contextId");
        verify(service).getDataSourceToNodeMapper();
        verify(mapperFacade).map(dataSources);
    }

    @Test
    public void getGroupsByContextId_shouldReturn_concatenatedTdmAndTestwareGroups() {
        List<DataSourceIdentityEntity> dataSources = dataSources("tdmGroup1", "tdmGroup2");
        doReturn(dataSources).when(service).getDataSourcesByParentContextId(anyString());
        doReturn(newArrayList("testwareGroup1", "testwareGroup2"))
                .when(ciPortalTestwareService).getTestwareGroups();

        Collection<String> groupList = service.getGroupsByContextId("contextId");

        assertThat(groupList).containsExactly("tdmGroup1", "tdmGroup2", "testwareGroup1", "testwareGroup2");
        verify(service).getDataSourcesByParentContextId("contextId");
        verify(ciPortalTestwareService).getTestwareGroups();
    }

    @Test
    public void getGroupsByContextId_shouldReturn_distinctGroups() {
        List<DataSourceIdentityEntity> dataSources = dataSources("group1", "group2", "group3");
        doReturn(dataSources).when(service).getDataSourcesByParentContextId(anyString());
        doReturn(newArrayList("group2", "group3", "group4"))
                .when(ciPortalTestwareService).getTestwareGroups();

        Collection<String> groupList = service.getGroupsByContextId("contextId");

        assertThat(groupList).containsExactly("group1", "group2", "group3", "group4");
        verify(service).getDataSourcesByParentContextId("contextId");
        verify(ciPortalTestwareService).getTestwareGroups();
    }

    @Test
    public void getGroupsByContextId_shouldReturn_sortedGroups() {
        List<DataSourceIdentityEntity> dataSources = dataSources("group4", "group2");
        doReturn(dataSources).when(service).getDataSourcesByParentContextId(anyString());
        doReturn(newArrayList("group1", "group3"))
                .when(ciPortalTestwareService).getTestwareGroups();

        Collection<String> groupList = service.getGroupsByContextId("contextId");

        assertThat(groupList).containsExactly("group1", "group2", "group3", "group4").inOrder();
        verify(service).getDataSourcesByParentContextId("contextId");
        verify(ciPortalTestwareService).getTestwareGroups();
    }

    @Test
    public void getDataSourcesByParentContextId_verifyFlow() throws Exception {
        List<String> contextSystemIds = newArrayList("systemId1");
        List<DataSourceIdentityEntity> dataSources = dataSources("group");
        doReturn(contextSystemIds).when(contextService).findAllParentContextIds("contextId");
        doReturn(dataSources).when(dataSourceRepository).findByContextIds(anyListOf(String.class));

        List<DataSourceIdentityEntity> result = service.getDataSourcesByParentContextId("contextId");

        assertThat(result).isSameAs(dataSources);
        verify(contextService).findAllParentContextIds("contextId");
        verify(dataSourceRepository).findByContextIds(contextSystemIds);
    }

    private List<DataSourceIdentityEntity> dataSources(String... groups) {
        return stream(groups).map(this::dataSource).collect(toList());
    }

    private DataSourceIdentityEntity dataSource(String group) {
        return aDataSourceIdentityEntity()
                .withGroup(group)
                .build();
    }
}
