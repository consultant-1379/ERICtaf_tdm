package com.ericsson.cifwk.tdm.application.metrics;

import com.ericsson.cifwk.tdm.api.model.UserMetricsObject;
import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.application.datasources.UserSessionRepository;
import com.ericsson.cifwk.tdm.model.UserSessionEntity;
import org.joda.time.DateTime;
import org.jongo.Jongo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.Date;

import org.joda.time.LocalDate;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

/**
 * All test data has been selected from {@code InitialChangelog} change set "mockData"
 *
 * @see com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog#mockData(Jongo)
 */
@ContextConfiguration(classes = UserSessionRepository.class)
public class UserSessionRepositoryITest extends QueryExecutionStatisticsITest {

    @Autowired
    private UserSessionRepository userSessionRepository;

    public UserSessionRepositoryITest() {
        super(UserSessionRepository.USER_SESSION_COLLECTION);
    }

    @Test
    public void testFindByMonth() throws Exception {
        DateTime dateTime = new DateTime(new Date());

        List<UserSessionEntity> UserSessions = newArrayList(
                userSession("Homer", dateTime.minusMonths(7).toDate()),
                userSession("Maggie", dateTime.minusMonths(6).toDate()),
                userSession("Marg", dateTime.minusMonths(5).toDate()),
                userSession("Bart", dateTime.minusMonths(5).toDate()),
                userSession("Lisa", dateTime.minusMonths(3).toDate()));
        userSessionRepository.insert(UserSessions);
        List<UserMetricsObject> byMonth = userSessionRepository.findByDate(dateTime.minusMonths(6).toDate());
        assertThat(byMonth.size()).isEqualTo(3);

        assertThat(byMonth.get(0).getValue()).isEqualTo(1);
        assertThat(byMonth.get(1).getValue()).isEqualTo(2);
        assertThat(byMonth.get(2).getValue()).isEqualTo(1);
    }

    @Test
    public void testOrderAndYearFallOver() throws Exception {
        LocalDate localDate = new LocalDate(2016, 11, 10);
        DateTime dateTime = new DateTime(localDate.toDateTimeAtStartOfDay());
        List<UserSessionEntity> UserSessions = newArrayList(
                userSession("Homer", dateTime.toDate()),
                userSession("Bart", dateTime.plusMonths(2).toDate()),
                userSession("Marg", dateTime.plusMonths(1).toDate()),
                userSession("Lisa", dateTime.plusMonths(3).toDate()));
        userSessionRepository.insert(UserSessions);
        List<UserMetricsObject> byMonth = userSessionRepository.findByDate(
                dateTime.plusMonths(3)
                        .minusMonths(6)
                        .toDate());

        assertThat(byMonth.size()).isEqualTo(4);

        assertThat(byMonth.get(0).getMonth()).isEqualTo("11");
        assertThat(byMonth.get(0).getYear()).isEqualTo("2016");
        assertThat(byMonth.get(1).getMonth()).isEqualTo("12");
        assertThat(byMonth.get(1).getYear()).isEqualTo("2016");
        assertThat(byMonth.get(2).getMonth()).isEqualTo("1");
        assertThat(byMonth.get(2).getYear()).isEqualTo("2017");
        assertThat(byMonth.get(3).getMonth()).isEqualTo("2");
        assertThat(byMonth.get(3).getYear()).isEqualTo("2017");
    }

    public UserSessionEntity userSession(String username, Date date) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setUsername(username);
        userSessionEntity.setCreatedAt(date);
        userSessionEntity.setSessionId(UUID.randomUUID().toString());
        return userSessionEntity;
    }
}