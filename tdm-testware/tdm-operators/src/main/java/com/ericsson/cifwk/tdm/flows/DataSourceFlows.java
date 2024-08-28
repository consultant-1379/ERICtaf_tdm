package com.ericsson.cifwk.tdm.flows;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.tdm.steps.ContextTestSteps;
import com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps;
import com.ericsson.cifwk.tdm.steps.DataSourceListTestSteps;
import com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps;
import com.google.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.tdm.steps.ContextTestSteps.Parameters.CONTEXT;
import static com.ericsson.cifwk.tdm.steps.ContextTestSteps.StepIds.NAVIGATE_TO_CONTEXT;
import static com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps.CREATE_DATA_SOURCE_WITH_CSV_DATA;
import static com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps.OPEN_CREATE_DATASOURCE_VIEW;
import static com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps.SAVE_DATA_SOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps.VERIFY_NO_ERRORS_DISPLAYED;
import static com.ericsson.cifwk.tdm.steps.DataSourceCreateTestSteps.VERIFY_TABLE_IS_POPULATED;
import static com.ericsson.cifwk.tdm.steps.DataSourceListTestSteps.StepIds.DELETE_DATA_SOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceListTestSteps.StepIds.VERIFY_DATASOURCE_IS_IN_LIST;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.NEW_DATA_SOURCE_NAME;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.COPY_DATASOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.GO_TO_LIST_PAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.OPEN_DATASOURCE_BY_NAME;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.VERIFY_DATASOURCE_ATTRIBUTES;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.VERIFY_DATASOURCE_COPIED_ATTRIBUTES;

/**
 * Created by ekonsla on 06/05/2016.
 */
public class DataSourceFlows {

    public static final String FLOW_DATASOURCE = "flowDataSource";
    public static final String SYSTEM_CONTEXT_ID = "systemId-1";
    public static final String SAVE_DATSOURCE_VERIFY_FLOW = "Save Data Source and Verify Save Flow";

    @Inject
    private DataSourceViewTestSteps dataSourceViewTestSteps;

    @Inject
    private DataSourceListTestSteps dataSourceListTestSteps;

    @Inject
    private DataSourceCreateTestSteps dataSourceCreateTestSteps;

    @Inject
    private ContextTestSteps contextsTestSteps;

    public TestStepFlow verifyDataSourceCanBeCopied() {
        return flow("Verify Data Source Can Be Copied Flow")
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, OPEN_DATASOURCE_BY_NAME))
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, VERIFY_DATASOURCE_ATTRIBUTES))
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, COPY_DATASOURCE))
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, VERIFY_DATASOURCE_COPIED_ATTRIBUTES))
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, GO_TO_LIST_PAGE)).alwaysRun()
                .addTestStep(annotatedMethod(dataSourceListTestSteps, DELETE_DATA_SOURCE)).alwaysRun()
                .withDataSources(dataSource(FLOW_DATASOURCE))
                .build();
    }

    public TestStepFlow verifyDataSourcesList() {
        return flow("Verify Data Sources List Flow")
                .addTestStep(annotatedMethod(contextsTestSteps, NAVIGATE_TO_CONTEXT)
                        .withParameter(CONTEXT, SYSTEM_CONTEXT_ID))
                .addSubFlow(flow("Verify Data Source Row")
                        .addTestStep(annotatedMethod(dataSourceListTestSteps, VERIFY_DATASOURCE_IS_IN_LIST))
                        .withDataSources(dataSource("datasources")))
                .build();
    }

    public TestStepFlow navigateToContext() {
        return flow("Verify Data Sources List Flow")
                .addTestStep(annotatedMethod(contextsTestSteps, NAVIGATE_TO_CONTEXT))
                .build();
    }

    public TestStepFlow createDataSourceFromCsvFile() {
        return flow("Create Datasource from Csv File Flow")
                .addTestStep(annotatedMethod(contextsTestSteps, NAVIGATE_TO_CONTEXT)
                        .withParameter(CONTEXT, SYSTEM_CONTEXT_ID))
                .addTestStep(annotatedMethod(dataSourceCreateTestSteps, OPEN_CREATE_DATASOURCE_VIEW))
                .addTestStep(annotatedMethod(dataSourceCreateTestSteps, CREATE_DATA_SOURCE_WITH_CSV_DATA))
                .withDataSources(dataSource("dataSourceToCreate"))
                .build();
    }

    public TestStepFlow verifyCsvDataAddedToTable() {
        return flow("Verify Csv data is added to table Flow")
                .addTestStep(annotatedMethod(dataSourceCreateTestSteps, VERIFY_NO_ERRORS_DISPLAYED))
                .addTestStep(annotatedMethod(dataSourceCreateTestSteps, VERIFY_TABLE_IS_POPULATED))
                .withDataSources(dataSource("dataSourceToCreate"))
                .build();
    }

    public TestStepFlow saveDataSource() {
        return flow(SAVE_DATSOURCE_VERIFY_FLOW)
                .addTestStep(annotatedMethod(dataSourceCreateTestSteps, SAVE_DATA_SOURCE))
                .build();
    }

    public TestStepFlow verifyDataSourceSaved() {
        return flow(SAVE_DATSOURCE_VERIFY_FLOW)
                .addTestStep(annotatedMethod(dataSourceViewTestSteps, GO_TO_LIST_PAGE))
                .addSubFlow(flow("Verify Data Source Row")
                        .addTestStep(annotatedMethod(dataSourceListTestSteps, VERIFY_DATASOURCE_IS_IN_LIST))
                        .withDataSources(dataSource("dataSourceToCreate")))
                .build();
    }

    public TestStepFlow deleteDataSource() {
        return flow(SAVE_DATSOURCE_VERIFY_FLOW)
                .addTestStep(annotatedMethod(dataSourceListTestSteps, DELETE_DATA_SOURCE))
                .withDataSources(dataSource("dataSourceToCreate").bindColumn("datasource", NEW_DATA_SOURCE_NAME))
                .build();
    }
}
