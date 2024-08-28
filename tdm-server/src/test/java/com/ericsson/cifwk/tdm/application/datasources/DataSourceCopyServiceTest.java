package com.ericsson.cifwk.tdm.application.datasources;

import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.google.common.truth.Truth.assertThat;

import java.util.List;
import java.util.Optional;

import com.ericsson.cifwk.tdm.api.model.Records;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import ma.glasnost.orika.impl.DefaultMapperFactory;
import ma.glasnost.orika.impl.DefaultMapperFactory.Builder;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceCopyServiceTest {

    static final String USER_ID = "userId";

    @Mock
    DataSourceRepository dataSourceRepository;

    @Mock
    private DataSourceActionRepository dataSourceActionRepository;

    @Mock
    private DataRecordRepository dataRecordRepository;

    @Mock
    SecurityService securityService;

    @Mock
    DataSourceCopyRequest dataSourceCopyRequest;

    @Mock
    DataSourceService dataSourceService;

    @Mock
    DataSourceActionsService dataSourceActionsService;

    @Mock
    ContextService contextService;

    @Mock
    Context context1;
    @Mock
    Context context2;

    @Captor
    ArgumentCaptor<List<DataRecordEntity>> recordEntityCaptor;

    @Captor
    ArgumentCaptor<List<DataSourceActionEntity>> actionEntityCaptor;

    DataSourceIdentityEntity dataSourceIdentityEntity;

    @InjectMocks
    DataSourceCopyService service;

    List<DataRecord> dataRecords = Lists.newArrayList();

    @Before
    public void setUp() {
        service.mapperFacade = new Builder().build();

        mockContextService();

        dataSourceIdentityEntity = new DataSourceIdentityEntity();
        dataSourceIdentityEntity.setId("dataSourceId-1");
        dataSourceIdentityEntity.setVersion(Version.INITIAL_VERSION);
        dataSourceIdentityEntity.setName("name");
        dataSourceIdentityEntity.setGroup("group");
        dataSourceIdentityEntity.setContext(context1.getName());
        dataSourceIdentityEntity.setContextId(context1.getId());

        Records records = new Records(dataRecords, null);

        when(dataSourceService.findDataSourceEntityByVersion("dataSourceId-1", "0.0.1")).thenReturn(dataSourceIdentityEntity);
        when(securityService.getCurrentUser()).thenReturn(new AuthenticationStatus(USER_ID, true, Lists.newArrayList(),false));
        when(dataSourceService.findRecordsByVersion("dataSourceId-1", "0.0.1")).thenReturn(records);

        Mockito.doAnswer(invocationOnMock -> {
            DataSourceIdentityEntity identityEntity = (DataSourceIdentityEntity) invocationOnMock.getArguments()[0];
            identityEntity.setId("saved-id");
            return null;
        }).when(dataSourceRepository).insert(dataSourceIdentityEntity);

        when(dataSourceCopyRequest.getDataSourceId()).thenReturn("dataSourceId-1");
        when(dataSourceCopyRequest.getVersion()).thenReturn("0.0.1");

        mockDataRecords();
    }

    private void mockDataRecords() {
        dataRecords.add(mockDataRecord("1"));
        dataRecords.add(mockDataRecord("2"));
        dataRecords.add(mockDataRecord("3"));
    }

    private DataRecord mockDataRecord(String idPostfix) {
        DataRecord dataRecord = Mockito.mock(DataRecord.class, RETURNS_DEEP_STUBS);
        when(dataRecord.getId()).thenReturn("id-" + idPostfix);
        when(dataRecord.getDataSourceId()).thenReturn(dataSourceIdentityEntity.getId());
        when(dataRecord.getValues()).thenReturn(ImmutableMap.of(
                "key-1", "value-1",
                "property-1", "value-1"));
        return dataRecord;
    }

    @Test
    public void shouldCopyDataSource_LeaveOldAttributes() {
        when(dataSourceCopyRequest.getNewContextId()).thenReturn(null);
        when(dataSourceCopyRequest.getNewGroup()).thenReturn(null);
        when(dataSourceCopyRequest.getNewName()).thenReturn("new.name");

        DataSourceIdentity copy = service.copy(dataSourceCopyRequest);

        assertThat(copy.getId()).isEqualTo("saved-id");
        assertThat(copy.getName()).isEqualTo("new.name");
        assertThat(copy.getGroup()).isEqualTo("group");
        assertThat(copy.getContext()).isEqualTo(context1.getId());
        assertThat(copy.getContextId()).isEqualTo(context1.getName());
        assertThat(copy.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(copy.getCreatedBy()).isEqualTo(USER_ID);
    }

    @Test
    public void shouldCopyDataSource_OverrideAttributes() {
        when(dataSourceCopyRequest.getNewContextId()).thenReturn("contextId-2");
        when(dataSourceCopyRequest.getNewGroup()).thenReturn("new.group");
        when(dataSourceCopyRequest.getNewName()).thenReturn("new.name");
        when(dataSourceCopyRequest.isBaseVersion()).thenReturn(true);

        DataSourceIdentity copy = service.copy(dataSourceCopyRequest);

        assertThat(copy.getName()).isEqualTo(dataSourceCopyRequest.getNewName());
        assertThat(copy.getGroup()).isEqualTo(dataSourceCopyRequest.getNewGroup());

        assertThat(copy.getContextId()).isEqualTo(context2.getId());
        assertThat(copy.getContext()).isEqualTo(context2.getName());
        assertThat(copy.getVersion()).isEqualTo("0.0.2-SNAPSHOT");
        assertThat(copy.getCreatedBy()).isEqualTo(USER_ID);
    }

    @Test
    public void shouldCopyDataSourceRecords_RecordEntitiesAreSaved() {
        when(dataSourceCopyRequest.getNewName()).thenReturn("new.name");

        DataSourceIdentity dataSourceCopy = service.copy(dataSourceCopyRequest);

        verify(dataRecordRepository).insert(recordEntityCaptor.capture());

        List<DataRecordEntity> savedRecords = recordEntityCaptor.getValue();
        assertThat(savedRecords).hasSize(3);

        for (int i = 0; i < savedRecords.size(); i++) {
            DataRecordEntity recordEntity = savedRecords.get(i);
            assertThat(recordEntity.getId()).isNull();
            assertThat(recordEntity.getDataSourceId()).isEqualTo(dataSourceCopy.getId());
            assertThat(recordEntity.getValues()).containsEntry("key-1", "value-1");
            assertThat(recordEntity.getValues()).containsEntry("property-1", "value-1");
        }
    }

    @Test
    public void shouldCopyDataSourceRecords_RecordActionsAreSaved() {
        when(dataSourceCopyRequest.getNewName()).thenReturn("new.name");

        Mockito.doAnswer(invocationOnMock -> {
            @SuppressWarnings("unchecked")
            List<DataRecordEntity> recordEntities = (List<DataRecordEntity>) invocationOnMock.getArguments()[0];
            int id = 1;
            for (DataRecordEntity recordEntity : recordEntities) {
                recordEntity.setId("" + id++);
            }
            return null;
        }).when(dataRecordRepository).insert(anyCollectionOf(DataRecordEntity.class));

        service.copy(dataSourceCopyRequest);

        verify(dataSourceActionRepository).insert(actionEntityCaptor.capture());

        List<DataSourceActionEntity> savedActions = actionEntityCaptor.getValue();
        assertThat(savedActions).hasSize(3);

        for (int i = 0; i < savedActions.size(); i++) {
            DataSourceActionEntity actionEntity = savedActions.get(i);
            assertThat(actionEntity.getId()).isNull();
            assertThat(actionEntity.getType()).isEqualTo(DataSourceActionType.RECORD_ADD);
            assertThat(actionEntity.getParentId()).isAnyOf("1", "2", "3");
            assertThat(actionEntity.getValues()).containsEntry("key-1", "value-1");
            assertThat(actionEntity.getValues()).containsEntry("property-1", "value-1");
        }
    }

    private void mockContextService() {
        when(contextService.findBySystemId("contextId-1")).thenReturn(Optional.of(context1));
        when(contextService.findBySystemId("contextId-2")).thenReturn(Optional.of(context2));

        when(context1.getId()).thenReturn("contextId-1");
        when(context1.getName()).thenReturn("contextId-1");

        when(context2.getId()).thenReturn("contextId-2");
        when(context2.getName()).thenReturn("contextId-2");
    }

}
