package com.ericsson.cifwk.tdm;


import static org.assertj.core.api.Assertions.assertThat;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.datasource.DataRecord;
import com.ericsson.cifwk.taf.datasource.TestDataSource;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.ericsson.cifwk.taf.spi.DataSourceAdapter;
import com.ericsson.cifwk.tdm.adapter.TdmDataSourceAdapter;
import com.google.common.base.Optional;
import com.google.common.base.Stopwatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

public class TDMPerformanceTest {

    private static final Logger LOGGER = LogManager.getLogger(TDMPerformanceTest.class);

    private static final String GLOBAL_DATA_SOURCE = "globalDataSource";

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");

    private static final TdmDataSourceAdapter tdmDataSourceAdapter = getAdapter();
    private static final String DURATION = System.getProperty("tdm.performance.test.duration", "24");

    @Before
    public void init() {
        LOGGER.info("timestamp,dataSourceId,vUser,execTime,success");
        createDatasoure();
    }

    private static TestDataSource<DataRecord> createDatasoure(){
        TestDataSource<DataRecord> dataSource = TafTestContext.getContext().dataSource(GLOBAL_DATA_SOURCE);
        dataSource.addRecord().setField("dataSourceId", "tdm-id-1");
        dataSource.addRecord().setField("dataSourceId", "tdm-id-2");
        dataSource.addRecord().setField("dataSourceId", "tdm-id-3");
        dataSource.addRecord().setField("dataSourceId", "tdm-id-4");
        dataSource.addRecord().setField("dataSourceId", "tdm-id-5");
        return dataSource;
    }

    @Test
    public void tdmPerformanceScenario() {
        System.out.println(DURATION);
        TestScenario scenario = TestScenarios.scenario()
                .addFlow(flow("tdmPerformanceFlow")
                        .withVusers(100)
                        .withDuration(Long.valueOf(DURATION), TimeUnit.HOURS)
                        .withDataSources(dataSource(GLOBAL_DATA_SOURCE))
                        .addTestStep(annotatedMethod(this, "tdmPerformanceTestStep")))
                .build();
        runner().build().start(scenario);
    }

    @TestStep(id = "tdmPerformanceTestStep")
    public synchronized void tdmPerformanceTestStep(@Input(value = "dataSourceId") String tdmDataSourceId) {
        Stopwatch timer = Stopwatch.createStarted();
        boolean success = false;
        try {
            Optional<TestDataSource<DataRecord>> adapterProviders =
                    tdmDataSourceAdapter.provide(tdmDataSourceId, null, null, null, DataRecord.class);
            assertThat(adapterProviders.isPresent()).isTrue();
            adapterProviders.get().close();
            success = true;
        } catch (Throwable ex) {
            success = false;
            LOGGER.error("Unexpected exception: ", ex);
        } finally {
            LOGGER.info(String.format("%s,%s,%s,%s,%s", SDF.format(new Date()), tdmDataSourceId, TafTestContext.getContext().getVUser(), timer.elapsed(TimeUnit.MILLISECONDS), success));
        }
    }

    private static TdmDataSourceAdapter getAdapter() {
        List<DataSourceAdapter> tdmDataSourceAdapterList = ServiceRegistry.getAllDataSourceAdapters();

        for (DataSourceAdapter tdmDataSourceAdapter : tdmDataSourceAdapterList) {
            if (tdmDataSourceAdapter instanceof TdmDataSourceAdapter) {
                return (TdmDataSourceAdapter) tdmDataSourceAdapter;
            }
        }

        throw new IllegalStateException(String.format("Adapter %s not found", TdmDataSourceAdapter.class.getCanonicalName()));
    }

}


