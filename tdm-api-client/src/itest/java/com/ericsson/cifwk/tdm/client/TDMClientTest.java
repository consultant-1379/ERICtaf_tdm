package com.ericsson.cifwk.tdm.client;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSource;
import com.ericsson.cifwk.tdm.api.model.DataSourceExecution;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Execution;
import com.ericsson.cifwk.tdm.api.model.Lock;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.client.services.DataSourceService;
import com.ericsson.cifwk.tdm.client.services.ExecutionService;
import com.ericsson.cifwk.tdm.client.services.LockService;
import com.ericsson.cifwk.tdm.client.services.LoginService;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 22/02/2016
 */
@Ignore
public class TDMClientTest {

    //    @Value("${local.server.port}")
    int port;

    private TDMClient tdmClient;

    @Before
    public void setUp() {
        tdmClient = new TDMClient(String.format("http://localhost:%d/api/", port));
    }

    @Test
    public void checkLoginService() throws IOException {
        UserCredentials userCredentials = new UserCredentials();
        userCredentials.setPassword("taf");
        userCredentials.setUsername("taf");

        LoginService loginService = tdmClient.getLoginService();

        Response<AuthenticationStatus> execute = loginService.login(userCredentials).execute();
        AuthenticationStatus body = execute.body();
        assertThat(body.getUserId()).isEqualTo("taf");

        Response<AuthenticationStatus> response = loginService.status().execute();
        boolean authenticated = response.body().isAuthenticated();
        assertThat(authenticated).isTrue();
    }

    @Test
    public void checkExecutionService() throws IOException {
        ExecutionService executionService = tdmClient.getExecutionService();

        Execution execution = new Execution();
        execution.setProperty("team", "TAF");
        Response<Execution> response = executionService.startExecution(execution).execute();

        assertThat(response.isSuccess()).isTrue();

        response = executionService.finishExecution(response.body().getId()).execute();
        assertThat(response.isSuccess()).isTrue();

        response = executionService.getExecutionById(response.body().getId()).execute();
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.body().getProperty("team")).isEqualTo("TAF");
    }

    @Test
    public void checkDataSourceService() throws IOException {
        DataSourceService dataSourceService = tdmClient.getDataSourceService();

        Response<List<DataSourceIdentity>> response = dataSourceService.getDataSourceIdentities().execute();
        assertThat(response.isSuccess()).isTrue();

        List<DataSourceIdentity> identities = response.body();

        DataSourceIdentity dataSource = null;
        for (DataSourceIdentity identity : identities) {
            if (identity.getName().equals("Star wars planets")) {
                dataSource = identity;
                break;
            }
        }
        assertThat(dataSource).isNotNull();

        Response<List<DataRecord>> recordResponse = dataSourceService.getRecords(dataSource.getId()).execute();

        assertThat(recordResponse.isSuccess()).isTrue();
        assertThat(recordResponse.body()).hasSize(10);
    }

    @Test
    public void checkInsertDataSource() throws IOException {
        DataSourceService dataSourceService = tdmClient.getDataSourceService();

        DataSource dataSource = createTestDataSource();

        Response<DataSourceIdentity> response = dataSourceService.createDataSource(dataSource).execute();
        DataSourceIdentity identity = response.body();

        Response<List<DataRecord>> dataRecordsResponse = dataSourceService.getRecords(identity.getId()).execute();
        assertThat(dataRecordsResponse.isSuccess()).isTrue();

        List<DataRecord> dataRecords = dataRecordsResponse.body();
        for (DataRecord dataRecord : dataRecords) {
            assertThat(dataRecord.getValues().get("mim_version")).isEqualTo("G1260-V2lim");
            assertThat(dataRecord.getValues().get("netsim_host")).isEqualTo("127.0.0.1");
            assertThat(dataRecord.getValues().get("network_element_ip")).isEqualTo("10.154.17.248");
            assertThat(dataRecord.getValues().get("network_element_name")).isEqualTo("LTE01ERBS00002");
            assertThat(dataRecord.getValues().get("simulation_name")).isEqualTo("LTEG1260-V2limx160-5K-FDD-LTE01");
        }
    }

    @Test
    public void checkLockService() throws IOException {
        LockService lockService = tdmClient.getLockService();

        Lock lock = new Lock();
        DataSourceExecution dataSourceExecution = new DataSourceExecution();
        lock.setDataSourceExecution(dataSourceExecution);
        dataSourceExecution.setDataSourceId("56c5ddd29759e577fc68ab74");
        dataSourceExecution.setExecutionId("56c5ddf29759e577fc68ab7f");
        dataSourceExecution.addPredicate("name=Alderaan");
        lock.setTimeout(10);

        Response<Lock> response = lockService.createDataRecordLock(lock).execute();
        assertThat(response.isSuccess()).isTrue();

        Response<List<DataRecord>> dataRecordsForLock = lockService.getDataRecordsForLock(response.body().getId()).execute();
        assertThat(dataRecordsForLock.isSuccess()).isTrue();

        assertThat(dataRecordsForLock.body().size()).isEqualTo(1);
    }

    private DataSource createTestDataSource() {
        Map<String, Object> values = new HashMap<>();
        values.put("mim_version", "G1260-V2lim");
        values.put("netsim_host", "127.0.0.1");
        values.put("network_element_ip", "10.154.17.248");
        values.put("network_element_name", "LTE01ERBS00002");
        values.put("simulation_name", "LTEG1260-V2limx160-5K-FDD-LTE01");

        DataRecord record = new DataRecord();
        record.setValues(values);

        List<DataRecord> records = new ArrayList<>();
        records.add(record);

        DataSourceIdentity dataSourceIdentity = new DataSourceIdentity();
        dataSourceIdentity.setName("netsim");

        DataSource dataSource = new DataSource();
        dataSource.setRecords(records);
        dataSource.setIdentity(dataSourceIdentity);
        return dataSource;
    }
}
