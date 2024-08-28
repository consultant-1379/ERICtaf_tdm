package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.api.model.LockType;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.ericsson.cifwk.tdm.application.locks.LockDataService;
import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.cifwk.tdm.model.Execution;
import com.ericsson.cifwk.tdm.presentation.controllers.client.ExecutionControllerClient;
import com.ericsson.cifwk.tdm.presentation.controllers.client.LockControllerClient;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.LockType.EXCLUSIVE;
import static com.ericsson.cifwk.tdm.api.model.LockType.SHARED;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseList;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObjectFile;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_2;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_USER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs")
public class LockControllerITest extends AbstractControllerITest {

    private static final String TESTWARE_1 = "testWare1";
    private static final String TESTWARE_2 = "testWare2";

    private static final int LOCK_TIMEOUT_SECONDS = 3;
    private final UserCredentials taf = new UserCredentials(TAF_USER, "taf");
    private final UserCredentials taf2 = new UserCredentials(TAF_2, "taf2");

    @Autowired
    private LockControllerClient lockControllerClient;

    @Autowired
    private ExecutionControllerClient executionControllerClient;

    @Autowired
    private LockDataService lockDataService;

    @MockBean
    private TceContextRepository contextClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Before
    public void setUp() {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);
        scheduledTasks.runJobLoadContexts();
    }

    @DirtiesContext
    @Test
    public void shouldCreateDataRecordLock() throws Exception {
        Lock lock = createLockObject(createDataSource("shouldCreateDataRecordLock").getId(),
                EXCLUSIVE, TESTWARE_1, "0.0.1-SNAPSHOT", UUID.randomUUID().toString());
        MockHttpServletResponse response = lockControllerClient.createLock(lock);
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        assertThat(createdLock.getId()).isNotNull();
        assertThat(createdLock.getDataSourceExecution().getRecords()).hasSize(1);
    }

    @DirtiesContext
    @Test
    public void shouldAllowSharedLocksByAnyTestware() throws Exception {
        String dataSourceId = createDataSource("shouldAllowSharedLocksByAnyTestware").getId();
        String jobId = UUID.randomUUID().toString();
        Lock lock1 = createLockObject(dataSourceId, SHARED, TESTWARE_1, "0.0.1-SNAPSHOT", jobId);
        Lock lock2 = createLockObject(dataSourceId, SHARED, TESTWARE_2, "0.0.1-SNAPSHOT", jobId);

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @DirtiesContext
    @Test
    public void shouldExpireLockRecordsAfterTimeout() throws Exception {
        String dataSourceId = createDataSource("shouldExpireLockRecordsAfterTimeout").getId();
        String jobId = UUID.randomUUID().toString();
        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, "0.0.1-SNAPSHOT", jobId);
        Lock lock2 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_2, "0.0.1-SNAPSHOT", jobId);

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        Thread.sleep(LOCK_TIMEOUT_SECONDS * 1000);
        lockDataService.expireLocks();

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void shouldReturnDataRecordsWhenLockCreated() throws Exception {
        String dataSourceId = createDataSource("shouldReturnDataRecordsWhenLockCreated").getId();
        Lock lock = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, "0.0.1-SNAPSHOT", UUID.randomUUID().toString());
        MockHttpServletResponse response = lockControllerClient.createLock(lock);
        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        List<DataRecord> records = createdLock.getDataSourceExecution().getRecords();
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getValues()).containsEntry("name", "Alderaan");
    }

    @Test
    public void shouldReturnDifferentLockRecords() throws Exception {
        String dataSourceId = createDataSource("shouldReturnDifferentLockRecords").getId();
        String jobId = UUID.randomUUID().toString();
        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);
        Lock lock2 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("climate=temperate"), jobId);
        Lock lock3 = createLockObject(dataSourceId, SHARED, TESTWARE_2, Lists.newArrayList("climate=temperate"), jobId);

        lock2.getDataSourceExecution().setLimit(3);

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lock01 = parseObject(firstLockResponse.getContentAsString(), Lock.class);

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lock02 = parseObject(secondLockResponse.getContentAsString(), Lock.class);

        MockHttpServletResponse thirdLockResponse = lockControllerClient.createLock(lock3);
        assertThat(thirdLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lock03 = parseObject(thirdLockResponse.getContentAsString(), Lock.class);

        assertThat(lock01.getDataSourceExecution().getRecords().size()).isEqualTo(1);
        assertThat(lock02.getDataSourceExecution().getRecords().size()).isEqualTo(3);
        assertThat(lock03.getDataSourceExecution().getRecords().size()).isEqualTo(2);
    }

    @Test
    public void shouldRunOutOfRecords() throws Exception {
        String dataSourceId = createDataSource("shouldRunOutOfRecords").getId();
        String jobId = UUID.randomUUID().toString();
        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);
        Lock lock2 = createLockObject(dataSourceId, SHARED, TESTWARE_1, Lists.newArrayList("climate=temperate"), jobId);
        Lock lock3 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_2, Lists.newArrayList("climate=temperate"), jobId);

        lock2.getDataSourceExecution().setLimit(4);
        lock3.getDataSourceExecution().setLimit(3);

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        MockHttpServletResponse secondLockResponse3 = lockControllerClient.createLock(lock3);
        assertThat(secondLockResponse3.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        String badLock = secondLockResponse3.getContentAsString();

        assertThat(badLock).contains("Could not obtain enough records");
    }

    @Test
    public void shouldGetLock() throws Exception {
        String dataSourceId = createDataSource("shouldGetLock").getId();
        String jobId = UUID.randomUUID().toString();

        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);
        Lock lock2 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);

        lock1.setJobId("test_run");
        lock2.setJobId("test_run2");

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lockResponse1 = parseObject(firstLockResponse.getContentAsString(), Lock.class);

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lockResponse2 = parseObject(secondLockResponse.getContentAsString(), Lock.class);

        assertThat(lockResponse1.getDataSourceExecution().getRecords().size())
                .isEqualTo(lockResponse2.getDataSourceExecution().getRecords().size());
    }

    @Test
    public void shouldNotGetLock() throws Exception {
        String dataSourceId = createDataSource("shouldNotGetLock").getId();
        String jobId = UUID.randomUUID().toString();

        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);
        Lock lock2 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Kamino"), jobId);

        lock1.setJobId("test_run");
        lock2.setJobId("test_run");

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        String badLock = secondLockResponse.getContentAsString();

        assertThat(badLock).contains("Could not obtain enough records");
    }

    @Test
    public void shouldFindNoRecords() throws Exception {
        String dataSourceId = createDataSource("shouldFindNoRecords").getId();
        String jobId = UUID.randomUUID().toString();

        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("name=Jupitar"), jobId);

        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        String badLock = firstLockResponse.getContentAsString();

        assertThat(badLock).contains("Could not obtain enough records");
    }

    @Test
    public void shouldReturnDataRecordsForLock() throws Exception {
        String dataSourceId = createDataSource("shouldReturnDataRecordsForLock").getId();
        String jobId = UUID.randomUUID().toString();

        Lock lock = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, "0.0.1-SNAPSHOT", jobId);
        MockHttpServletResponse response = lockControllerClient.createLock(lock);
        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        response = lockControllerClient.getDataRecordsForLock(createdLock.getId());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        List<DataRecord> records = parseList(response.getContentAsString(), DataRecord.class);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getValues()).containsEntry("name", "Alderaan");
    }

    @Test
    public void shouldReleaseLock() throws Exception {
        String dataSourceId = createDataSource("shouldReleaseLock").getId();
        String jobId = UUID.randomUUID().toString();

        Lock lock = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, "0.0.1-SNAPSHOT", jobId);
        MockHttpServletResponse response = lockControllerClient.createLock(lock);
        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        response = lockControllerClient.releaseLock(createdLock.getId());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());

        response = lockControllerClient.releaseLock(createdLock.getId());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    public void shouldReturnCorrectNumberOfDataRecordsForVersion() throws Exception{
        dataSourceControllerClient.login(taf);

        final String dataSourceId = createDataSource("dataSourceWithVersionHistory").getId();
        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf2);

        approve(dataSourceId);

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf);

        deleteARecord(dataSourceId, "0.0.2-SNAPSHOT");

        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf2);

        approve(dataSourceId);

        String jobId = UUID.randomUUID().toString();

        final Lock lockVersionTwo = createLockObject(dataSourceId, SHARED, TESTWARE_1, "0.0.2", jobId);
        lockVersionTwo.getDataSourceExecution().setPredicates(new ArrayList<>());
        MockHttpServletResponse response = lockControllerClient.createLock(lockVersionTwo);

        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        List<DataRecord> retrievedRecords = createdLock.getDataSourceExecution().getRecords();
        assertThat(retrievedRecords).hasSize(9);
        lockControllerClient.releaseLock(lockVersionTwo.getId());

        final Lock lockVersionOne = createLockObject(dataSourceId, SHARED, TESTWARE_1, "0.0.1", jobId);
        lockVersionOne.getDataSourceExecution().setPredicates(new ArrayList<>());
        response = lockControllerClient.createLock(lockVersionOne);

        createdLock = parseObject(response.getContentAsString(), Lock.class);

        retrievedRecords = createdLock.getDataSourceExecution().getRecords();
        assertThat(retrievedRecords).hasSize(10);
    }

    @Test
    public void shouldReturnCorrectVersionOfDataRecords() throws Exception {
        dataSourceControllerClient.login(taf);

        String dataSourceId = createDataSource("dataSourceWithVersionHistory").getId();
        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf2);

        approve(dataSourceId);

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf);

        Records records = dataSourceControllerClient.getRecords(dataSourceId, "0.0.2-SNAPSHOT");
        Optional<DataRecord> alderaan = records.getData().stream()
                                               .filter(record -> record.getValues().get("name").equals("Alderaan"))
                                               .findFirst();
        DataSourceAction action = aDataSourceAction()
                .withId(alderaan.get().getId())
                .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                .withKey("orbital_period")
                .withNewValue("5")
                .withVersion("0.0.2-SNAPSHOT")
                .withLocalTimestamp(1460009673453L)
                .build();
        dataSourceControllerClient.edit(dataSourceId, action, newVersionAction(dataSourceId, "0.0.2-SNAPSHOT"));

        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf2);

        approve(dataSourceId);

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(taf);

        action = aDataSourceAction()
                .withId(alderaan.get().getId())
                .withType(DataSourceActionType.RECORD_VALUE_EDIT.name())
                .withKey("orbital_period")
                .withNewValue("953")
                .withVersion("0.0.3-SNAPSHOT")
                .withLocalTimestamp(1460009673453L)
                .build();
        dataSourceControllerClient.edit(dataSourceId, action, newVersionAction(dataSourceId, "0.0.3-SNAPSHOT"));

        String jobId = UUID.randomUUID().toString();

        Lock lock = createLockObject(dataSourceId, SHARED, TESTWARE_1, "0.0.2", jobId);
        MockHttpServletResponse response = lockControllerClient.createLock(lock);

        Lock createdLock = parseObject(response.getContentAsString(), Lock.class);

        List<DataRecord> retrievedRecords = createdLock.getDataSourceExecution().getRecords();
        assertThat(retrievedRecords).hasSize(1);
        assertThat(retrievedRecords.get(0).getValues()).containsEntry("orbital_period", "5");
    }

    @Test
    public void shouldReturnIdenticalLockRecordsDifferentJobId() throws Exception {
        String dataSourceId = createDataSource("shouldReturnIdenticalLockRecordsDifferentJobId").getId();
        String jobId = UUID.randomUUID().toString();
        String jobId1 = UUID.randomUUID().toString();
        Lock lock1 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("climate=temperate"), jobId);
        Lock lock2 = createLockObject(dataSourceId, EXCLUSIVE, TESTWARE_1, Lists.newArrayList("climate=temperate"), jobId1);


        MockHttpServletResponse firstLockResponse = lockControllerClient.createLock(lock1);
        assertThat(firstLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lock01 = parseObject(firstLockResponse.getContentAsString(), Lock.class);

        MockHttpServletResponse secondLockResponse = lockControllerClient.createLock(lock2);
        assertThat(secondLockResponse.getStatus()).isEqualTo(HttpStatus.OK.value());
        Lock lock02 = parseObject(secondLockResponse.getContentAsString(), Lock.class);


        assertThat(lock01.getDataSourceExecution().getRecords().size()).isEqualTo(6);
        assertThat(lock02.getDataSourceExecution().getRecords().size()).isEqualTo(6);
    }


    private User taf2() {
        return anUser()
                .withId(1134L)
                .withUsername(TAF_2)
                .build();
    }

    private Execution createNewExecution() throws Exception {
        Execution execution = new Execution();
        execution.getProperties().put("team", "New");
        MockHttpServletResponse executionResponse = executionControllerClient.createExecution(execution);
        return parseObject(executionResponse.getContentAsString(), Execution.class);
    }

    private Lock createLockObject(String dataSourceId, LockType lockType, String testWare,
            final String version, final String jobId) throws Exception {
        Lock lock = new Lock();
        lock.setDataSourceExecution(new DataSourceExecution());
        lock.getDataSourceExecution().setExecutionId(createNewExecution().getId());
        lock.getDataSourceExecution().setDataSourceId(dataSourceId);
        lock.getDataSourceExecution().setVersion(version);
        lock.getDataSourceExecution().setTestwarePackage(testWare);

        lock.getDataSourceExecution().setPredicates(Lists.newArrayList("name=Alderaan"));
        lock.setTimeoutSeconds(LOCK_TIMEOUT_SECONDS);
        lock.setType(lockType);
        lock.setJobId(jobId);
        return lock;
    }

    private Lock createLockObject(String dataSourceId, LockType lockType, String testWare, List<String> predicates,
            String jobId)
    throws Exception {
        Lock lockObject = createLockObject(dataSourceId, lockType, testWare, "0.0.1-SNAPSHOT", jobId);
        lockObject.getDataSourceExecution().setPredicates(predicates);
        return lockObject;
    }

    private DataSourceIdentity createDataSource(String name) throws Exception {
        DataSource dataSource =
                parseObjectFile("datasources/planets.json", DataSource.class);

        dataSource.getIdentity().setName(name);
        return dataSourceControllerClient.createFromObject(dataSource);

    }
}
