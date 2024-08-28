package com.ericsson.cifwk.tdm.application.metrics;

import com.ericsson.cifwk.tdm.api.model.DataSourceMetricsObject;
import com.ericsson.cifwk.tdm.api.model.UserAgent;
import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceMetricRepository;
import com.ericsson.cifwk.tdm.model.DataSourceMetricEntity;
import org.joda.time.DateTime;
import org.jongo.Jongo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

/**
 * All test data has been selected from {@code InitialChangelog} change set "mockData"
 *
 * @see com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog#mockData(Jongo)
 */
@ContextConfiguration(classes = DataSourceMetricRepository.class)
public class DataSourceMetricRepositoryITest extends QueryExecutionStatisticsITest {

    @Autowired
    private DataSourceMetricRepository dataSourceMetricRepository;

    public DataSourceMetricRepositoryITest() {
        super(DataSourceMetricRepository.DATA_SOURCE_METRIC_COLLECTION);
    }

    @Test
    public void testFindByMonthAndAgent() throws Exception {
        DateTime dateTime = new DateTime(new Date());

        List<DataSourceMetricEntity> dataSourceMetricEntities = newArrayList(
                dataSourceMetric("test", "1", "Browser", "PM"),
                dataSourceMetric("test", "1", "Browser", "PM"),
                dataSourceMetric("test", "1", "Rest", "PM"),
                dataSourceMetric("test", "1", "Rest", "PM"),
                dataSourceMetric("test", "1", "Rest", "PM"));
        dataSourceMetricRepository.insert(dataSourceMetricEntities);
        List<DataSourceMetricsObject> dataSourceMetricsObjects =
                dataSourceMetricRepository.findByMonthAndAgent(
                        dateTime.minusMonths(6).toDate(), UserAgent.REST.getName());

        assertThat(dataSourceMetricsObjects.size()).isEqualTo(1);

        assertThat(dataSourceMetricsObjects.get(0).getTotal()).isEqualTo(3);

        dataSourceMetricsObjects =
                dataSourceMetricRepository.findByMonthAndAgent(
                        dateTime.minusMonths(6).toDate(), UserAgent.BROWSER.getName());

        assertThat(dataSourceMetricsObjects.size()).isEqualTo(1);

        assertThat(dataSourceMetricsObjects.get(0).getTotal()).isEqualTo(2);


    }

    private DataSourceMetricEntity dataSourceMetric(String dataSourceName, String dataSourceId,
                                                          String userAgent, String contextName) {
        DataSourceMetricEntity dataSourceMetricEntity = new DataSourceMetricEntity();
        dataSourceMetricEntity.setCreatedAt(new Date());
        dataSourceMetricEntity.setDataSourceName(dataSourceName);
        dataSourceMetricEntity.setDataSourceId(dataSourceId);
        dataSourceMetricEntity.setUserAgent(userAgent);
        dataSourceMetricEntity.setContextName(contextName);

        return dataSourceMetricEntity;
    }
}