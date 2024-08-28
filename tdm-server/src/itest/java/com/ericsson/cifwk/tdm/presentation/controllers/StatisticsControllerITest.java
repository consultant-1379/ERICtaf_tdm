package com.ericsson.cifwk.tdm.presentation.controllers;

import com.ericsson.cifwk.tdm.api.model.StatisticsObject;
import com.ericsson.cifwk.tdm.api.model.UserAgent;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceMetricRepository;
import com.ericsson.cifwk.tdm.application.datasources.UserSessionRepository;
import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.model.UserSessionEntity;
import com.ericsson.cifwk.tdm.presentation.controllers.client.DataSourceControllerClient;
import com.ericsson.cifwk.tdm.presentation.controllers.client.StatisticsControllerClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.LAST_APPROVED_VERSION_IS_UNAPPROVED;
import static com.google.common.truth.Truth.assertThat;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs", invokeCleanBeforeMethod = true)
public class StatisticsControllerITest {

    @Autowired
    private StatisticsControllerClient statisticsControllerClient;

    @Autowired
    private DataSourceControllerClient dataSourceControllerClient;

    @Autowired
    private DataSourceMetricRepository dataSourceMetricRepository;

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Before
    public void setUp() throws Exception {
        userSessionRepository.insert(createUserSession("ted"));
        userSessionRepository.insert(createUserSession("ted"));
        userSessionRepository.insert(createUserSession("ted"));
        userSessionRepository.insert(createUserSession("ted"));
    }

    @Test
    public void shouldGetUserStatistics() throws Exception {
        List<StatisticsObject> userUsage = statisticsControllerClient.getUserUsage();

        assertThat(userUsage.size()).isEqualTo(1);

        StatisticsObject statisticsObject = userUsage.get(userUsage.size() - 1);

        assertThat(statisticsObject.getValue()).isEqualTo(4);
    }

    @Test
    public void shouldGetDataSourceStatistics() throws Exception {
        dataSourceControllerClient.getRecords(LAST_APPROVED_VERSION_IS_UNAPPROVED);

        List<StatisticsObject> dataSourcesFromRest = statisticsControllerClient.getDataSourcesUsage(
                UserAgent.REST.getName());

        assertThat(dataSourcesFromRest.size()).isEqualTo(1);
    }

    private UserSessionEntity createUserSession(String username) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setCreatedAt(new Date());
        userSessionEntity.setSessionId("test");
        userSessionEntity.setUsername(username);

        return userSessionEntity;

    }
}
