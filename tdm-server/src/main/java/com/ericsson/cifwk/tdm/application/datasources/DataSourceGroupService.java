package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.Node;
import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareService;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import ma.glasnost.orika.BoundMapperFacade;
import ma.glasnost.orika.MapperFactory;
import ma.glasnost.orika.metadata.Type;
import ma.glasnost.orika.metadata.TypeBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@Service
public class DataSourceGroupService {

    @Autowired
    private CIPortalTestwareService ciPortalTestwareService;

    @Autowired
    private DataSourceRepository dataSourceRepository;

    @Autowired
    private ContextService contextService;

    @Autowired
    private MapperFactory mapperFactory;

    public Collection<Node> getGroupTreeByContextId(String contextId) {
        BoundMapperFacade<List<DataSourceIdentityEntity>, List<Node>> mapper = getDataSourceToNodeMapper();
        List<DataSourceIdentityEntity> dataSources = getDataSourcesByParentContextId(contextId);
        return mapper.map(dataSources);
    }

    public Collection<String> getGroupsByContextId(String contextId) {
        List<DataSourceIdentityEntity> dataSources = getDataSourcesByParentContextId(contextId);
        Stream<String> tdmGroups = dataSources.stream().map(DataSourceIdentityEntity::getGroup);
        Stream<String> testwareGroups = ciPortalTestwareService.getTestwareGroups().stream();
        return concat(tdmGroups, testwareGroups)
                .distinct()
                .sorted()
                .collect(toList());
    }

    List<DataSourceIdentityEntity> getDataSourcesByParentContextId(String contextId) {
        List<String> contextSystemIds = contextService.findAllParentContextIds(contextId);
        return dataSourceRepository.findByContextIds(contextSystemIds);
    }

    BoundMapperFacade<List<DataSourceIdentityEntity>, List<Node>> getDataSourceToNodeMapper() {
        Type<List<DataSourceIdentityEntity>> listOfDataSourcesType = new TypeBuilder<List<DataSourceIdentityEntity>>() {
            }.build();
        Type<List<Node>> listOfNodesType = new TypeBuilder<List<Node>>() { }.build();
        return mapperFactory.getMapperFacade(listOfDataSourcesType, listOfNodesType);
    }
}
