package com.ericsson.cifwk.tdm.adapter;

import com.ericsson.cifwk.taf.datasource.UnknownDataSourceTypeException;
import com.ericsson.cifwk.tdm.adapter.TdmDataSourceFactory.DataSourceProperties;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.Context;
import com.ericsson.cifwk.tdm.api.model.ContextBuilder;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentityBuilder;
import com.ericsson.cifwk.tdm.client.TDMClient;
import com.ericsson.cifwk.tdm.client.services.ContextService;
import com.ericsson.cifwk.tdm.client.services.DataSourceService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.stubbing.Stubber;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_APPROVED;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_CONTEXT;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_ID;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_LABEL;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_NAME;
import static com.ericsson.cifwk.tdm.adapter.TdmDataSourceConfiguration.TDM_DATASOURCE_VERSION;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.ContextBuilder.aContextBuilder;
import static com.ericsson.cifwk.tdm.api.model.DataSourceIdentityBuilder.aDataSourceIdentity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Lists.newArrayListWithCapacity;
import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.CoreMatchers.isA;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class TdmDataSourceFactoryTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private TDMClient client;
    private DataSourceService dataSourceService;
    private ContextService contextService;
    private Context context;

    @Before
    public void setUp() throws Exception {
        client = mock(TDMClient.class);
        dataSourceService = mock(DataSourceService.class);
        contextService = mock(ContextService.class);

        String contextName = "context";
        context = context(contextName).build();
        doReturnSingleContext(context)
                .when(contextService).getContextByName(contextName);
    }

    @Test
    public void create_shouldThrowIllegalArgumentException_when_dataSourceId_and_dataSourceContext_and_dataSourceLabel_null() throws Exception {
        expectIllegalArgumentExceptionOnMissingConfigurationProperties(TDM_DATASOURCE_ID, TDM_DATASOURCE_CONTEXT, TDM_DATASOURCE_LABEL);
    }

    @Test
    public void create_shouldThrowIllegalArgumentException_when_dataSourceId_and_dataSourceName_and_dataSourceLabel_null() throws Exception {
        expectIllegalArgumentExceptionOnMissingConfigurationProperties(TDM_DATASOURCE_ID, TDM_DATASOURCE_NAME, TDM_DATASOURCE_LABEL);
    }

    private void expectIllegalArgumentExceptionOnMissingConfigurationProperties(String... missingProperties) {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage(TdmDataSourceFactory.ERROR_PROPERTIES);

        TdmDataSourceConfiguration configurationSource = fullConfiguration();
        for (String missingProperty : missingProperties) {
            doReturn(null).when(configurationSource).getProperty(missingProperty);
        }

        TdmDataSourceFactory tdmDataSourceFactory = new TdmDataSourceFactory(configurationSource, null);
        try {
            tdmDataSourceFactory.createDataSource("TDM", configurationSource);
        } catch (UnknownDataSourceTypeException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void create_shouldThrowUnknownHostException_whenTdmHost_doesNotExist() throws Exception {
        thrown.expectCause(isA(UnknownHostException.class));

        TdmDataSourceConfiguration configurationSource = fullConfiguration();
        TdmDataSourceFactory tdmDataSourceFactory = new TdmDataSourceFactory(configurationSource, null);
        tdmDataSourceFactory.createDataSource("TDM", configurationSource);
    }

    private TdmDataSourceConfiguration fullConfiguration() {
        return configuration(
                "http://dummy-host-that-definitely-does-not-exist.dummy",
                "56c5ddd29759e577fc68ab74",
                null,
                "ENM",
                "Star wars planets",
                "true",
                null
        );
    }

    @Test
    public void getByContextAndName_notFound_byContextAndName() throws Exception {
        DataSourceProperties properties = properties("context", "name", "true");
        thrown.expect(NullPointerException.class);
        thrown.expectMessage(String.format(TdmDataSourceFactory.ERROR_NOT_FOUND, properties));


        doReturnSingle(null)
                .when(dataSourceService).getLatestIdentityByContextAndName(context.getId(), "name", true);

        TdmDataSourceFactory.getByContextAndName(properties, contextService, dataSourceService);
    }

    @Test
    public void getByContextAndName_byContextAndName_unapproved_success() throws Exception {
        String dataSourceContext = "context";
        String dataSourceName = "name";
        DataSourceProperties properties = properties(dataSourceContext, dataSourceName, "false");
        doReturnSingle(
                dataSource(dataSourceContext, dataSourceName, "1.0", UNAPPROVED).build()
        ).when(dataSourceService).getLatestIdentityByContextAndName(context.getId(), dataSourceName, false);

        DataSourceIdentity result = TdmDataSourceFactory.getByContextAndName(properties, contextService, dataSourceService);

        assertThat(result.getContext()).isEqualTo(dataSourceContext);
        assertThat(result.getName()).isEqualTo(dataSourceName);
        assertThat(result.getVersion()).isEqualTo("1.0");
        verify(dataSourceService, only()).getLatestIdentityByContextAndName(context.getId(), dataSourceName,false);
    }

    @Test
    public void getByContextAndName_byContextAndName_unapproved_error() throws Exception {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage(TdmDataSourceFactory.ERROR_NOT_APPROVED);
        String dataSourceContext = "context";
        String dataSourceName = "name";
        DataSourceProperties properties = properties(dataSourceContext, dataSourceName, "true");
        doReturnSingle(
                dataSource(dataSourceContext, dataSourceName, "1.0", UNAPPROVED).build()
        ).when(dataSourceService).getLatestIdentityByContextAndName(context.getId(), dataSourceName, true);

        TdmDataSourceFactory.getByContextAndName(properties, contextService, dataSourceService);
    }

    @Test
    public void getByContextAndName_notFound_byIdAndVersion() throws Exception {
        String dataSourceContext = "context";
        String dataSourceName = "name";
        DataSourceProperties properties = properties("1.0", dataSourceContext, dataSourceName, "true");

        thrown.expect(NullPointerException.class);
        thrown.expectMessage(String.format(TdmDataSourceFactory.ERROR_NOT_FOUND, properties));


        doReturnSingle(dataSource(dataSourceContext, dataSourceName, "1.0", APPROVED).build())
                .when(dataSourceService).getLatestIdentityByContextAndName(context.getId(), dataSourceName, true);
        doReturnSingle(null)
                .when(dataSourceService).getDataSourceByIdAndVersion(anyString(), anyString());

        TdmDataSourceFactory.getByContextAndName(properties, contextService, dataSourceService);
    }

    @Test
    public void getByContextAndName_happyPath_byContextAndName() throws Exception {
        String dataSourceContext = "context";
        String dataSourceName = "name";
        DataSourceProperties properties = properties(dataSourceContext, dataSourceName, "true");

        doReturnSingle(
                dataSource(dataSourceContext, dataSourceName, "2.0", APPROVED).build()
        ).when(dataSourceService).getLatestIdentityByContextAndName(context.getId(), dataSourceName, true);

        DataSourceIdentity result = TdmDataSourceFactory.getByContextAndName(
                properties, contextService, dataSourceService);

        assertThat(result.getContext()).isEqualTo(dataSourceContext);
        assertThat(result.getName()).isEqualTo(dataSourceName);
        assertThat(result.getVersion()).isEqualTo("2.0");
        verify(dataSourceService, only()).getLatestIdentityByContextAndName(context.getId(), dataSourceName,true);
    }

    @Test
    public void getByContextAndName_happyPath_byIdAndVersion() throws Exception {
        String id = "id";
        String version = "1.0";
        String dataSourceContext = "context";
        String dataSourceName = "name";
        DataSourceProperties properties = properties(version, dataSourceContext, dataSourceName, "true");

        DataSourceIdentityBuilder dataSource = dataSource(
                dataSourceContext, dataSourceName, version, APPROVED).withId(id);
        doReturnSingle(dataSource.build()).when(dataSourceService)
                .getLatestIdentityByContextAndName(context.getId(), dataSourceName, true);
        doReturnSingle(dataSource.build()).when(dataSourceService).getDataSourceByIdAndVersion(id, version);

        DataSourceIdentity result = TdmDataSourceFactory.getByContextAndName(
                properties, contextService, dataSourceService);

        assertThat(result.getContext()).isEqualTo(dataSourceContext);
        assertThat(result.getName()).isEqualTo(dataSourceName);
        verify(dataSourceService).getLatestIdentityByContextAndName(context.getId(), dataSourceName, true);
        verify(dataSourceService).getDataSourceByIdAndVersion(id, version);
        verifyNoMoreInteractions(dataSourceService);
    }

    @Test
    public void getByLabel_happyPath() throws Exception {
        String label = "newLabel";
        String id = "id";
        String version = "1.0";
        String dataSourceContext = "context";
        String dataSourceName = "name";

        DataSourceProperties properties = properties(label, dataSourceContext);

        DataSourceIdentityBuilder dataSource = dataSource(
                dataSourceContext, dataSourceName, version, APPROVED).withId(id);
        doReturnSingle(dataSource.build()).when(dataSourceService)
                .getDataSourceByLabel(label, context.getId());

        DataSourceIdentity result = TdmDataSourceFactory.getByLabelAndContext(properties, dataSourceService,
                contextService);

        assertThat(result.getContext()).isEqualTo(dataSourceContext);
        assertThat(result.getName()).isEqualTo(dataSourceName);
        assertThat(result.getVersion()).isEqualTo(version);
        verify(dataSourceService).getDataSourceByLabel(label, context.getId());
        verifyNoMoreInteractions(dataSourceService);
    }

    private DataSourceProperties properties(String context, String name, String approved) {
        return new DataSourceProperties(configuration(null, null, null, context, name, approved, null));
    }

    private DataSourceProperties properties(String version, String context, String name, String approved) {
        return new DataSourceProperties(configuration(null, null, version, context, name, approved, null));
    }

    private DataSourceProperties properties(String label, String context) {
        return new DataSourceProperties(configuration(null, null, null, context, null, null, label));
    }

    private TdmDataSourceConfiguration configuration(String host, String id, String version,
                                                     String context, String name, String approved, String label) {
        TdmDataSourceConfiguration configuration = mock(TdmDataSourceConfiguration.class);
        doReturn(host).when(configuration).getTdmHost();
        doReturn(id).when(configuration).getProperty(TDM_DATASOURCE_ID);
        doReturn(version).when(configuration).getProperty(TDM_DATASOURCE_VERSION);
        doReturn(context).when(configuration).getProperty(TDM_DATASOURCE_CONTEXT);
        doReturn(name).when(configuration).getProperty(TDM_DATASOURCE_NAME);
        doReturn(approved).when(configuration).getProperty(TDM_APPROVED, "true");
        doReturn(label).when(configuration).getProperty(TDM_DATASOURCE_LABEL);
        return configuration;
    }

    private Stubber doReturnListOf(DataSourceIdentityBuilder... builders) throws IOException {
        return doReturn(stub(dataSources(builders)));
    }

    private Stubber doReturnEmptyList() throws IOException {
        return doReturn(stub(newArrayList()));
    }

    private Stubber doReturnSingle(DataSourceIdentity dataSourceIdentity) throws IOException {
        return doReturn(stub(dataSourceIdentity));
    }

    private Stubber doReturnSingleContext(Context context) throws IOException {
        return doReturn(stub(context));
    }

    @SuppressWarnings("unchecked")
    private Call<?> stub(Object object) throws IOException {
        Call<?> call = mock(Call.class);
        doReturn(Response.success(object)).when(call).execute();
        return call;
    }

    private List<DataSourceIdentity> dataSources(DataSourceIdentityBuilder... builders) {
        List<DataSourceIdentity> dataSources = newArrayListWithCapacity(builders.length);
        for (DataSourceIdentityBuilder builder : builders) {
            dataSources.add(builder.build());
        }
        return dataSources;
    }

    private DataSourceIdentityBuilder dataSource(String context, String name,
                                                 String version, ApprovalStatus approvalStatus) {
        return aDataSourceIdentity()
                .withApprovalStatus(approvalStatus)
                .withContext(context)
                .withName(name)
                .withVersion(version);
    }

    private ContextBuilder context(String context) {
        return aContextBuilder()
                .withName(context)
                .withId(Integer.toString(new Random().nextInt()));
    }
}
