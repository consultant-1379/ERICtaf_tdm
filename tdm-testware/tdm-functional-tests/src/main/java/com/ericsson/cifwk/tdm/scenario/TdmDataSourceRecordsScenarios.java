package com.ericsson.cifwk.tdm.scenario;

import static org.assertj.core.api.Assertions.assertThat;

import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.runner;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.api.TafRxScenarios.annotatedMethod;
import static com.ericsson.de.scenariorx.api.TafRxScenarios.dataSource;

import java.util.concurrent.atomic.AtomicInteger;

import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxScenario;

public final class TdmDataSourceRecordsScenarios extends TafTestBase {

    private final AtomicInteger executionCount = new AtomicInteger();

    @Test
    public void verifyRecordsForSpecificVersion(){
        executionCount.set(0);
        runner().withDebugLogEnabled().build().run(testScenarioRecordsSpecificToVersion());
        assertThat(executionCount.intValue())
                .as("Check that the correct number of records has been returned")
                .isEqualTo(5);

    }

    private RxScenario testScenarioRecordsSpecificToVersion() {
        return scenario("Acceptance Test to verify records returned for specific approved version")
                .addFlow(flowForRecords("specificVersionFromTdm", "specificVersionExpectedData"))
                .build();
    }

    private RxFlow flowForRecords(final String dataProviderFromTDM, final String dataProviderFromCsv) {
        return flow("Verify data returned from TDM")
                .addTestStep(annotatedMethod(this, "verifyRecordData"))
                .withDataSources(dataSource(dataProviderFromTDM, DataRecord.class),
                        dataSource(dataProviderFromCsv, DataRecord.class))
                .build();
    }

    @TestStep(id = "verifyRecordData")
    public void verifyRecordData(@Input("sport") final String sport, @Input("age") final Integer age, @Output
            ("expectedSport") final String expectedSport, @Output("expectedAge") final Integer expectedAge) {
        assertThat(sport).as("Check that the correct data has been returned").isEqualTo(expectedSport);
        assertThat(age).as("Check that the correct data has been returned").isEqualTo(expectedAge);
        executionCount.incrementAndGet();
    }

    @Test
    public void verifyRecordsForLatestApproved(){
        executionCount.set(0);
        runner().withDebugLogEnabled().build().run(testScenarioRecordsForLatestApproved());
        assertThat(executionCount.intValue())
                .as("Check that the correct number of records has been returned")
                .isEqualTo(3);
    }

    private RxScenario testScenarioRecordsForLatestApproved() {
        return scenario("Acceptance Test to verify records returned for latest approved version")
                .addFlow(flowForRecords("latestFromTdm", "latestExpectedData"))
                .build();
    }
}
