package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.UserMetricsObject;
import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.UserSessionEntity;
import org.jongo.Aggregate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Repository
public class UserSessionRepository extends BaseRepository<UserSessionEntity> {

    public static final String USER_SESSION_COLLECTION = "userSession";

    public UserSessionRepository() {
        super(USER_SESSION_COLLECTION, UserSessionEntity.class);
    }

    public List<UserMetricsObject> findByDate(Date date) {
        Aggregate aggregate = getCollection().aggregate("{$match : { createdAt: {$gte:#}}}", date)
                .and("{ $group: { _id: {month:{$month: '$createdAt'}, year: {$year: '$createdAt'}}, total: {$sum: 1}}}")
                .and("{ $sort: { _id.year: 1, _id.month: 1 } }");

        Iterator<UserMetricsObject> iterator = aggregate.as(UserMetricsObject.class).iterator();
        ArrayList<UserMetricsObject> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);

        return list;
    }
}
