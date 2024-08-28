package com.ericsson.cifwk.tdm.application.datasources;

import static com.google.common.collect.Maps.newTreeMap;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprovalRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.DataSourceLabelBuilder.*;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_ADD;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.RECORD_VALUE_EDIT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceLabelEntityBuilder.*;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.truth.Truth.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.application.contexts.ContextService;
import com.ericsson.cifwk.tdm.api.model.DataSourceLabel;
import com.ericsson.cifwk.tdm.application.notification.NotificationService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.DataSourceLabelEntity;
import com.ericsson.cifwk.tdm.model.Version;
import com.google.common.collect.Maps;
import com.google.common.truth.Truth;

import ma.glasnost.orika.impl.DefaultMapperFactory.Builder;

@RunWith(MockitoJUnitRunner.class)
public class DataSourceServiceTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @InjectMocks
    private DataSourceService dataSourceService;

    @Mock
    private NotificationService notificationService;

    @Mock
    private DataSourceRepository dataSourceRepository;

    @Mock
    private DataSourceHelper dataSourceHelper;

    @Mock
    private SecurityService securityService;

    @Mock
    private DataSourceActionsService dataSourceActionsService;

    @Mock
    private ApprovalRequestValidationService approvalRequestValidationService;

    @Mock
    private DataSourceLabelRepository dataSourceLabelRepository;

    @Mock
    private ContextService contextService;

    @Mock
    private DataRecordRepository dataRecordRepository;

    @Mock
    private DataSourceActionRepository dataSourceActionRepository;

    @Mock
    Context system1;

    private DataSourceIdentityEntity dataSourceIdentity;

    private DataSourceLabelEntity dataSourceLabelEntity;

    private DataSourceLabel dataSourceLabel;

    @Before
    public void setUp() {
        dataSourceService.mapperFactory = new Builder().build();
        dataSourceService.filterToPredicateConverter = new FilterToPredicateConverter();
        when(securityService.getCurrentUser()).thenReturn(new AuthenticationStatus("userId", true, newArrayList(),false));

        when(contextService.findBySystemId(anyString())).thenReturn(Optional.of(system1));

        dataSourceIdentity = aDataSourceIdentityEntity()
                .withId("dataSourceId")
                .withInitialVersion()
                .withApprovalStatus(UNAPPROVED)
                .withName("myDatasource")
                .withContext("myContext")
                .build();

        dataSourceLabelEntity = aDataSourceLabelEntity()
                .withDataSourceId("dataSourceId")
                .withName("myLabel")
                .withVersion("0.0.1-SNAPSHOT")
                .build();

        dataSourceLabel = aDataSourceLabel()
                .withId("labelId")
                .withName("myLabel")
                .withDataSourceId("dataSourceId")
                .withVersion("0.0.1-SNAPSHOT")
                .build();

        doReturn(dataSourceIdentity).when(dataSourceRepository).findById(dataSourceIdentity.getId());

        doReturn(null).when(dataSourceLabelRepository).findByDataSourceIdAndVersion(dataSourceIdentity.getId(),
                dataSourceIdentity.getVersion().toString());

        doAnswer(invocationOnMock -> {
            DataSourceIdentityEntity identity = (DataSourceIdentityEntity) invocationOnMock.getArguments()[0];
            identity.setVersion(identity.getVersion().incrementMinor());
            return null;
        }).when(dataSourceActionsService)
                .applyActionsAndSave(eq(dataSourceIdentity), anyListOf(DataSourceAction.class));
    }

    @Test
    public void edit_shouldThrowIllegalArgumentException_whenZeroActions() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(DataSourceService.ERR_EDIT_NO_ACTIONS);

        dataSourceService.edit(dataSourceIdentity.getId(), emptyList());
    }

    @Test
    public void edit_shouldThrowIllegalArgumentException_whenApprovalStatus_PENDING() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(DataSourceService.ERR_EDIT_PENDING_APPROVAL);

        dataSourceIdentity.setApprovalStatus(PENDING);
        List<DataSourceAction> actions = newArrayList(aDataSourceAction().build());

        dataSourceService.edit(dataSourceIdentity.getId(), actions);
    }

    @Test
    public void edit_verifyHappyPath() {
        List<DataSourceAction> actions = asList(
                aDataSourceAction().withType(RECORD_ADD.toString()).withVersion("0.0.1").build(),
                aDataSourceAction().withType(RECORD_VALUE_EDIT.toString()).withVersion("0.0.1").build(),
                aDataSourceAction().withType(RECORD_VALUE_EDIT.toString()).withVersion("0.0.1").build()
        );

        DataSourceIdentity result = dataSourceService.edit(dataSourceIdentity.getId(), actions);

        verify(dataSourceActionsService).applyActionsAndSave(dataSourceIdentity, actions);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(dataSourceIdentity.getId());
        assertThat(result.getVersion()).isEqualTo("0.0.2-SNAPSHOT");
    }

    @Test
    public void checkVersion_shouldNotThrowAnyExceptions_whenAllVersionsAreSameOrGreater() {
        List<DataSourceAction> actions = newArrayList(
                aDataSourceAction().withVersion("0.0.1").build(),
                aDataSourceAction().withVersion("0.0.1").build(),
                aDataSourceAction().withVersion("0.0.2").build()
        );

        dataSourceService.checkVersion(dataSourceIdentity, actions);
    }

    @Test
    public void checkVersion_shouldThrowIllegalArgumentException_whenIsLessThanPreviousVersion() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(DataSourceService.ERR_EDIT_VERSION_MISMATCH);

        DataSourceIdentityEntity dataSourceIdentity = new DataSourceIdentityEntity();
        dataSourceIdentity.setVersion(new Version(0, 0, 2));

        List<DataSourceAction> actions = newArrayList(
                aDataSourceAction().withVersion("0.0.1").build(),
                aDataSourceAction().withVersion("0.0.3").build(),
                aDataSourceAction().withVersion("0.0.2").build()
        );

        dataSourceService.checkVersion(dataSourceIdentity, actions);
    }

    @Test
    public void handleApproval_shouldNotApplyApprovalStatusChange_whenInvalidApprovalRequest() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("validation error");

        ApprovalRequest request = anApprovalRequest().build();
        doThrow(new IllegalArgumentException("validation error"))
                .when(approvalRequestValidationService)
                .validate(dataSourceIdentity, request);

        dataSourceService.handleApproval(dataSourceIdentity.getId(), request);
    }

    @Test
    public void handleApproval_shouldApplyApprovalStatusChange_afterApprovalRequestValidation() {
        ApprovalRequest request = anApprovalRequest().build();

        dataSourceService.handleApproval(dataSourceIdentity.getId(), request);

        InOrder order = inOrder(approvalRequestValidationService, dataSourceActionsService, notificationService);
        order.verify(approvalRequestValidationService).validate(dataSourceIdentity, request);
        order.verify(notificationService).notifyDataSourceChange(dataSourceIdentity, request);
        order.verify(dataSourceActionsService).applyApprovalStatusChange(dataSourceIdentity, request);
        order.verifyNoMoreInteractions();
    }

    @Test
    public void verify_approval_data_source_request_notified() {
        ApprovalRequest request = anApprovalRequest().withStatus(PENDING).build();

        dataSourceService.handleApproval(dataSourceIdentity.getId(), request);

        verify(dataSourceActionsService).applyApprovalStatusChange(dataSourceIdentity, request);
        verify(notificationService).notifyDataSourceChange(any(), any());
    }

    @Test
    public void getVersions_shouldReturnVersionStrings() {
        List<Version> versions = newArrayList(
                new Version(1, 0, 0),
                new Version(2, 0, 0),
                new Version(1, 1, 0)
        );
        doReturn(versions).when(dataSourceActionsService).getActualVersions(anyString());
        List<String> result = dataSourceService.getVersions("dataSourceId");
        assertThat(result).containsExactly("2.0.0-SNAPSHOT", "1.1.0-SNAPSHOT", "1.0.0-SNAPSHOT").inOrder();
    }

    @Test
    public void getVersions_shouldReturnVersionsInOrder(){
        List<Version> versions = newArrayList(
                new Version(0, 0, 10),
                new Version(0, 0, 8)
        );
        Version approved = new Version(0, 0, 9);
        approved.setSnapshot(false);
        versions.add(approved);
        doReturn(versions).when(dataSourceActionsService).getActualVersions(anyString());
        List<String> result = dataSourceService.getVersions("dataSourceId");
        assertThat(result).hasSize(3);
        assertThat(result.get(0)).matches("0.0.10-SNAPSHOT");
        assertThat(result.get(1)).matches("0.0.9");
        assertThat(result.get(2)).matches("0.0.8-SNAPSHOT");
    }

    @Test
    public void getModifiedActions_shouldReturnModifiedActionsWithActionTypeAddAndEdit() {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setId("TEST_ID");

        Version defaultVersion = new Version("0.0.1-SNAPSHOT");

        List<DataSourceActionEntity> actions = newArrayList(
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_ADD),
                createActionWithType(dataRecord.getId(), defaultVersion, RECORD_ADD),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_ADD),
                createActionWithType(dataRecord.getId(), defaultVersion, RECORD_VALUE_EDIT),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_NAME_EDIT),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_RENAME),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.RECORD_DELETE)
        );

        List<DataSourceActionEntity> modifiedActions = dataSourceService.getModifiedActions(defaultVersion, actions, dataRecord);
        assertThat(modifiedActions).hasSize(2);

        List<DataSourceActionType> actionTypes = modifiedActions.stream()
                .map(DataSourceActionEntity::getType)
                .collect(toList());

        assertThat(actionTypes).containsExactly(RECORD_ADD, RECORD_VALUE_EDIT);
    }

    @Test
    public void getModifiedActions_shouldReturnEmptyModifiedActionsIfIdsOrVersionsAreNotTheSame() {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setId("TEST_ID");
        Version defaultVersion = new Version("0.0.1-SNAPSHOT");

        List<DataSourceActionEntity> actions = newArrayList(
                createActionWithType("WRONG_ID", defaultVersion, RECORD_ADD),
                createActionWithType(dataRecord.getId(), new Version("0.0.2-SNAPSHOT"), RECORD_VALUE_EDIT)
        );

        List<DataSourceActionEntity> modifiedActions = dataSourceService.getModifiedActions(defaultVersion, actions, dataRecord);
        assertThat(modifiedActions).isEmpty();
    }

    @Test
    public void getModifiedActions_shouldReturnEmptyModifiedActionsForNotModifiableTypes() {
        DataRecord dataRecord = new DataRecord();
        dataRecord.setId("TEST_ID");

        Version defaultVersion = new Version("0.0.1-SNAPSHOT");

        List<DataSourceActionEntity> actions = newArrayList(
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_ADD),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_ADD),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_NAME_EDIT),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.IDENTITY_KEY_RENAME),
                createActionWithType(dataRecord.getId(), defaultVersion, DataSourceActionType.RECORD_DELETE)
        );

        List<DataSourceActionEntity> modifiedActions = dataSourceService.getModifiedActions(defaultVersion, actions, dataRecord);
        assertThat(modifiedActions).isEmpty();
    }

    private DataSourceActionEntity createActionWithType(String dataRecordId,
                                                        Version version,
                                                        DataSourceActionType type) {
        DataSourceActionEntity action =
                DataSourceActionEntity.identityInitialCreate(dataRecordId, newTreeMap(), version);
        action.setType(type);
        return action;
    }

    @Test
    public void shouldThrowExceptionIfLabelAlreadyExists(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("myLabel is already applied to this version of the datasource");
        doReturn(dataSourceLabelEntity).when(dataSourceLabelRepository).findByNameAndContextId
                (dataSourceLabel.getName(), dataSourceLabel.getContextId());
        dataSourceService.checkIfLabelExistsInContext(dataSourceLabel);
    }

    @Test
    public void shouldNotThrowExceptionIfLabelDoesntExist(){
        doReturn(null).when(dataSourceLabelRepository).findByNameAndContextId
                (dataSourceLabel.getName(), dataSourceLabel.getContextId());
        dataSourceService.checkIfLabelExistsInContext(dataSourceLabel);
    }

    @Test
    public void shouldCreateDataSourceWithInitialVersion(){
        DataSource dataSource = createDataSource();
        DataSourceIdentity dataSourceIdentity = dataSourceService.create(dataSource);
        Truth.assertThat(dataSourceIdentity.getVersion()).isEqualTo(Version.INITIAL_VERSION.toString());
    }

    @Test
    public void shouldCreateDataSourceWithSetVersion(){
        String version = "1.0.1-SNAPSHOT";
        DataSource dataSource = createDataSource();
        dataSource.getIdentity().setVersion(version);
        DataSourceIdentity dataSourceIdentity = dataSourceService.create(dataSource);
        Truth.assertThat(dataSourceIdentity.getVersion()).isEqualTo(version);
    }

    private DataSource createDataSource() {
        DataSource dataSource = new DataSource();

        DataRecord record = new DataRecord();
        HashMap<String, Object> values = Maps.newHashMap();
        values.put("col-1", "value-1");
        record.setValues(values);

        List<DataRecord> records = newArrayList();
        records.add(record);

        dataSource.setRecords(records);

        DataSourceIdentity identity = new DataSourceIdentity();
        identity.setContext("System-1");
        dataSource.setIdentity(identity);
        identity.setName("testDatasource");

        return dataSource;

    }

    @Test
    public void shouldNotThrowExceptionIfLabelDoesntExistInContext(){
        DataSourceLabel newLabel = new DataSourceLabel();
        newLabel.setName("myLabel");
        newLabel.setDataSourceId("dataSourceId");
        newLabel.setContextId("contextId");
        newLabel.setVersion("0.1.0");

        when(dataSourceLabelRepository.findByNameAndContextId("myLabel", "contextId")).thenReturn(null);
        DataSourceLabel appliedLabel = dataSourceService.updateLabel(newLabel);
        assertThat(appliedLabel.getDataSourceId()).matches("dataSourceId");
        assertThat(appliedLabel.getContextId()).matches("contextId");
    }

    @Test
    public void shouldThrowExceptionIfLabelExistsInContext(){
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("myLabel is already applied to version 0.0.1-SNAPSHOT of this datasource");

        DataSourceLabel newLabel = new DataSourceLabel();
        newLabel.setName("myLabel");
        newLabel.setDataSourceId("dataSourceId");
        newLabel.setContextId("contextId");
        newLabel.setVersion("0.1.0");

        when(dataSourceLabelRepository.findByNameAndContextId("myLabel", "contextId")).thenReturn(dataSourceLabelEntity);
        dataSourceService.updateLabel(newLabel);
    }
}
