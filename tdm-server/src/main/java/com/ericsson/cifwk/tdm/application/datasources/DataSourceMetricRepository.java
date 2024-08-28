package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.DataSourceMetricsObject;
import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.DataSourceMetricEntity;
import org.jongo.Aggregate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Repository
public class DataSourceMetricRepository extends BaseRepository<DataSourceMetricEntity> {

    public static final String DATA_SOURCE_METRIC_COLLECTION = "dataSourceMetric";

    public DataSourceMetricRepository() {
        super(DATA_SOURCE_METRIC_COLLECTION, DataSourceMetricEntity.class);
    }

    public List<DataSourceMetricsObject> findByMonthAndAgent(Date date, String type) {
        Aggregate aggregate = getCollection()
                .aggregate("{$match : { createdAt: {$gte:#}, userAgent: {$eq:#}} }", date, type)
                .and("{ $group: { _id: {dataSourceId: '$dataSourceId', name: '$dataSourceName', " +
                        "context: '$contextName', userAgent: '$userAgent' }, total: {$sum: 1}}}")
                .and("{ $sort: { total: -1} }");

        Iterator<DataSourceMetricsObject> iterator = aggregate.as(DataSourceMetricsObject.class).iterator();
        ArrayList<DataSourceMetricsObject> list = new ArrayList<>();
        iterator.forEachRemaining(list::add);

        return list;
    }
}
