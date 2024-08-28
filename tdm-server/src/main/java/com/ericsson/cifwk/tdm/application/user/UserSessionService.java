package com.ericsson.cifwk.tdm.application.user;

import com.ericsson.cifwk.tdm.api.model.Month;
import com.ericsson.cifwk.tdm.api.model.StatisticsObject;
import com.ericsson.cifwk.tdm.api.model.UserMetricsObject;
import com.ericsson.cifwk.tdm.application.datasources.UserSessionRepository;
import com.ericsson.cifwk.tdm.model.UserSessionEntity;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserSessionService {

    static final int PREVIOUS_MONTHS = 8;

    @Autowired
    private UserSessionRepository userSessionRepository;

    public void addUserSession(String sessionId, String userName) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setUsername(userName);
        userSessionEntity.setSessionId(sessionId);
        userSessionEntity.setCreatedAt(new Date());

        userSessionRepository.insert(userSessionEntity);
    }

    public List<StatisticsObject> getLoggedOnUsers() {
        DateTime dateTime = new DateTime(new Date());
        List<UserMetricsObject> byMonth = userSessionRepository
                .findByDate(dateTime.minusMonths(PREVIOUS_MONTHS).toDate());
        return byMonth.stream()
                .map(item -> new StatisticsObject(Month.getMonthName(item.getMonth()) + " " +
                        item.getYear(), item.getValue()))
                .collect(Collectors.toList());
    }
}
