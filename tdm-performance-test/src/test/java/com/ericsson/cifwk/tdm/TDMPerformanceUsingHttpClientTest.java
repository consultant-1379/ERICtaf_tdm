package com.ericsson.cifwk.tdm;

import com.ericsson.cifwk.taf.TafTestContext;
import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.configuration.TafConfigurationProvider;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.taf.scenario.TestScenarios;
import com.google.common.base.Stopwatch;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.iterable;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;

public class TDMPerformanceUsingHttpClientTest {

    private static final Logger LOGGER = LogManager.getLogger(TDMPerformanceUsingHttpClientTest.class);

    private static final SimpleDateFormat SDF = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss.SSS");

    private static final Map<String, String> DATA_SOURCES = new HashMap<>();

    private static final ThreadLocal<CloseableHttpClient> HTTP_CLIENT = new ThreadLocal<CloseableHttpClient>() {
        @Override
        protected CloseableHttpClient initialValue() {
            return HttpClients.createDefault();
        }
    };

    private static final int MAX_VUSERS = 100;

    private static final String HOST = TafConfigurationProvider.provide().getString("tdm.api.host", "https://taf-tdm-prod"
            + ".seli.wh.rnd.internal.ericsson.com/api/");

    @Before
    public void init() {
        DATA_SOURCES.put("tdm-id-1", "586e04b39932c65371c37503");
        DATA_SOURCES.put("tdm-id-2", "586e05929932c4083cadee53");
        DATA_SOURCES.put("tdm-id-3", "586e061f9932c4083cadf035");
        DATA_SOURCES.put("tdm-id-4", "586e06559932c65371c37a65");
        DATA_SOURCES.put("tdm-id-5", "586e06729932c65371c37af7");

        LOGGER.info("timestamp,dataSourceId,vUser,execTime,success");


    }

    @DataSource
    public List<Map<String, Object>> createDataSource() {
        return Arrays.asList(createDataRecord("tdm-id-1"), createDataRecord("tdm-id-2"), createDataRecord("tdm-id-3"), createDataRecord("tdm-id-4"), createDataRecord("tdm-id-5"));
    }

    private Map<String, Object> createDataRecord(String dataSourceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("dataSourceId", dataSourceId);
        return data;
    }

    @Test
    public void tdmPerformanceScenario() {
        TestScenario scenario = TestScenarios.scenario().addFlow(
                flow("tdmPerformanceFlow")
                        .withVusers(MAX_VUSERS)
                        .withDuration(24, TimeUnit.HOURS)
                        .withDataSources(iterable("dataSourceIds", createDataSource()))
                        .addTestStep(annotatedMethod(this, "tdmPerformanceTestStep"))
        ).build();
        runner().build().start(scenario);
    }

    @TestStep(id = "tdmPerformanceTestStep")
    public synchronized void tdmPerformanceTestStep(@Input(value = "dataSourceId") String tdmDataSourceId) {
        Stopwatch timer = Stopwatch.createStarted();
        boolean success = false;
        try {
            CloseableHttpClient client = HTTP_CLIENT.get();
            HttpGet request = new HttpGet(HOST + "datasources/" + DATA_SOURCES.get(tdmDataSourceId) + "/records");
            try (CloseableHttpResponse response = client.execute(request)) {
                StatusLine statusLine = response.getStatusLine();
                success = statusLine != null && statusLine.getStatusCode() == 200;
                if (success) {
                    new BasicResponseHandler().handleResponse(response);
                }
            }
        } catch (Throwable ex) {
            success = false;
            LOGGER.error("Unexpected exception: ", ex);
        } finally {
            LOGGER.info(String.format("%s,%s,%s,%s,%s", SDF.format(new Date()), tdmDataSourceId, TafTestContext.getContext().getVUser(), timer.elapsed(TimeUnit.MILLISECONDS), success));
        }
    }
}
