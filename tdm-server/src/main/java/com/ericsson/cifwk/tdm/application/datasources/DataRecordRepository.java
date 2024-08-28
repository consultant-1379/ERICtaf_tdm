package com.ericsson.cifwk.tdm.application.datasources;

import static java.util.stream.Collectors.toList;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.jongo.MongoCursor;
import org.springframework.stereotype.Repository;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.RecordPredicate;
import java.util.TreeMap;
import java.util.SortedMap;

@Repository
public class DataRecordRepository extends BaseRepository<DataRecordEntity> {

    public static final String DATA_RECORDS_COLLECTION = "dataRecords";
    private static final String DATA_SOURCE_ID_COLUMN_NAME = "dataSourceId";
    private static final List<String> REQUIRED_FIELDS = newArrayList(DATA_SOURCE_ID_COLUMN_NAME);


    public DataRecordRepository() {
        super(DATA_RECORDS_COLLECTION, DataRecordEntity.class);
    }

    public void update(Map<String, DataRecordEntity> dataRecords) {
        Collection<DataRecordEntity> allRecords = dataRecords.values();
        List<DataRecordEntity> newValues = allRecords.stream().filter(e -> e.getId() == null).collect(toList());
        List<DataRecordEntity> oldValues = allRecords.stream().filter(e -> e.getId() != null).collect(toList());

        if (!newValues.isEmpty()) {
            insert(newValues);
        }

        for (DataRecordEntity oldValue : oldValues) {
            update(oldValue);
        }
    }

    public SortedMap<String, DataRecordEntity> find(String dataSourceId, List<RecordPredicate> recordPredicates,
            List<String> visibleColumns) {
        String query = buildSearchQuery(dataSourceId, recordPredicates);
        String visibleFieldsProjection = buildVisibleColumnsProjection(visibleColumns);
        MongoCursor<DataRecordEntity> result = getCollection()
                .find(query)
                .projection(visibleFieldsProjection)
                .as(DataRecordEntity.class);
        return StreamSupport                            //NOSONAR
                .stream(result.spliterator(), false)    //NOSONAR
                .collect(Collectors.toMap(DataRecordEntity::getId, k -> k,
                    (v1, v2) -> {
                        throw new RuntimeException(String.format(
                            "Duplicate key for values %s and %s", v1, v2));
                    }, TreeMap::new));
    }

    public List<DataRecordEntity> findAll(Collection<String> ids) {
        List<ObjectId> objectIds = ids.stream().map(item -> new ObjectId(item)).collect(toList());
        MongoCursor<DataRecordEntity> result = getCollection()
                .find("{_id: {$in: #}}", objectIds)
                .as(DataRecordEntity.class);
        return StreamSupport                            //NOSONAR
                .stream(result.spliterator(), false)    //NOSONAR
                .collect(toList());
    }

    private static String buildSearchQuery(String dataSourceId, List<RecordPredicate> recordPredicates) {
        List<String> searchParams = newArrayList("dataSourceId:'" + dataSourceId + "'");
        searchParams.addAll(predicateToStringList(recordPredicates));
        return searchParams.stream().collect(QUERY_PROPERTIES_COLLECTOR);
    }

    private static List<String> predicateToStringList(List<RecordPredicate> predicates) {
        return predicates.stream().map(RecordPredicate::toString).collect(toList());
    }

    private static String buildVisibleColumnsProjection(List<String> visibleColumns) {
        String projection = StringUtils.EMPTY;
        if (!visibleColumns.isEmpty()) {
            List<String> columns = newArrayList(DATA_SOURCE_ID_COLUMN_NAME);
            columns.addAll(visibleColumns);
            projection = joinIntoProjection(REQUIRED_FIELDS, columns, "values");
        }
        return projection;
    }

    public List<String> findIds(String dataSourceId) {
        MongoCursor<DataRecordEntity> result = getCollection()
                .find("{dataSourceId: #}", dataSourceId)
                .projection("{_id: 1}")
                .as(DataRecordEntity.class);
        return StreamSupport                            //NOSONAR
                .stream(result.spliterator(), false)    //NOSONAR
                .map(DataRecordEntity::getId)
                .collect(toList());
    }
}
