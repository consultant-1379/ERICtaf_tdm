package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes;
import com.ericsson.cifwk.tdm.model.RecordPredicate;
import com.ericsson.cifwk.tdm.model.Version;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import java.util.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprovalRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_GROUP_EDIT;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_KEY_ADD;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_NAME_EDIT;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_ADD;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_DELETE;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_KEY_DELETE;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_KEY_RENAME;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_VALUE_EDIT;
import static com.ericsson.cifwk.tdm.model.DataRecordEntity.DataRecordEntityBuilder.aDataRecordEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder.aDataSourceActionEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.COMMENT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEWERS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEW_REQUESTER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.VERSION;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.truth.Truth.assertThat;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 10/05/2016
 */
@RunWith(MockitoJUnitRunner.class)
public class DataSourceActionsServiceTest {

    private static final String USER_ID = "userId";

    @Mock
    private DataSourceRepository dataSourceRepository;

    @Mock
    private DataSourceActionRepository dataSourceActionRepository;

    @Mock
    private DataRecordRepository dataRecordRepository;

    @Mock
    private SecurityService securityService;

    @Mock
    private VersioningService versioningService;

    @Mock
    private DataSourceHelper dataSourceHelper;

    @Captor
    private ArgumentCaptor<Map<String, DataRecordEntity>> idRecordsMapCaptor;
    @Captor
    private ArgumentCaptor<Collection<DataSourceActionEntity>> actionsCaptor;
    @Captor
    private ArgumentCaptor<DataSourceActionEntity> actionCaptor;

    @Spy
    @InjectMocks
    private DataSourceActionsService service;

    private DataRecordEntity dataRecordEntity;
    private DataSourceIdentityEntity dataSource;
    private SortedMap<String, DataRecordEntity> idRecordsMap = newTreeMap();
    private ApprovalRequest request;

    @Before
    public void setUp() {
        dataSource = aDataSourceIdentityEntity()
                .withId("dsId-1")
                .withInitialVersion()
                .withName("name-1")
                .withApprovalStatus(ApprovalStatus.UNAPPROVED)
                .withGroup("group-1")
                .withContext("context-1")
                .withContextId("contextId-1")
                .withProperties(newHashMap())
                .withCreatedBy(USER_ID)
                .withCreateTime(new Date())
                .build();

        dataRecordEntity = aDataRecordEntity()
                .withId("record-1")
                .withDataSourceId(dataSource.getId())
                .withValue("key-1", "value-1")
                .withValue("key-2", "value-2")
                .withValue("key-3", "value-3")
                .build();

        request = anApprovalRequest()
                .withStatus(UNAPPROVED)
                .build();

        when(securityService.getCurrentUser()).thenReturn(new AuthenticationStatus(USER_ID, true, newArrayList(),false));
        when(dataRecordRepository.find(eq("dsId-1"), anyListOf(RecordPredicate.class), anyListOf(String.class)))
                .thenReturn(idRecordsMap);
        when(dataSourceActionRepository.findLatestOrder(idRecordsMap.keySet())).thenReturn(new AtomicInteger());
    }

