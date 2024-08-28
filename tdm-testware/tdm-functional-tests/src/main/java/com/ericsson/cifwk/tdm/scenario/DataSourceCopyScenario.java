package com.ericsson.cifwk.tdm.scenario;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;
import com.ericsson.cifwk.tdm.HostResolver;
import com.ericsson.cifwk.tdm.ScreenShotExceptionHandler;
import com.ericsson.cifwk.tdm.flows.DataSourceFlows;
import com.ericsson.cifwk.tdm.flows.LoginFlows;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.google.common.collect.Lists.newArrayList;

/**
 * Created by ekonsla on 09/05/2016.
 */
public class DataSourceCopyScenario extends TafTestBase {

    @Inject
    private TDMOperator operator;

    @Inject
    private LoginFlows loginFlows;

    @Inject
    private DataSourceFlows dataSourceFlows;

    @BeforeMethod
    public void setUp() {
        operator.init(HostResolver.resolve());
    }

    @Test
    @TestId(id = "TAF_TDM_003")
    public void copyDataSourceScenario() {
        TestDataSource<DataRecord> dataSource = fromClass(TDMDataSource.class);
        TafTestContext.getContext().addDataSource(DataSourceFlows.FLOW_DATASOURCE, dataSource);

        TestScenario scenario = scenario("View Data Source Scenario")
                .addFlow(loginFlows.loginFlow())
                .addFlow(dataSourceFlows.verifyDataSourceCanBeCopied())
                .addFlow(loginFlows.logoutFlow())
                .build();

        ScreenShotExceptionHandler screenShotExceptionHandler = new ScreenShotExceptionHandler(operator.getBrowser());
        runner().withListener(new LoggingScenarioListener()).withDefaultExceptionHandler(screenShotExceptionHandler)
                .build().start(scenario);
    }

    public static class TDMDataSource {
        public static final String APPROVED_VERSION = "0.0.2";

        @DataSource
        public List<Map<String, Object>> records() throws IOException {
            return ImmutableList.of(ImmutableMap.<String, Object>builder()
                    .put("dataSourceId", "56c5ddd29759e577fc68ab74")
                    .put("dataSourceName", "Star wars planets")
                    .put("dataSourceGroup", "com.ericsson.firstds")
                    .put("dataSourceVersions",
                            newArrayList("0.0.1-SNAPSHOT", APPROVED_VERSION, "0.0.3", "0.0.4-SNAPSHOT"))
                    .put("versionToCopy", APPROVED_VERSION)
                    .put("dataSourceContext", "System/BUCI/DUAC/NAM/ENM")
                    .put("newDataSourceName", "Star wars destroyed planets" + System.currentTimeMillis())
                    .put("newDataSourceGroup", "this.is.new.group")
                    .put("newDataSourceContext", "System/BUCI/DUAC/NAM/Assure/ASSURE")
                    .put("version", APPROVED_VERSION)
                    .put("numberOfDataRecords", 10)
                    .put("expectedCopyToastMessage", "Data source successfully copied")
                    .build());
        }
    }
}
