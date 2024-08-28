package com.ericsson.cifwk.tdm.scenario;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;
import static com.google.common.truth.Truth.assertThat;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.beust.jcommander.internal.Maps;
import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TafDataSources;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarioRunner;
import com.ericsson.cifwk.taf.scenario.impl.LoggingScenarioListener;

/**
 * Created by ekonsla on 13/05/2016.
 */
public class TdmDataSourceScenario extends TafTestBase {

    private static final String TEST_STEP = "testStep";
    private final TestScenarioRunner runner = runner()
            .withListener(new LoggingScenarioListener())
            .build();

    private Map<String, String> namesDiameters;
    private AtomicInteger recordCount;

    @BeforeMethod
    public void setUp() {
        recordCount = new AtomicInteger(0);
        namesDiameters = Maps.newHashMap();
        namesDiameters.put("Alderaan", "12500");
        namesDiameters.put("Geonosis", "11370");
        namesDiameters.put("Kamino", "19720");
        namesDiameters.put("Bespin", "118000");
        namesDiameters.put("Hoth", "7200");
        namesDiameters.put("Yavin IV", "10200");
        namesDiameters.put("Dagobah", "8900");
        namesDiameters.put("Coruscant", "12240");
        namesDiameters.put("Naboo", "12120");
        namesDiameters.put("Endor", "4900");
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByNameInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by name")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-named"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByIdInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by id")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByIdAndVersionInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by id and version")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id-version"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_008")
    public void verifyTdmDataSourceRetrievableByStaticbyId() {
        TestDataSource<DataRecord> fromDataProvider = TafDataSources.fromTafDataProvider("tdm-id");

        assertThat(fromDataProvider).isNotNull();
        assertThat(fromDataProvider).hasSize(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_008")
    public void verifyTdmDataSourceRetrievableByStaticbyLabel() {
        TestDataSource<DataRecord> fromDataProvider = TafDataSources.fromTafDataProvider("tdm-label");

        assertThat(fromDataProvider).isNotNull();
        assertThat(fromDataProvider).hasSize(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByLabelInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by label")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-label"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByIdAndNotApprovedInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by Id when unapproved")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id-notApproved"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(namesDiameters.size());
    }

    @Test
    @TestId(id = "TAF_TDM_0012")
    public void verifyTdmDataSourceRetrievedByIdAndVersionShouldGetSnapshotInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by id and version and unapproved")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id-version-snapshot"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(5);
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByIdAndVersionWithFilterInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by id, version with filter")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id-version-filter"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(1);
    }

    @Test
    @TestId(id = "TAF_TDM_006")
    public void verifyTdmDataSourceRetrievedByIdAndVersionWithFilterByColumnInScenario() {
        TestScenario scenario = scenario("Verify data source is correctly retrieved by id, version and only columns provided")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-id-version-columns"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(10);
    }

    @Test
    @TestId(id = "TAF_TDM_009")
    public void verifyTdmBothDataSourceRetrievedBothDataSources() {
        TestScenario scenario = scenario("Verify both data sources are received and have the correct lock type")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-options-test"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(7);
    }

    @Test
    @TestId(id = "TAF_TDM_010")
    public void verifyTdmBothDataSourceRetrievedBothDataSourcesWithNormalisedValues() {
        TestScenario scenario = scenario("Verify both data sources are received and have the correct lock type")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-options-test2"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(2);
    }

    @TestStep(id = TEST_STEP)
    public void step(@Input("name") String name,
                     @Input("diameter") String diameter) {
        recordCount.incrementAndGet();

        assertThat(namesDiameters).containsEntry(name, diameter);
    }

    @Test
    @TestId(id = "TAF_TDM_007")
    @DataDriven(name = "tdm-id")
    public void verifyTdmDataSourceByIdInDataDrivenTest(@Input("name") String name,
                                                        @Input("diameter") String diameter) {
        assertThat(namesDiameters).containsEntry(name, diameter);
    }

    @Test
    @TestId(id = "TAF_TDM_007")
    @DataDriven(name = "tdm-named")
    public void verifyTdmDataSourceByNameInDataDrivenTest(@Input("name") String name,
                                                          @Input("diameter") String diameter) {
        assertThat(namesDiameters).containsEntry(name, diameter);
    }

    @Test
    @TestId(id = "TAF_TDM_011")
    public void verifyTdmFiltersMultipleColumns() {
        TestScenario scenario = scenario("Verify that a user can filter more than one column in a single datasource")
                .addFlow(
                        flow("flow")
                                .addTestStep(annotatedMethod(this, TEST_STEP))
                                .withDataSources(dataSource("tdm-filter-multiple"))
                )
                .build();

        runner.start(scenario);

        assertThat(recordCount.intValue()).isEqualTo(3);
    }
}
