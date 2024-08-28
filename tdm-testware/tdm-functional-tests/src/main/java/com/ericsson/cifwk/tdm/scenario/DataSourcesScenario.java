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
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.taf.datasource.TafDataSources.fromClass;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

/**
 * Created by ekonsla on 09/05/2016.
 */
public class DataSourcesScenario extends TafTestBase {

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
    @TestId(id = "TAF_TDM_002")
    public void verifyDataSourcesPresentScenario() {
        TestDataSource<DataRecord> dataSource = fromClass(DatasourcesToCheckDatasource.class);
        TafTestContext.getContext().addDataSource("datasources", dataSource);

        TestScenario scenario = scenario("Verify DataSources list Scenario")
                .addFlow(loginFlows.loginFlow())
                .addFlow(dataSourceFlows.verifyDataSourcesList())
                .addFlow(loginFlows.logoutFlow())
                .build();

        ScreenShotExceptionHandler screenShotExceptionHandler = new ScreenShotExceptionHandler(operator.getBrowser());
        runner().withListener(new LoggingScenarioListener()).withDefaultExceptionHandler(screenShotExceptionHandler)
                .build().start(scenario);
    }

    @Test
    @TestId(id = "TAF_TDM_005")
    public void importCsvDataSourceScenario() {
        TestDataSource<DataRecord> dataSource = fromClass(DataSourcesToCreateDatasource.class);
        TafTestContext.getContext().addDataSource("dataSourceToCreate", dataSource);

        TestScenario scenario = scenario("Verify DataSources list Scenario")
                .addFlow(loginFlows.loginFlow())
                .addFlow(dataSourceFlows.navigateToContext())
                .addFlow(dataSourceFlows.createDataSourceFromCsvFile())
                .addFlow(dataSourceFlows.verifyCsvDataAddedToTable())
                .addFlow(dataSourceFlows.saveDataSource())
                .addFlow(dataSourceFlows.verifyDataSourceSaved())
                .addFlow(dataSourceFlows.deleteDataSource()).alwaysRun()
                .addFlow(loginFlows.logoutFlow())
                .build();

        ScreenShotExceptionHandler screenShotExceptionHandler = new ScreenShotExceptionHandler(operator.getBrowser());
        runner().withListener(new LoggingScenarioListener()).withDefaultExceptionHandler(screenShotExceptionHandler)
                .build().start(scenario);
    }

    public static class DatasourcesToCheckDatasource {
        @DataSource
        public List<Map<String, Object>> datasources() {
            return ImmutableList.of(
                    ImmutableMap.of("context", "ASSURE", "group", "com.ericsson", "datasource", "DS1"),
                    ImmutableMap.of("context", "ASSURE", "group", "com.ericsson.taf", "datasource", "DS2")
            );
        }
    }

    public static class DataSourcesToCreateDatasource{

        private static final long CURRENT_TIME = System.currentTimeMillis();

        @DataSource
        public List<Map<String, Object>> datasource(){
            return ImmutableList.of(ImmutableMap.<String, Object>builder()
                    .put("context", "System")
                    .put("datasource", "NewCSVDataSource" + CURRENT_TIME)
                    .put("group", "com.ericsson")
                    .put("csvFilename", "sampleData.csv")
                    .put("columnCount", 7)
                    .put("rowCount", 10)
                    .build());




        }
    }
}
