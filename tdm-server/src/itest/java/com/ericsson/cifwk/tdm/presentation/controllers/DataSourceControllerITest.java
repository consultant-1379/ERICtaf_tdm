package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequest;
import com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequestBuilder;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.DataSourceLabel;
import com.ericsson.cifwk.tdm.api.model.GroupView;
import com.ericsson.cifwk.tdm.api.model.Node;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.api.model.validation.FieldError;
import com.ericsson.cifwk.tdm.api.model.validation.ValidationError;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceRepository;
import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.DataSourceCopyRequestBuilder.aDataSourceCopyRequest;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_USER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.ASSURE_CONTEXT_ID;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.LAST_APPROVED_VERSION_IS_UNAPPROVED;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.PREVIOUSLY_APPROVED_DATA_SOURCE;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.STAR_WARS_DS_ID;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.UNAPPROVED_DATA_SOURCE;
import static com.google.common.collect.Iterables.getLast;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs", invokeCleanBeforeMethod = true)
public class DataSourceControllerITest extends AbstractControllerITest {

    private static final String NO_USER_PERMISSIONS = "User does not have permissions for this context";

    @MockBean
    private TceContextRepository contextClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Autowired
    private DataSourceRepository repository;

    @Before
    public void setUp() {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);

        scheduledTasks.runJobLoadContexts();
    }

    @Test
    public void shouldGetTestwareGroupsListFromCIPortal() throws Exception {
        scheduledTasks.runJobLoadContexts();

        List<String> testwareGroupsList = dataSourceControllerClient.getGroupsByContext("systemId-1", GroupView
                .LIST, String.class);

        assertThat(testwareGroupsList).containsExactly("group1", "group2", "group3");
    }

    @Test
    public void shouldReturnCorrectlyNestedGroupsInTree() throws Exception {
        createDataSources("datasources/parentGroupDataSources.json");
        final List<Node> groupsByContext = dataSourceControllerClient.getGroupsByContext("systemId-2",
                GroupView.TREE, Node.class);
        assertThat(groupsByContext).hasSize(1);
        final Node parentGroup = groupsByContext.get(0);
        assertThat(parentGroup.getChildren())
                .extracting("group", "name", "groupName")
                .containsExactly(tuple(false, "DS2", "com.ericsson.taf"), tuple(true, "test", "com.ericsson.taf.test"));
    }

    @Test
    public void shouldFailToCreateDataSource() throws Exception {
        when(contextClient.getContexts()).thenReturn(newArrayList(new ContextBean()));

        dataSourceControllerClient
                .tryCreateFromResource("datasources/datasource.json")
                .andExpect(status().isNotFound());
    }

    @Test
    public void create_shouldCreateDataSource() throws Exception {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);
        scheduledTasks.runJobLoadContexts();

        DataSourceIdentity dataSourceIdentity = dataSourceControllerClient.createFromResource("datasources/planets.json");

        assertThat(dataSourceIdentity.getId()).isNotEmpty();
        assertThat(dataSourceIdentity.getName()).isEqualTo("Star wars planets");
        assertThat(dataSourceIdentity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(dataSourceIdentity.getGroup()).isEqualTo("com.darth.vader.enterprise");
        assertThat(dataSourceIdentity.getContext()).isEqualTo("PDU-ABC");
        assertThat(dataSourceIdentity.getCreatedBy()).isNotEmpty();
        assertThat(dataSourceIdentity.getCreateTime()).isNotNull();

        Records records =
                dataSourceControllerClient.getRecords(dataSourceIdentity.getId());

        assertThat(records.getData().size()).isEqualTo(10);
    }

    @Test
    public void shouldGetDataSource() throws Exception {
        DataSourceIdentity created = createDataSource();

        DataSourceIdentity dataSourceIdentity =
                dataSourceControllerClient.getById(created.getId());

        assertThat(dataSourceIdentity.getId()).isEqualTo(created.getId());
        assertThat(dataSourceIdentity.getName()).isEqualTo("Star wars planets");
        assertThat(dataSourceIdentity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(dataSourceIdentity.getGroup()).isEqualTo("com.darth.vader.enterprise");
        assertThat(dataSourceIdentity.getContext()).isEqualTo("PDU-ABC");
        assertThat(dataSourceIdentity.getCreatedBy()).isNotEmpty();
        assertThat(dataSourceIdentity.getCreateTime()).isNotNull();
    }

    @Test
    public void shouldGetAllDataSources() throws Exception {
        List<DataSourceIdentity> dataSourceIdentities =
                dataSourceControllerClient.getIdentities();

        assertThat(dataSourceIdentities)
                .hasSize(11)
                .extracting(DataSourceIdentity::getName)
                .containsExactly("DS1", "DS2", "DS3", "DS4", "DS5", "DS6", "Star wars planets",
                        "Approved data source", "Unapproved data source", "DS with approval history where last "
                                + "approved version is unapproved", "acceptanceTestData");
    }

    @Test
    public void shouldGetLatestApprovedDataSourceByContextIdAndName() throws Exception {
        DataSourceIdentity dataSourceIdentity =
                dataSourceControllerClient.getApprovedDataSourceIdentityByContextAndName(ASSURE_CONTEXT_ID, "Approved"
                        + " data source", true);

        assertThat(dataSourceIdentity.getName()).isEqualTo("Approved data source");
        assertThat(dataSourceIdentity.getGroup()).isEqualTo("com.ericsson.firstds");
        assertThat(dataSourceIdentity.getVersion()).isEqualTo("0.0.2");
        assertThat(dataSourceIdentity.getApprovalStatus()).isEqualTo(APPROVED);
    }

    @Test
    public void shouldGetLatestDataSourceByContextIdAndName() throws Exception {
        createMultipleVersionsOfDataSource();
        DataSourceIdentity dataSourceIdentity =
                dataSourceControllerClient.getApprovedDataSourceIdentityByContextAndName("systemId-2", "Star wars planets",
                        false);

        assertThat(dataSourceIdentity.getName()).isEqualTo("Star wars planets");
        assertThat(dataSourceIdentity.getGroup()).isEqualTo("group3");
        assertThat(dataSourceIdentity.getVersion()).isEqualTo("0.0.3-SNAPSHOT");
        assertThat(dataSourceIdentity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    private void createMultipleVersionsOfDataSource() throws Exception {
        dataSourceControllerClient.login(new UserCredentials(TAF_USER, "taf"));
        DataSourceActionBuilder actionBuilder = aDataSourceAction()
                .withType(DataSourceActionType.IDENTITY_GROUP_EDIT.name());

        final String v2group = "group1";
        final String v3group = "group2";
        final String v4group = "group3";

        String dataSourceId = createDataSource().getId(); // version 0.0.1
        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(v2group).withVersion("0.0.1-SNAPSHOT")
                                                                   .build());
        requestApprovalAndApprove(dataSourceId);

        dataSourceControllerClient.edit(dataSourceId, newVersionAction(dataSourceId, "0.0.2-SNAPSHOT"), actionBuilder
                .withNewValue(v3group).withVersion("0.0.2-SNAPSHOT").build());
        requestApprovalAndApprove(dataSourceId);

        dataSourceControllerClient.edit(dataSourceId, newVersionAction(dataSourceId, "0.0.3-SNAPSHOT"), actionBuilder
                .withNewValue(v4group).withVersion("0.0.3-SNAPSHOT").build());
        dataSourceControllerClient.logout();
    }

    private void requestApprovalAndApprove(String dataSourceId) throws Exception {
        requestApproval(dataSourceId, tceManager());
        approve(dataSourceId);
    }

    @Test
    public void shouldGetAllDataSourcesByContext() throws Exception {
        createDataSources("datasources/datasources.json");

        assertThat(dataSourceControllerClient.getAllByContextId("systemId-1").size()).isEqualTo(6);
        assertThat(dataSourceControllerClient.getAllByContextId("systemId-2").size()).isEqualTo(2);
        assertThat(dataSourceControllerClient.getAllByContextId("systemId-3").size()).isEqualTo(2);
        assertThat(dataSourceControllerClient.getAllByContextId("systemId-4").size()).isEqualTo(1);
        assertThat(dataSourceControllerClient.getAllByContextId("systemId-5").size()).isEqualTo(1);
    }

    private List<DataSourceIdentity> createDataSources(final String dataSourcesFilePath) throws Exception {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);

        scheduledTasks.runJobLoadContexts();

        List<DataSource> dataSourceIdentityList =
                parseListFile(dataSourcesFilePath, DataSource.class);

        List<DataSourceIdentity> dataSourceIdentities = newArrayList();
        for (DataSource dataSource : dataSourceIdentityList) {
            DataSourceIdentity dataSourceIdentity = dataSourceControllerClient.createFromObject(dataSource);
            dataSourceIdentities.add(dataSourceIdentity);
            assertThat(dataSourceIdentity.getName()).isNotNull();
        }
        return dataSourceIdentities;
    }

    @Test
    public void shouldGetDataRecords() throws Exception {
        DataSourceIdentity created = createDataSource();

        Records records =
                dataSourceControllerClient.getRecords(created.getId());

        assertThat(records.getData().size()).isGreaterThan(0);

        Optional<DataRecord> maybeRecord = records.getData().stream()
                .filter(r -> "Alderaan".equals(r.getValues().get("name")))
                .findFirst();

        assertThat(maybeRecord.isPresent()).isTrue();

        DataRecord record = maybeRecord.get();

        assertThat(record.getDataSourceId()).isEqualTo(created.getId());

        assertThat(record.getValues()).containsEntry("name", "Alderaan");
        assertThat(record.getValues()).containsEntry("rotation_period", "24");
        assertThat(record.getValues()).containsEntry("orbital_period", "364");
        assertThat(record.getValues()).containsEntry("diameter", "12500");
        assertThat(record.getValues()).containsEntry("climate", "temperate");
        assertThat(record.getValues()).containsEntry("gravity", "1 standard");
        assertThat(record.getValues()).containsEntry("terrain", "grasslands, mountains");
        assertThat(record.getValues()).containsEntry("surface_water", "40");
        assertThat(record.getValues()).containsEntry("population", "2000000000");
    }

    @Test
    public void shouldGetDataRecordsWithSpecifiedColumns() throws Exception {
        DataSourceIdentity created = createDataSource();
        Records records =
                dataSourceControllerClient.getRecordsWithSpecifiedColumns(created.getId(), "name,climate");

        assertThat(records.getData()).isNotEmpty()
                .extracting(DataRecord::getValues)
                .flatExtracting(Map::keySet)
                .containsOnly("name", "climate");
    }

    @Test
    public void shouldGetDataRecordsWithModifiedColumnsForTheCreatedRecord() throws Exception {
        DataSourceIdentity created = createDataSource();

        Records records =
                dataSourceControllerClient.getRecords(created.getId());

        assertThat(records.getData()).isNotEmpty()
                .extracting(DataRecord::getModifiedColumns).isNotEmpty();
    }

    @Test
    public void shouldGetDataRecordsWithOldValuesForApprovedDataRecord() throws Exception {
        dataSourceControllerClient.login(new UserCredentials(TAF_USER, "taf"));
        DataSourceIdentity created = createDataSource();
        requestApprovalAndApprove(created.getId());
        Records snapshotRecords = dataSourceControllerClient.getRecords(created.getId(), "0.0.2-SNAPSHOT");
        DataSourceAction action = aDataSourceAction()
                .withId(snapshotRecords.getData().get(0).getId())
                .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                .withKey("name")
                .withNewValue("my new Value")
                .withVersion("0.0.2-SNAPSHOT")
                .withLocalTimestamp(1460009673453L)
                .build();
        dataSourceControllerClient.edit(created.getId(), newVersionAction(created.getId(), "0.0.2.SNAPSHOT"), action);
        Records records = dataSourceControllerClient.getRecords(created.getId());
        assertThat(records.getData())
                .isNotEmpty()
                .extracting(DataRecord::getOldValues)
                .isNotEmpty()
                .hasAtLeastOneElementOfType(Map.class);
    }

    @Test
    public void shouldGetDataRecordWithSpecifiedModifiedColumnOnly() throws Exception {
        DataSourceAction[] actions = new DataSourceAction[]{
                aDataSourceAction()
                        .withId("local_f911bda7-6c47-24af-bafc-4a5f22431f40")
                        .withType(DataSourceActionType.RECORD_ADD.name())
                        .withKey("orbital_period")
                        .withNewValue("1")
                        .withVersion("0.0.1-SNAPSHOT")
                        .withLocalTimestamp(1460009665143L)
                        .build(),
                aDataSourceAction()
                        .withId("local_f911bda7-6c47-24af-bafc-4a5f22431f40")
                        .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                        .withKey("name")
                        .withNewValue("Alpha Laputa IV")
                        .withVersion("0.0.1-SNAPSHOT")
                        .withLocalTimestamp(1460009673453L)
                        .build(),
        };

        String dataSourceId = createDataSource().getId();
        dataSourceControllerClient.edit(dataSourceId, actions);

        List<String> modifiedColumns = newArrayList("orbital_period", "name");

        List<DataRecord> entities = dataSourceControllerClient.getRecords(dataSourceId).getData().stream()
                .filter(record -> record.getModifiedColumns() != null
                                    && record.getModifiedColumns().containsAll(modifiedColumns)
                                    && record.getModifiedColumns().size() == 2
                ).collect(toList());

        assertThat(entities).isNotEmpty();
    }

    @Test
    public void testPostAndGet() throws Exception {
        DataSourceIdentity dataSourceIdentityEntity = createDataSource();

        assertThat(dataSourceIdentityEntity.getName()).isEqualTo("Star wars planets");

        DataSourceIdentity identity = dataSourceControllerClient.getById(dataSourceIdentityEntity.getId());
        assertThat(identity.getName()).isEqualTo("Star wars planets");

        Records entities = dataSourceControllerClient.getRecords(dataSourceIdentityEntity.getId());
        assertThat(entities.getData()).hasSize(10);
    }

    @Test
    public void testOnlyDeleteDataRecordInVersion() throws Exception {
        dataSourceControllerClient.login(new UserCredentials(TAF_USER, "taf"));
        DataSourceIdentity dataSourceIdentity = createDataSource();

        final String dataSourceId = dataSourceIdentity.getId();
        requestApprovalAndApprove(dataSourceId);

        final String snapshotVersion = "0.0.2-SNAPSHOT";
        List<DataRecord> records = deleteARecord(dataSourceId, snapshotVersion);

        Records updatedRecords = dataSourceControllerClient.getRecords(dataSourceId, snapshotVersion);

        assertThat(updatedRecords.getData()).hasSize(records.size()-1);

        final String approvedVersion = "0.0.1";
        Records approvedRecords = dataSourceControllerClient.getRecords(dataSourceId, approvedVersion);

        assertThat(approvedRecords.getData()).hasSize(updatedRecords.getData().size()+1);

    }

    @Test
    public void edit_testAddDeleteDataRecords() throws Exception {
        DataSourceAction[] actions = new DataSourceAction[]{
                aDataSourceAction()
                        .withId("local_f911bda7-6c47-24af-bafc-4a5f22431f40")
                        .withType(DataSourceActionType.RECORD_ADD.name())
                        .withKey("orbital_period")
                        .withNewValue("1")
                        .withVersion("0.0.1")
                        .withLocalTimestamp(1460009665143L)
                        .build(),
                aDataSourceAction()
                        .withId("local_f911bda7-6c47-24af-bafc-4a5f22431f40")
                        .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                        .withKey("name")
                        .withNewValue("Alpha Laputa IV")
                        .withVersion("0.0.1")
                        .withLocalTimestamp(1460009673453L)
                        .build(),
                aDataSourceAction()
                        .withId("local_f6edf06e-a732-c5a1-1f2d-e9b5f259b889")
                        .withType(DataSourceActionType.RECORD_ADD.name())
                        .withKey("orbital_period")
                        .withNewValue("2")
                        .withVersion("0.0.1")
                        .withLocalTimestamp(1460009674860L)
                        .build(),
                aDataSourceAction()
                        .withId("local_f6edf06e-a732-c5a1-1f2d-e9b5f259b889")
                        .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                        .withKey("name")
                        .withNewValue("Aaamazzara")
                        .withVersion("0.0.1")
                        .withLocalTimestamp(1460009679617L)
                        .build(),
                aDataSourceAction()
                        .withId("local_f911bda7-6c47-24af-bafc-4a5f22431f40")
                        .withType(DataSourceActionType.RECORD_DELETE.name())
                        .withVersion("0.0.1")
                        .withLocalTimestamp(1460009682641L)
                        .build()
        };

        String dataSourceId = createDataSource().getId();

        dataSourceControllerClient.edit(dataSourceId, actions);

        Records entities = dataSourceControllerClient.getRecords(dataSourceId);
        assertThat(entities.getData()).hasSize(11);
        Optional<DataRecord> added = entities.getData().stream().filter(r -> "Aaamazzara".equals(r.getValues().get("name"))).findFirst();
        assertThat(added.get().getValues()).containsEntry("orbital_period", "2");
    }

    @Test
    public void edit_testDataSourceVersions() throws Exception {
        DataSourceActionBuilder actionBuilder = aDataSourceAction()
                .withType(DataSourceActionType.IDENTITY_NAME_EDIT.name());

        final String v2name = "Star Wars Planets";
        final String v3name = "Planets of Star Wars";
        final String v4name = "SWP";

        String dataSourceId = createDataSource().getId(); // version 0.0.1-SNAPSHOT
        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(v2name).withVersion("0.0.1-SNAPSHOT").build());

        DataSourceIdentity identity = dataSourceControllerClient.getById(dataSourceId);
        assertThat(identity.getName()).isEqualTo(v2name);
        assertThat(identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");

        List<String> versions = dataSourceControllerClient.getVersions(dataSourceId);
        assertThat(versions).containsExactly("0.0.1-SNAPSHOT");

        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(v3name).withVersion("0.0.1-SNAPSHOT").build());

        DataSourceIdentity v2identity = dataSourceControllerClient.getById(dataSourceId, "0.0.1-SNAPSHOT");
        assertThat(v2identity.getName()).isEqualTo(v3name);
        assertThat(v2identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");

        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(v4name).withVersion("0.0.1-SNAPSHOT").build());

        DataSourceIdentity v3identity = dataSourceControllerClient.getById(dataSourceId, "0.0.1-SNAPSHOT");
        assertThat(v3identity.getName()).isEqualTo(v4name);
        assertThat(v3identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
    }

    @Test
    public void edit_testDataRecordsVersions() throws Exception {
        String dataSourceId = createDataSource().getId(); // version 0.0.1-SNAPSHOT
        Records entities = dataSourceControllerClient.getRecords(dataSourceId);
        DataRecord entity = getLast(entities.getData());
        String entityId = entity.getId();

        DataSourceActionBuilder actionBuilder = aDataSourceAction()
                .withId(entityId)
                .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                .withKey("name");

        final String name1 = "Brax";
        final String name2 = "Bre'el IV";
        final String name3 = "T'Lani Prime";

        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(name1).withVersion("0.0.1-SNAPSHOT").build());

        List<String> versions = dataSourceControllerClient.getVersions(dataSourceId);
        assertThat(versions).containsExactly("0.0.1-SNAPSHOT");

        Records entities1 = dataSourceControllerClient.getRecords(dataSourceId, "0.0.1-SNAPSHOT");

        assertThat(entities1.getData()).hasSize(entities.getData().size());
        Optional<DataRecord> entity1 = entities1.getData().stream().filter(e -> entityId.equals(e.getId())).findFirst();
        assertThat(entity1.isPresent()).isTrue();
        assertThat(entity1.get().getValues()).containsEntry("name", name1);
        assertThat(entity1.get().getValues().size()).isEqualTo(entity.getValues().size());

        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(name2).withVersion("0.0.1-SNAPSHOT").build());

        Records entities2 = dataSourceControllerClient.getRecords(dataSourceId, "0.0.1-SNAPSHOT");

        assertThat(entities2.getData()).hasSize(entities.getData().size());
        Optional<DataRecord> entity2 = entities2.getData().stream().filter(e -> entityId.equals(e.getId())).findFirst();
        assertThat(entity2.isPresent()).isTrue();
        assertThat(entity2.get().getValues()).containsEntry("name", name2);
        assertThat(entity2.get().getValues().size()).isEqualTo(entity.getValues().size());

        dataSourceControllerClient.edit(dataSourceId, actionBuilder.withNewValue(name3).withVersion("0.0.1-SNAPSHOT").build());

        Records entities3 = dataSourceControllerClient.getRecords(dataSourceId, "0.0.1-SNAPSHOT");

        assertThat(entities3.getData()).hasSize(entities.getData().size());
        Optional<DataRecord> entity3 = entities3.getData().stream().filter(e -> entityId.equals(e.getId())).findFirst();
        assertThat(entity3.isPresent()).isTrue();
        assertThat(entity3.get().getValues()).containsEntry("name", name3);
        assertThat(entity3.get().getValues().size()).isEqualTo(entity.getValues().size());

    }

    @Test
    public void getApprovedById_shouldReturnNotFound_whenNotApproved() throws Exception {
        dataSourceControllerClient.tryGetApprovedById(UNAPPROVED_DATA_SOURCE)
                .andExpect(status().isNotFound());
    }

    @Test
    public void getApprovedById_shouldReturnLatestApprovedVersion() throws Exception {
        DataSourceIdentity byId = dataSourceControllerClient.getById(PREVIOUSLY_APPROVED_DATA_SOURCE);
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        assertThat(byId.getVersion()).isEqualTo("0.0.3-SNAPSHOT");
        assertThat(byId.getApprovalStatus()).isEqualTo(UNAPPROVED);

        assertThat(approvedById.getVersion()).isEqualTo("0.0.2");
        assertThat(approvedById.getApprovalStatus()).isEqualTo(APPROVED);
    }

    @Test
    public void getApprovedById_shouldUnApprovePreviouslyApprovedVersionIfLastApprovedVersionIsUnapproved() throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(LAST_APPROVED_VERSION_IS_UNAPPROVED);

        assertThat(approvedById.getVersion()).isEqualTo("0.0.4");
        assertThat(approvedById.getApprovalStatus()).isEqualTo(APPROVED);

        unapprove(LAST_APPROVED_VERSION_IS_UNAPPROVED);

        approvedById = dataSourceControllerClient.getApprovedById(LAST_APPROVED_VERSION_IS_UNAPPROVED);
        assertThat(approvedById.getVersion()).isEqualTo("0.0.2");
        assertThat(approvedById.getApprovalStatus()).isEqualTo(APPROVED);
    }

    @Test
    public void shouldDeleteDatasource() throws Exception {
        DataSourceIdentity dataSource = createDataSource();

        List<DataSourceIdentity> identities = dataSourceControllerClient.getIdentities();

        DataSourceIdentity deletedIdentity = dataSourceControllerClient.deleteById(dataSource.getId());

        List<String> identityIdsAfterDelete = dataSourceControllerClient.getIdentities().stream()
                .map(DataSourceIdentity::getId)
                .collect(toList());

        assertThat(deletedIdentity.getId()).isEqualTo(dataSource.getId());
        assertThat(identityIdsAfterDelete).doesNotContain(dataSource.getId());
        assertThat(identities.size()).isGreaterThan(identityIdsAfterDelete.size());
    }

    @Test
    public void shouldDeleteDataSourceLabels() throws Exception {
        dataSourceControllerClient.login(new UserCredentials(TAF_USER, "taf"));
        DataSourceIdentity dataSource = createDataSource();

        requestApprovalAndApprove(dataSource.getId());

        DataSourceLabel dataSourceLabel1 = createDataSourceLabel(dataSource, "Heckle", "0.0.1");
        addContextTo(dataSourceLabel1, "systemId-2");
        DataSourceLabel label1 = dataSourceControllerClient.createLabel(dataSourceLabel1);
        assertThat(label1.getId()).isNotEmpty();
        final String dataSourceId = dataSource.getId();
        DataSourceAction action = aDataSourceAction().withId(dataSourceId)
                                                     .withType(DataSourceActionType.RECORD_ADD.name())
                                                     .withKey("name")
                                                     .withNewValue("Alpha Laputa IV")
                                                     .withVersion("0.0.2-SNAPSHOT")
                                                     .withLocalTimestamp(1460009673453L)
                                                     .build();
        dataSourceControllerClient.edit(dataSourceId, newVersionAction(dataSourceId, "0.0.2-SNAPSHOT"), action);
        requestApprovalAndApprove(dataSource.getId());
        DataSourceLabel dataSourceLabel2 = createDataSourceLabel(dataSource, "Jeckle", "0.0.2");
        addContextTo(dataSourceLabel2, "systemId-2");
        DataSourceLabel label2 = dataSourceControllerClient.createLabel(dataSourceLabel2);
        assertThat(label2.getId()).isNotEmpty();

        dataSourceControllerClient.deleteById(dataSource.getId());

        ResultActions heckle = dataSourceControllerClient.tryGetDataSourceByLabel("Heckle");
        heckle.andExpect(status().isNotFound());

        ResultActions jeckle = dataSourceControllerClient.tryGetDataSourceByLabel("Jeckle");
        jeckle.andExpect(status().isNotFound());

    }

    @Test
    public void copy_shouldReturnFieldError_whenDataSourceId_null() throws Exception {
        DataSourceCopyRequest request = copyRequest().withDataSourceId(null).build();

        MockHttpServletResponse response = dataSourceControllerClient.tryCopy(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        ValidationError validationError = parseObject(response.getContentAsString(), ValidationError.class);
        assertThat(validationError.getFieldErrors()).containsExactly(
                new FieldError("dataSourceId", "may not be null")
        );
    }

    @Test
    public void copy_shouldReturnFieldError_whenVersion_null() throws Exception {
        DataSourceCopyRequest request = copyRequest().withVersion(null).build();

        MockHttpServletResponse response = dataSourceControllerClient.tryCopy(request)
                .andExpect(status().isBadRequest())
                .andReturn().getResponse();

        ValidationError validationError = parseObject(response.getContentAsString(), ValidationError.class);
        assertThat(validationError.getFieldErrors()).containsExactly(
                new FieldError("version", "may not be null")
        );
    }

    @Test
    public void copy_shouldCopyDataSource() throws Exception {
        DataSourceCopyRequest request = copyRequest().build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withNewName() throws Exception {
        DataSourceCopyRequest request = copyRequest()
                .withNewName("new Name").build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo(request.getNewName());
        assertThat(identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withNewGroup() throws Exception {
        DataSourceCopyRequest request = copyRequest().withNewGroup("new.group").build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getGroup()).isEqualTo(request.getNewGroup());
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withBaseVersionTrueNoSnapshot() throws Exception {
        DataSourceCopyRequest request = copyRequest()
                .withBaseVersion(true)
                .withVersion("0.0.2")
                .build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getVersion()).isEqualTo("0.0.3-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withBaseVersionTrueSnapshot() throws Exception {
        DataSourceCopyRequest request = copyRequest()
                .withBaseVersion(true)
                .withVersion("0.0.3-SNAPSHOT")
                .build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getVersion()).isEqualTo("0.0.3-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withBaseVersionFalseNoSnapshot() throws Exception {
        DataSourceCopyRequest request = copyRequest()
                .withBaseVersion(false)
                .withVersion("0.0.2")
                .build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withBaseVersionFalseSnapshot() throws Exception {
        DataSourceCopyRequest request = copyRequest()
                .withBaseVersion(false)
                .withVersion("0.0.3-SNAPSHOT")
                .build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo("Star wars planets Copy");
        assertThat(identity.getVersion()).isEqualTo("0.0.1-SNAPSHOT");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataSource_withNewContext() throws Exception {
        DataSourceCopyRequest request = copyRequest().withNewContextId("systemId-2").build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getContext()).isEqualTo("PDU-ABC");
        assertThat(identity.getContextId()).isEqualTo("systemId-2");
        assertThat(identity.getApprovalStatus()).isEqualTo(UNAPPROVED);
    }

    @Test
    public void copy_shouldCopyDataRecords() throws Exception {
        DataSourceCopyRequest request = copyRequest().build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        Records records = dataSourceControllerClient.getRecords(identity.getId());
        assertThat(records.getData()).hasSize(10);
    }

    @Test
    public void createDataSourceLabel() throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "Han Solo", "0.0.2");
        addContextTo(dataSourceLabel, "systemId-2");
        DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getId()).isNotEmpty();

        List<DataRecord> records = getDataRecords(label);
        assertThat(records.size()).isEqualTo(0);
    }

    @Test
    public void createDataSourceLabelInTwoContexts() throws Exception{
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);
        DataSourceIdentity starWars = dataSourceControllerClient.getApprovedById(STAR_WARS_DS_ID);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "Han Solo", approvedById.getVersion());
        addContextTo(dataSourceLabel, "systemId-2");
        DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getDataSourceId()).isEqualTo(approvedById.getId());

        dataSourceLabel = createDataSourceLabel(starWars, "Han Solo", starWars.getVersion());
        addContextTo(dataSourceLabel, "systemId-3");

        label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getDataSourceId()).isEqualTo(starWars.getId());

        List<DataSourceLabel> totalLabels = dataSourceControllerClient.getDataSourceLabels("Han Solo");

        assertThat(totalLabels.size()).isEqualTo(2);

        ResultActions result = dataSourceControllerClient.deleteLabel("Han Solo", "systemId-2");
        result.andExpect(status().isNoContent());

        totalLabels = dataSourceControllerClient.getDataSourceLabels("Han Solo");

        assertThat(totalLabels.size()).isEqualTo(1);
        assertThat(totalLabels.get(0).getContextId()).matches("systemId-3");

    }

    @Test
    public void createDataSourceLabelAndTrySetToAnotherDataSourceInContext() throws Exception {
        DataSourceIdentity lastApprovedVersion = dataSourceControllerClient.getApprovedById(LAST_APPROVED_VERSION_IS_UNAPPROVED);
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "Han Solo", approvedById.getVersion());
        addContextTo(dataSourceLabel, "systemId-3");
        DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getDataSourceId()).isEqualTo(approvedById.getId());

        List<DataRecord> records = getDataRecords(label);
        assertThat(records.size()).isEqualTo(0);

        List<DataSourceLabel> totalLabels = dataSourceControllerClient.getDataSourceLabels("Han Solo");

        assertThat(totalLabels.size()).isEqualTo(1);


        // set label to second datasource
        dataSourceLabel.setDataSourceId(lastApprovedVersion.getId());
        dataSourceLabel.setVersion(lastApprovedVersion.getVersion());
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        totalLabels = dataSourceControllerClient.getDataSourceLabels("Han Solo");
        assertThat(totalLabels.size()).isEqualTo(1);

        assertThat(label.getDataSourceId()).isEqualTo(approvedById.getId());

        records = getDataRecords(label);
        assertThat(records.size()).isEqualTo(0);

        //set label on datasource with new name
        dataSourceLabel.setName("Luke");
        dataSourceLabel.setDataSourceId(approvedById.getId());
        dataSourceLabel.setVersion(approvedById.getVersion());
        addContextTo(dataSourceLabel, "systemId-3");
        label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getDataSourceId()).isEqualTo(approvedById.getId());

        dataSourceControllerClient.tryGetDataSourceByLabel("Han Solo").andExpect(status().isNotFound());
    }

    @Test
    public void createBadDataSourceLabel() throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "", "0.0.2");

        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        //should not save with space
        dataSourceLabel.setName(" ");
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        //should not save with snapshot
        dataSourceLabel.setName("Darth Vader");
        dataSourceLabel.setVersion("0.0.1-SNAPSHOT");
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        //should not save with same name
        dataSourceLabel.setName("Yoda");
        dataSourceLabel.setVersion("0.0.2");
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel);
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());
    }

    @Test
    public void createDataSourceLabel_withNullFields() throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, null, "0.0.2");

        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        //should not save with space
        dataSourceLabel.setName("Test");
        dataSourceLabel.setVersion(null);
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

        //should not save with snapshot
        dataSourceLabel.setName("Darth Vader");
        dataSourceLabel.setVersion("0.0.2");
        dataSourceLabel.setDataSourceId(null);
        dataSourceControllerClient.tryCreateLabel(dataSourceLabel).andExpect(status().isBadRequest());

    }

    @Test
    public void getDataSourceLabels() throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "wookie2", "0.0.2");
        addContextTo(dataSourceLabel, "systemId-3");
        dataSourceControllerClient.createLabel(dataSourceLabel);
        List<DataSourceLabel> dataSourceLabels = dataSourceControllerClient
                .getDataSourceLabels("woo");

        assertThat(dataSourceLabels.size()).isEqualTo(2);
    }

    @Test
    public void shouldDeleteLabel()  throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);
        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "myLabel", "0.0.2");
        addContextTo(dataSourceLabel, "systemId-3");
        DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getId()).isNotEmpty();

        List<DataSourceLabel> dataSourceLabels = dataSourceControllerClient.getDataSourceLabels("myLabel");
        assertThat(dataSourceLabels.size()).isEqualTo(1);

        ResultActions result = dataSourceControllerClient.deleteLabel("myLabel", "systemId-3");
        result.andExpect(status().isNoContent());
    }

    @Test
    public void shouldDeleteLabel_When_Approved_DataSource_Is_Unapproved()  throws Exception {
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);
        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "myLabel", "0.0.2");
        addContextTo(dataSourceLabel, "systemId-2");
        DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
        assertThat(label.getId()).isNotEmpty();
        List<DataSourceLabel> dataSourceLabels = dataSourceControllerClient.getDataSourceLabels("myLabel");
        assertThat(dataSourceLabels.size()).isEqualTo(1);
        unapprove(PREVIOUSLY_APPROVED_DATA_SOURCE);

        List<DataSourceLabel> dataSourceLabelsShouldBeEmpty = dataSourceControllerClient.getDataSourceLabels("myLabel");
        assertThat(dataSourceLabelsShouldBeEmpty.size()).isEqualTo(0);
    }

    @Test
    public void userHasNoPermissionsToCreateDataSourceToContext() throws Exception{
        try {
            dataSourceControllerClient.tryCreateFromResource("datasources/badContextPlanets.json");
            fail();
        } catch (NestedServletException e) {
            assertThat(e.getMessage().contains(NO_USER_PERMISSIONS)).isTrue();
        }
    }

    @Test
    public void userHasNoPermissionsToCreateDataSourceLabel() throws Exception{
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "Darth Sidious", approvedById.getVersion());
        addContextTo(dataSourceLabel, "badContext");
        try {
            dataSourceControllerClient.createLabel(dataSourceLabel);
            fail();
        } catch (NestedServletException e) {
            assertThat(e.getMessage().contains(NO_USER_PERMISSIONS)).isTrue();
        }
    }

    @Test
    public void userHasNoPermissionsToCopyDataSource() throws Exception {
        DataSourceCopyRequest request = copyRequest().build();
        request.setNewContextId(null);

        try {
            dataSourceControllerClient.tryCopy(request);
            fail();
        } catch (NestedServletException e) {
            assertThat(e.getMessage().contains(NO_USER_PERMISSIONS)).isTrue();
        }
    }

    @Test
    public void userHasNoPermissionsToDeleteDataSourceLabel() throws Exception{
        DataSourceIdentity approvedById = dataSourceControllerClient.getApprovedById(PREVIOUSLY_APPROVED_DATA_SOURCE);

        DataSourceLabel dataSourceLabel = createDataSourceLabel(approvedById, "Darth Maul", approvedById.getVersion());
        addContextTo(dataSourceLabel, "systemId-2");
        try {
            DataSourceLabel label = dataSourceControllerClient.createLabel(dataSourceLabel);
            dataSourceControllerClient.deleteLabel(label.getDataSourceId(), "wrongContext");
            fail();
        } catch (NestedServletException e) {
            assertThat(e.getMessage().contains(NO_USER_PERMISSIONS)).isTrue();
        }
    }

    private List<DataRecord> getDataRecords(final DataSourceLabel label) throws Exception {
        DataSourceIdentity entity = dataSourceControllerClient.getDataSourceByLabel(label.getName(), label.getContextId());
        return dataSourceControllerClient.getRecords(entity.getId()).getData();
    }

    private DataSourceCopyRequestBuilder copyRequest() {
        return aDataSourceCopyRequest()
                .withNewName("Star wars planets Copy")
                .withDataSourceId("56c5ddd29759e577fc68ab74")
                .withVersion("0.0.2")
                .withNewContextId("systemId-2");
    }

    private DataSourceLabel createDataSourceLabel(final DataSourceIdentity approvedById, String name, String version) {
        DataSourceLabel dataSourceLabel = new DataSourceLabel();
        dataSourceLabel.setName(name);
        dataSourceLabel.setVersion(version);
        dataSourceLabel.setDataSourceId(approvedById.getId());
        return dataSourceLabel;
    }

    private void addContextTo(final DataSourceLabel dataSourceLabel, final String contextId){
        dataSourceLabel.setContextId(contextId);
    }

    @Test
    public void shouldGetDataSourceByNameAndContextPath() throws Exception {
        DataSourceIdentity created = createDataSource();
        DataSourceIdentity dataSourceIdentity =
                dataSourceControllerClient.getDataSourceByContextPathAndName("System/PDU-ABC", created.getName());
        assertThat(dataSourceIdentity.getId()).isEqualTo(created.getId());
    }

    @Test
    public void shouldFailsForTheWrongContextPath_getDataSourceByNameAndContextPath() throws Exception {
        DataSourceIdentity created = createDataSource();
        ResultActions result = dataSourceControllerClient.tryGetDataSourceByContextPathAndName("System/PDU-ABC/FAILED", created.getName());
        result.andExpect(status().isNotFound());
    }

    @Test
    public void shouldFailsForTheWrongDataSourceName_getDataSourceByNameAndContextPath() throws Exception {
        ResultActions result = dataSourceControllerClient.tryGetDataSourceByContextPathAndName("System/PDU-ABC", "WRONG_DS_NAME");
        result.andExpect(status().isNotFound());
    }

    @Test
    public void shouldGetDataRecordsWithIdAndVersionAndSpecifiedColumns() throws Exception {
        DataSourceIdentity created = createDataSource();
        Records records =
                dataSourceControllerClient.getRecordsByIdAndVersionAndSpecifiedColumns(created.getId(), created
                        .getVersion(), "name,climate");

        assertThat(records.getData())
                .extracting(DataRecord::getValues)
                .flatExtracting(Map::keySet)
                .containsOnly("name", "climate");
    }

    @Test
    public void shouldGetDataRecordsWithIdAndVersionWithFilter() throws Exception {
        DataSourceIdentity created = createDataSource();
        Records records =
                dataSourceControllerClient.getRecordsByIdAndVersionWithFilter(created.getId(), created
                        .getVersion(), "name=Hoth");

        assertThat(records.getData().size()).isEqualTo(1);
    }

    @Test
    public void shouldGetDataRecordsWithIdAndVersionWithFilterAndSpecifiedColumns() throws Exception {
        DataSourceIdentity created = createDataSource();
        Records records =
                dataSourceControllerClient.getRecordsByIdAndVersionWithFilterAndSpecifiedColumns(created.getId(), created
                        .getVersion(), "name,climate", "name=Hoth");

        assertThat(records.getData().size()).isEqualTo(1);

        assertThat(records.getData()).isNotEmpty()
                .extracting(DataRecord::getValues)
                .flatExtracting(Map::keySet)
                .containsOnly("name", "climate");
    }

    @Test
    public void testColumnChange() throws Exception {
        DataSourceIdentity dataSourceIdentityEntity = createDataSource();

        Records entities = dataSourceControllerClient.getRecords(dataSourceIdentityEntity.getId());
        Map<String, Object> columnOrder = entities.getMeta().getColumnOrder();
        assertThat(columnOrder).hasSize(9);

        Map<String, Object> newColumnOrder = new HashMap<>();
        List<DataSourceAction> dataSourceActions = new ArrayList<>();
        int index = columnOrder.size();
        for (String key : columnOrder.keySet()) {
            --index;
            newColumnOrder.put(key, index);
            DataSourceAction action = aDataSourceAction()
                    .withId(dataSourceIdentityEntity.getId())
                    .withType(DataSourceActionType.COLUMN_ORDER_CHANGE.name())
                    .withNewValue(String.valueOf(index))
                    .withVersion(dataSourceIdentityEntity.getVersion())
                    .withKey(key)
                    .withLocalTimestamp(new Date().getTime())
                    .build();

            dataSourceActions.add(action);
        }

        dataSourceControllerClient.edit(dataSourceIdentityEntity.getId(), dataSourceActions);

        Records entities2 = dataSourceControllerClient.getRecords(dataSourceIdentityEntity.getId());
        Map<String, Object> columnOrder2 = entities2.getMeta().getColumnOrder();
        assertThat(columnOrder2).hasSize(9);

        newColumnOrder.keySet().stream().forEach(key ->
            assertThat(newColumnOrder.get(key).toString()).isEqualToIgnoringCase(columnOrder2.get(key).toString())
        );
    }

    @Test
    public void copy_shouldCopyDataSource_withColumnOrder() throws Exception {
        DataSourceIdentity dataSourceIdentityEntity = createDataSource();

        DataSourceCopyRequest request = DataSourceCopyRequestBuilder.aDataSourceCopyRequest()
                .withVersion(dataSourceIdentityEntity.getVersion())
                .withDataSourceId(dataSourceIdentityEntity.getId())
                .withNewContextId("systemId-2")
                .withNewName("Column Order").build();

        DataSourceIdentity identity = dataSourceControllerClient.copy(request);

        assertThat(identity.getId()).isNotEqualTo(request.getDataSourceId());
        assertThat(identity.getName()).isEqualTo(request.getNewName());

        Records entities = dataSourceControllerClient.getRecords(identity.getId());
        Map<String, Object> columnOrderInfo = entities.getMeta().getColumnOrder();
        assertThat(columnOrderInfo).isNotEmpty();
    }

    @Test
    public void shouldOnlyCreateSnapshotVersionWhenApprovedIsEdited() throws Exception {
        dataSourceControllerClient.login(new UserCredentials("taf", "taf"));
        String dataSourceId = createDataSource().getId();
        requestApprovalAndApprove(dataSourceId);
        DataSourceIdentity latestDataSource = dataSourceControllerClient.getById(dataSourceId);
        assertThat(latestDataSource.getVersion()).isEqualToIgnoringCase("0.0.1");
        DataSourceAction action = aDataSourceAction().withId(dataSourceId)
                                                     .withType(DataSourceActionType.RECORD_ADD.name())
                                                     .withKey("name")
                                                     .withNewValue("Alpha Laputa IV")
                                                     .withVersion("0.0.2-SNAPSHOT")
                                                     .withLocalTimestamp(1460009673453L)
                                                     .build();
        DataSourceAction newVersion = newVersionAction(dataSourceId, "0.0.2-SNAPSHOT");
        dataSourceControllerClient.edit(dataSourceId, newVersion, action);
        DataSourceIdentity snapshotDataSource = dataSourceControllerClient.getById(dataSourceId);
        assertThat(snapshotDataSource.getVersion()).isEqualToIgnoringCase("0.0.2-SNAPSHOT");
        dataSourceControllerClient.logout();
    }

}