    @Test
    public void insertInitialAction_shouldInsertInitialActionToDatabase() {
        service.insertInitialAction(dataSource);

        ArgumentCaptor<DataSourceActionEntity> argument = ArgumentCaptor.forClass(DataSourceActionEntity.class);
        verify(dataSourceActionRepository).insert(argument.capture());

        DataSourceActionEntity actionToPersist = argument.getValue();

        assertThat(actionToPersist.getOrder()).isEqualTo(0);
        assertThat(actionToPersist.getType()).isEqualTo(DataSourceActionType.IDENTITY_INITIAL_CREATE);

        assertThat(actionToPersist.getValues()).containsEntry(Attributes.NAME, dataSource.getName());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.APPROVAL_STATUS, dataSource.getApprovalStatus());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.GROUP, dataSource.getGroup());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.CONTEXT, dataSource.getContext());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.CONTEXT_ID, dataSource.getContextId());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.PROPERTIES, dataSource.getProperties());
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.CREATED_BY, USER_ID);
        assertThat(actionToPersist.getValues()).containsEntry(Attributes.CREATE_TIME, dataSource.getCreateTime());
    }

    @Test
    public void applyActionsAndSave_AddNewRecord_SaveRecords() {
       List<DataSourceAction> action = asList(
                action(RECORD_ADD, "id-1", "key-1", "value-1"),
                action(RECORD_ADD, "id-2", "key-2", ""));

        service.applyActionsAndSave(dataSource, action);

        verify(dataSourceRepository).update(dataSource);
        verify(dataRecordRepository).update(idRecordsMapCaptor.capture());

        //Verify records for save
        Map<String, DataRecordEntity> idRecordsMap = idRecordsMapCaptor.getValue();
        assertThat(idRecordsMap).containsKey("id-1");

        assertThat(idRecordsMap.get("id-1").getId()).isEqualTo("id-1");
        assertThat(idRecordsMap.get("id-1").getDataSourceId()).isEqualTo(dataSource.getId());
        assertThat(idRecordsMap.get("id-1").getValues()).containsEntry("key-1", "value-1");
        assertThat(idRecordsMap.get("id-1").isDeleted()).isFalse();
        assertThat(idRecordsMap.get("id-2").getValues()).isEmpty();

    }

    @Test
    public void applyActionsAndSave_AddNewRecord_SaveActions() {

        List<DataSourceAction> action = asList(
                action(RECORD_ADD, "id-1", "key-1", "value-1"),
                action(RECORD_ADD, "id-2", "key-2", ""));

        service.applyActionsAndSave(dataSource, action);

        verify(dataSourceRepository).update(dataSource);
        verify(dataSourceActionRepository).insert(actionsCaptor.capture());

        //Verify actions for save
        List<DataSourceActionEntity> actionsToInsert = newArrayList(actionsCaptor.getValue());

        assertThat(actionsToInsert).hasSize(2);
        DataSourceActionEntity actionEntity = actionsToInsert.get(0);
        assertThat(actionEntity.getType()).isEqualTo(RECORD_ADD);
        assertThat(actionEntity.getParentId()).isEqualTo(action.get(0).getId());
        assertThat(actionEntity.getOrder()).isEqualTo(1);
        assertThat(actionEntity.getValues()).containsEntry("key-1", "value-1");
        assertNull(actionEntity.getValues().get("id-2"));

    }

    @Test
    public void applyActionsAndSave_EditExistingRecord_SaveRecords() {
        idRecordsMap.put("record-1", dataRecordEntity);

        List<DataSourceAction> actions = asList(
                action(RECORD_KEY_RENAME, "record-1", "key-1", "key-11"),
                action(RECORD_KEY_DELETE, "record-1", "key-2", null),
                action(RECORD_VALUE_EDIT, "record-1", "new-key-empty", ""),
                action(RECORD_VALUE_EDIT, "record-1", "new-key-1", "new-value-2"));


        service.applyActionsAndSave(dataSource, actions);

        verify(dataSourceRepository).update(dataSource);
        verify(dataRecordRepository).update(idRecordsMapCaptor.capture());

        //Verify records for save
        Map<String, DataRecordEntity> idRecordsMap = idRecordsMapCaptor.getValue();
        assertThat(idRecordsMap).containsKey("record-1");


        DataRecordEntity recordEntity = idRecordsMap.get("record-1");
        assertThat(recordEntity.getId()).isEqualTo("record-1");
        assertThat(recordEntity.getDataSourceId()).isEqualTo(dataSource.getId());
        assertThat(recordEntity.getValues()).containsEntry("key-11", "value-1");
        assertThat(recordEntity.getValues()).containsEntry("key-3", "value-3");
        assertThat(recordEntity.getValues()).containsEntry("new-key-1", "new-value-2");
        assertThat(recordEntity.getValues()).doesNotContainKey("key-2");
        assertNull(recordEntity.getValues().get("new-key-empty"));
        assertThat(recordEntity.isDeleted()).isFalse();
    }

    @Test
    public void applyActionsAndSave_EditExistingRecord_SaveActions() {
        idRecordsMap.put("record-1", dataRecordEntity);

        List<DataSourceAction> actions = asList(
                action(RECORD_KEY_RENAME, "record-1", "key-1", "key-11"),
                action(RECORD_KEY_DELETE, "record-1", "key-2", null),
                action(RECORD_VALUE_EDIT, "record-1", "new-key-empty", ""),
                action(RECORD_VALUE_EDIT, "record-1", "new-key-1", "new-value-2"));


        service.applyActionsAndSave(dataSource, actions);

        verify(dataSourceRepository).update(dataSource);
        verify(dataSourceActionRepository).insert(actionsCaptor.capture());

        //Verify actions for save
        List<DataSourceActionEntity> actionsToInsert = newArrayList(actionsCaptor.getValue());

        assertThat(actionsToInsert).hasSize(4);

        int order = 0;
        for (DataSourceActionEntity dataSourceActionEntity : actionsToInsert) {
            assertThat(dataSourceActionEntity.getParentId()).isEqualTo("record-1");
            assertThat(dataSourceActionEntity.getOrder()).isEqualTo(++order);
            assertThat(dataSourceActionEntity.getType()).isAnyOf(RECORD_KEY_DELETE, RECORD_VALUE_EDIT, RECORD_KEY_RENAME);
        }
    }

    @Test
    public void applyActionsAndSave_DeleteRecord_SaveRecords() {
        idRecordsMap.put("record-1", dataRecordEntity);

        List<DataSourceAction> actions = asList(
                action(RECORD_DELETE, "record-1", "", ""));

        service.applyActionsAndSave(dataSource, actions);

        verify(dataSourceRepository).update(dataSource);
        verify(dataRecordRepository).update(idRecordsMapCaptor.capture());

        //Verify records for save
        Map<String, DataRecordEntity> idRecordsMap = idRecordsMapCaptor.getValue();

        assertThat(idRecordsMap).containsKey("record-1");
        assertThat(idRecordsMap.get("record-1").isDeleted()).isTrue();
    }

    @Test
    public void applyActionsAndSave_DeleteRecord_SaveActions() {
        idRecordsMap.put("record-1", dataRecordEntity);

        List<DataSourceAction> actions = asList(
                action(RECORD_DELETE, "record-1", "", ""));

        service.applyActionsAndSave(dataSource, actions);

        verify(dataSourceRepository).update(dataSource);
        verify(dataSourceActionRepository).insert(actionsCaptor.capture());

        //Verify actions for save
        List<DataSourceActionEntity> actionsToInsert = newArrayList(actionsCaptor.getValue());

        assertThat(actionsToInsert).hasSize(1);
        DataSourceActionEntity actionEntity = actionsToInsert.get(0);
        assertThat(actionEntity.getType()).isEqualTo(RECORD_DELETE);
        assertThat(actionEntity.getParentId()).isEqualTo("record-1");
        assertThat(actionEntity.getOrder()).isEqualTo(1);
    }

    @Test
    public void applyActionsAndSave_EditIdentity() {
        List<DataSourceAction> actions = asList(
                action(IDENTITY_KEY_ADD, "dsId-1", "new-key-1", "new-value-1"),
                action(IDENTITY_GROUP_EDIT, "dsId-1", "", "new.group.taf"),
                action(IDENTITY_NAME_EDIT, "dsId-1", "", "new.name!!!")
        );

        service.applyActionsAndSave(dataSource, actions);

        assertThat(dataSource.getProperties()).containsEntry("new-key-1", "new-value-1");
        assertThat(dataSource.getName()).isEqualTo("new.name!!!");
        assertThat(dataSource.getGroup()).isEqualTo("new.group.taf");


        verify(dataSourceActionRepository).insert(actionsCaptor.capture());

        //Verify actions for save
        List<DataSourceActionEntity> actionsToInsert = newArrayList(actionsCaptor.getValue());
        assertThat(actionsToInsert).hasSize(3);

        verify(dataSourceRepository).update(dataSource);
    }

    @Test
    public void applyActionsAndSave_shouldIncrementVersion_afterMappingToActionEntities() throws Exception {
        List<DataSourceAction> actions = newArrayList();
        DataSourceActionEntity actionEntity = aDataSourceActionEntity()
                .withType(mock(DataSourceActionType.class))
                .build();
        List<DataSourceActionEntity> actionEntities = newArrayList(actionEntity);
        doReturn(actionEntities).when(service).toActionEntities(anyListOf(DataSourceAction.class));

        service.applyActionsAndSave(dataSource, actions);

        InOrder flowOrder = inOrder(service, versioningService);
        flowOrder.verify(service).toActionEntities(actions);
        flowOrder.verify(versioningService).incrementVersion(dataSource, actionEntities);
    }

    @Test
    public void applyApprovalStatusChange_shouldUpdateDataSourceApprovalStatus() throws Exception {
        request.setStatus(PENDING);

        service.applyApprovalStatusChange(dataSource, request);

        assertThat(dataSource.getApprovalStatus()).isEqualTo(PENDING);
        assertThat(dataSource.getReviewRequester()).isEqualTo(USER_ID);
        verify(dataSourceRepository).update(dataSource);
    }

    @Test
    public void applyApprovalStatusChange_shouldInsertApprovalStatusAction() throws Exception {
        request.setStatus(PENDING);
        request.setReviewers(newArrayList(
                anUser().withUsername("user1").build(),
                anUser().withUsername("user2").build()
        ));
        request.setComment("my first comment");

        service.applyApprovalStatusChange(dataSource, request);

        verify(dataSourceActionRepository).insert(actionCaptor.capture());
        DataSourceActionEntity action = actionCaptor.getValue();

        assertThat(action.getParentId()).isEqualTo(dataSource.getId());
        assertThat(action.getType()).isEqualTo(IDENTITY_APPROVAL_STATUS);
        assertThat(action.getValues()).containsExactly(
                APPROVAL_STATUS, PENDING,
                REVIEWERS, newArrayList("user1", "user2"),
                COMMENT, "my first comment",
                APPROVER, null,
                REVIEW_REQUESTER, "userId",
                VERSION, new Version("0.0.1-SNAPSHOT")
        );
        assertThat(action.getVersion()).isSameAs(dataSource.getVersion());
    }

    @Test
    public void applyApprovalStatusChange_shouldInsertAction_withLastOrder() {
        doReturn(42).when(dataSourceActionRepository).numberOfActions(anyString(), any(Version.class));

        when(dataSourceRepository.findById(dataSource.getId())).thenReturn(dataSource);
        service.applyApprovalStatusChange(dataSource, request);

        verify(dataSourceActionRepository).insert(actionCaptor.capture());
        DataSourceActionEntity action = actionCaptor.getValue();

        assertThat(action.getOrder()).isEqualTo(42);
        verify(dataSourceActionRepository).numberOfActions(dataSource.getId(), dataSource.getVersion());
    }

    @Test
    public void applyApprovalStatusChange_shouldInsertAction_withAuditData() {
        when(dataSourceRepository.findById(dataSource.getId())).thenReturn(dataSource);
        service.applyApprovalStatusChange(dataSource, request);

        verify(dataSourceActionRepository).insert(actionCaptor.capture());
        DataSourceActionEntity action = actionCaptor.getValue();

        assertThat(action.getCreatedBy()).isEqualTo(USER_ID);
        assertThat(action.getCreateTime()).isNotNull();
    }

    private DataSourceAction action(DataSourceActionType type,
                                    String id, String key, String value) {
        return aDataSourceAction()
                .withId(id)
                .withType(type.toString())
                .withKey(key)
                .withNewValue(value)
                .withVersion("1.0.0")
                .withLocalTimestamp(currentTimeMillis())
                .build();
    }

    @Test
    public void findLatestApprovedVersion_shouldReturnVersionWithoutSnapshotIfExist() {
        Version latestApprovedVersion = new Version(1, 1, 0);
        latestApprovedVersion.setSnapshot(false);
        List<Version> versions = newArrayList(
                new Version(1, 0, 0),
                new Version(2, 0, 0),
                latestApprovedVersion
        );

        doReturn(versions).when(service).getActualVersions(anyString());
        Optional<Version> latestApproved = service.findLatestApprovedVersion(anyString());
        assertThat(latestApproved.isPresent()).isTrue();
        assertThat(latestApproved.get()).isEqualTo(latestApprovedVersion);
    }

    @Test
    public void findLatestApprovedVersion_shouldReturnEmptyResultIfNonExist() {
        List<Version> versions = newArrayList(
                new Version(1, 0, 0),
                new Version(2, 0, 0),
                new Version(1, 1, 0)
        );

        doReturn(versions).when(service).getActualVersions(anyString());
        Optional<Version> latestApprovedVersion = service.findLatestApprovedVersion(anyString());
        assertThat(latestApprovedVersion.isPresent()).isFalse();
    }

}
