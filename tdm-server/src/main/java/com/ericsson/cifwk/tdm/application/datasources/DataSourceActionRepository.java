package com.ericsson.cifwk.tdm.application.datasources;

import static java.lang.String.format;
import static java.util.Collections.emptyList;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.jongo.MongoCursor;
import org.springframework.stereotype.Repository;

import com.ericsson.cifwk.tdm.application.common.repository.BaseRepository;
import com.ericsson.cifwk.tdm.application.util.JsonParser;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.Version;
import com.google.common.annotations.VisibleForTesting;

@Repository
public class DataSourceActionRepository extends BaseRepository<DataSourceActionEntity> {

    public static final String DATA_SOURCE_ACTIONS = "dataSourceActions";
    private static VersionCondtion equalsCondition = JsonParser.
            parseObjectFile("data/version-condition-equals.json", VersionCondtion.class);
    private static VersionCondtion lessThanOrEqualsCondition = JsonParser.
            parseObjectFile("data/version-condition-lessThanOrEquals.json", VersionCondtion.class);

    private static final String DATA_SOURCE_ID_COLUMN_NAME = "id";
    private static final String DATA_SOURCE_TYPE_COLUMN_NAME = "type";
    private static final String DATA_SOURCE_PARENTID_COLUMN_NAME = "parentId";

    private static final List<String> REQUIRED_FIELDS = newArrayList(DATA_SOURCE_ID_COLUMN_NAME,
            DATA_SOURCE_TYPE_COLUMN_NAME, DATA_SOURCE_PARENTID_COLUMN_NAME);

    public DataSourceActionRepository() {
        super(DATA_SOURCE_ACTIONS, DataSourceActionEntity.class);
    }

    public List<DataSourceActionEntity> find(Collection<String> parentIds, String version) {
        return find(parentIds, version, emptyList());
    }

    public List<DataSourceActionEntity> find(Collection<String> parentIds, String version, List<String> visibleColumn) {
        String versionCondition = versionCondition(lessThanOrEqualsCondition, new Version(version));
        String visibleFieldsProjection = buildVisibleColumnsProjection(visibleColumn);

        MongoCursor<DataSourceActionEntity> result = getCollection()
                .find(format("{parentId:{$in:#}, $and:[%s]}", versionCondition), parentIds)
                .projection(visibleFieldsProjection)
                .sort("{'version.major':1, 'version.minor':1, 'version.build':1, order:1}")
                .as(DataSourceActionEntity.class);
        return newArrayList((Iterator<DataSourceActionEntity>) result);
    }

    public List<DataSourceActionEntity> findActionsByType(Collection<String> parentIds,
                                                          DataSourceActionType dataSourceActionType) {
        MongoCursor<DataSourceActionEntity> result = getCollection()
                .find("{parentId:{$in:#}, type: #}", parentIds, dataSourceActionType.toString())
                .sort("{'version.major':1, 'version.minor':1, 'version.build':1, order:1}")
                .as(DataSourceActionEntity.class);
        return newArrayList((Iterator<DataSourceActionEntity>) result);
    }

    public int numberOfActions(String parentId, Version version) {
        Long numActions = getCollection()
                .count("{parentId:#, 'version.major':#, 'version.minor':#, 'version.build':#}", parentId,
                        version.getMajor(), version.getMinor(), version.getBuild());
        return numActions.intValue();
    }

    public List<Version> findVersions(Set<String> parentIds) {
        return getCollection()
                .distinct("version")
                .query("{parentId:{$in:#}}", parentIds)
                .as(Version.class);
    }

    public List<DataSourceActionEntity> findHistory(String parentId) {
        MongoCursor<DataSourceActionEntity> actions = getCollection().find("{parentId:#, type:#}", parentId,
                DataSourceActionType.IDENTITY_APPROVAL_STATUS.toString())
                .as(DataSourceActionEntity.class);
        return newArrayList(actions.iterator());

    }

    public List<DataSourceActionEntity> findByDataSourceId(String dataSourceId) {
        MongoCursor<DataSourceActionEntity> actions = getCollection().find("{parentId:#}", dataSourceId)
                .as(DataSourceActionEntity.class);
        return newArrayList(actions.iterator());
    }

    public List<DataSourceActionEntity> findByDataSourceIdAndVersion(String dataSourceId, String version) {
        String versionCondition = versionCondition(equalsCondition, new Version(version));
        MongoCursor<DataSourceActionEntity> result = getCollection()
                .find(format("{parentId:#, $and:[%s]}", versionCondition), dataSourceId)
                .as(DataSourceActionEntity.class);
        return newArrayList(result.iterator());
    }

    public AtomicInteger findLatestOrder(Collection<String> parentIds) {
        MongoCursor<DataSourceActionEntity> actions = getCollection().find("{parentId:{$in:#}}", parentIds)
                .sort("{order : -1}").limit(1).as(DataSourceActionEntity.class);

        if (actions.hasNext()) {
            Integer order = actions.next().getOrder();
            return new AtomicInteger(order);
        } else {
            return new AtomicInteger(0);
        }
    }

    private static String versionCondition(VersionCondtion condition, Version version) {
        return format(condition.toString(), version.getMajor(), version.getMinor(), version.getBuild(),
                version.isSnapshot());
    }

    @VisibleForTesting
    protected String buildVisibleColumnsProjection(List<String> visibleColumns) {
        String projection = StringUtils.EMPTY;
        if (!visibleColumns.isEmpty()) {
            List<String> columns = newArrayList(DATA_SOURCE_ID_COLUMN_NAME);
            columns.add(DATA_SOURCE_TYPE_COLUMN_NAME);
            columns.add(DATA_SOURCE_PARENTID_COLUMN_NAME);
            columns.addAll(visibleColumns);
            projection = joinIntoProjection(REQUIRED_FIELDS, columns, "values");
        }
        return projection;
    }
}
