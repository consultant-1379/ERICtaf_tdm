package com.ericsson.cifwk.tdm.infrastructure.changelogs;

import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.application.datasources.AppliedDataSourceActionAccumulator;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.DataSourceLabelEntity;
import com.ericsson.cifwk.tdm.model.Execution;
import com.ericsson.cifwk.tdm.model.PreferencesEntity;
import com.ericsson.cifwk.tdm.model.UserSessionEntity;
import com.ericsson.cifwk.tdm.model.Version;
import com.github.mongobee.changeset.ChangeLog;
import com.github.mongobee.changeset.ChangeSet;
import com.google.common.collect.ImmutableSortedMap;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.MongoCursor;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.application.contexts.ContextRepository.CONTEXTS;
import static com.ericsson.cifwk.tdm.application.datasources.DataRecordRepository.DATA_RECORDS_COLLECTION;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionRepository.DATA_SOURCE_ACTIONS;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceLabelRepository.DATA_SOURCES_LABELS;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceMetricRepository.DATA_SOURCE_METRIC_COLLECTION;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceRepository.DATA_SOURCES_COLLECTION;
import static com.ericsson.cifwk.tdm.application.datasources.UserSessionRepository.USER_SESSION_COLLECTION;
import static com.ericsson.cifwk.tdm.application.executions.DataSourceExecutionRepository.DATA_SOURCE_EXECUTIONS;
import static com.ericsson.cifwk.tdm.application.executions.ExecutionRepository.EXECUTIONS_COLLECTION;
import static com.ericsson.cifwk.tdm.application.locks.LockRepository.LOCKS_COLLECTION;
import static com.ericsson.cifwk.tdm.application.preferences.PreferencesRepository.PREFERENCES_COLLECTION;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.DEVELOPMENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.TEST;
import static com.ericsson.cifwk.tdm.model.DataRecordEntity.DataRecordEntityBuilder.aDataRecordEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.approvalStatus;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.recordDelete;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.recordEditSingleCell;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder.aDataSourceActionEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.COMMENT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEWERS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEW_REQUESTER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.VERSION;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceLabelEntityBuilder.aDataSourceLabelEntity;
import static com.ericsson.cifwk.tdm.model.PreferencesEntity.PreferencesEntityBuilder.aPreferencesEntity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newTreeMap;
import static java.util.Collections.emptySortedMap;
import static java.util.stream.Collectors.toList;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 19/02/2016
 */
@ChangeLog
@SuppressWarnings("unused")
public class InitialChangelog {

    public static final String STAR_WARS_DS_ID = "56c5ddd29759e577fc68ab74";
    public static final String PREVIOUSLY_APPROVED_DATA_SOURCE = "56c5ddd99999e577fc68ab74";
    public static final String UNAPPROVED_DATA_SOURCE = "56c5ddd99999e999fc68ab74";
    public static final String LAST_APPROVED_VERSION_IS_UNAPPROVED = "56c5ddd99999e999fc99ab99";
    public static final String ASSURE_CONTEXT_ID = "b9192db0-e487-4dc0-bc05-712da4b53345";
    public static final String ASSURE_CONEXT = "ASSURE";
    public static final String GROUP = "com.ericsson.firstds";
    public static final String KEY_1 = "key1";
    public static final String KEY_2 = "key2";
    public static final String KEY_3 = "key3";
    public static final String VALUE_1 = "value1";
    public static final String VALUE_2 = "value2";
    public static final String VALUE_3 = "value3";
    public static final String ROTATION_PERIOD = "rotation_period";
    public static final String ORBITAL_PERIOD = "orbital_period";
    public static final String DIAMETER = "diameter";
    public static final String CLIMATE = "climate";
    public static final String TEMPERATE = "temperate";
    public static final String GRAVITY = "gravity";
    public static final String STANDARD_1 = "1 standard";
    public static final String TERRAIN = "terrain";
    public static final String SURFACE_WATER = "surface_water";
    public static final String POPULATION = "population";
    public static final String UNKNOWN = "unknown";
    public static final String ENIKOAL = "enikoal";
    public static final String ENM_CONTEXT = "ENM";
    public static final String ENM_CONTEXT_ID = "da608d9f-905d-45a2-bdba-bb187aa94271";
    public static final String CONTEXT_NAME_INDEX = "{contextId:1, name:1}";
    public static final String DATA_RECORD_LOCKS_COLLECTION = "dataRecordLocks";


    @ChangeSet(order = "001", id = "initialSchema", author = "TDM")
    public void schema(DB db) {
        db.createCollection(DATA_SOURCES_COLLECTION, null);
        db.createCollection(DATA_RECORDS_COLLECTION, null);
        db.createCollection(EXECUTIONS_COLLECTION, null);
        db.createCollection(CONTEXTS, null);
        db.createCollection(LOCKS_COLLECTION, null);
        db.createCollection(DATA_RECORD_LOCKS_COLLECTION, null);
        db.createCollection(DATA_SOURCE_EXECUTIONS, null);
        db.createCollection(PREFERENCES_COLLECTION, null);

        db.getCollection(DATA_RECORD_LOCKS_COLLECTION)
                .createIndex(new BasicDBObject("dataRecordId", 1), new BasicDBObject("unique", true));
        db.getCollection(DATA_RECORD_LOCKS_COLLECTION)
                .createIndex(new BasicDBObject("expireAt", 1), new BasicDBObject("expireAfterSeconds", 0));
    }

    @ChangeSet(order = "002", id = "indexes", author = "TDM")
    public void indexes(Jongo jongo) {
        createIndex(jongo, CONTEXTS, "{systemId:1}");
        createIndex(jongo, CONTEXTS, "{parentId:1}");
        createIndex(jongo, CONTEXTS, "{name:1}");

        createIndex(jongo, DATA_SOURCES_COLLECTION, CONTEXT_NAME_INDEX);
        createIndex(jongo, DATA_SOURCES_COLLECTION, "{deleted:1}");

        createIndex(jongo, DATA_RECORDS_COLLECTION, "{dataSourceId:1}");

        createIndex(jongo, DATA_SOURCE_ACTIONS, "{parentId:1, type:1}");
    }

    @ChangeSet(order = "003", id = "versions", author = "TDM")
    public void versions(Jongo jongo) {
        MongoCollection collection = jongo.getCollection(DATA_SOURCES_COLLECTION);
        MongoCursor<DataSourceIdentityEntity> dataSources = collection.find().as(DataSourceIdentityEntity.class);

        for (DataSourceIdentityEntity dataSourceIdentityEntity : dataSources) {
            dataSourceIdentityEntity.setVersion(dataSourceIdentityEntity.getVersion());
            if (!ApprovalStatus.APPROVED.equals(dataSourceIdentityEntity.getApprovalStatus())) {
                dataSourceIdentityEntity.getVersion().setSnapshot(true);
            } else {
                dataSourceIdentityEntity.getVersion().setSnapshot(false);
            }
            update(jongo, DATA_SOURCES_COLLECTION, dataSourceIdentityEntity);
            // add new version to datasource as approved
            if (ApprovalStatus.APPROVED.equals(dataSourceIdentityEntity.getApprovalStatus())) {
                createNewVersion(jongo, dataSourceIdentityEntity);
            }
        }

        MongoCollection actionsCollection = jongo.getCollection(DATA_SOURCE_ACTIONS);
        MongoCursor<DataSourceActionEntity> dataSourceActions = actionsCollection.find()
                .as(DataSourceActionEntity.class);

        for (DataSourceActionEntity dataSourceActionEntity : dataSourceActions) {
            dataSourceActionEntity.setVersion(dataSourceActionEntity.getVersion());
            if (!APPROVED.equals(dataSourceActionEntity.getValues().get(APPROVAL_STATUS))) {
                dataSourceActionEntity.getVersion().setSnapshot(true);
            } else {
                dataSourceActionEntity.getVersion().setSnapshot(false);
            }
            update(jongo, DATA_SOURCE_ACTIONS, dataSourceActionEntity);
        }

    }

    @ChangeSet(order = "004", id = "userAndDataSourceMetrics", author = "TDM")
    public void userMetrics(DB db) {
        db.createCollection(USER_SESSION_COLLECTION, null);
        db.createCollection(DATA_SOURCE_METRIC_COLLECTION, null);
    }

    @ChangeSet(order = "005", id = "dropDataRecordsLockIndex", author = "TDM")
    public void changeDataRecordsLock(DB db) {
        db.getCollection(DATA_RECORD_LOCKS_COLLECTION)
                .dropIndex(new BasicDBObject("dataRecordId", 1));
    }

    @ChangeSet(order = "006", id = "UniqueIndexes", author = "TDM")
    public void uniqueIndexes(Jongo jongo) {
        jongo.getCollection(DATA_SOURCES_COLLECTION).dropIndex(CONTEXT_NAME_INDEX);
        createUniqueIndex(jongo, DATA_SOURCES_COLLECTION, CONTEXT_NAME_INDEX, " { unique: true }");
    }

    @ChangeSet(order = "007", id = "mockData", author = "TDM")
    @Profile({DEVELOPMENT, TEST, INTEGRATION_TEST})
    public void mockData(Jongo jongo) {
        createExecutions(jongo);
        createPreferences(jongo);
        createSampleDataSources(jongo);
        createStarWarsDataSource(jongo);
        createDataSourceWithApproveHistory(jongo);
        createUnapprovedDataSource(jongo);
        createDataSourceWithApproveHistoryWhereAllVersionsAreApproved(jongo);
        createDatasourceLabels(jongo);
    }

    @ChangeSet(order = "008", id = "DropDataRecordLocksCollection", author = "TDM")
    public void dropDataRecordLocksCollection(Jongo jongo) {
        jongo.getCollection(DATA_RECORD_LOCKS_COLLECTION).drop();
    }

    @ChangeSet(order = "009", id = "productionHealthCheckData", author = "TDM")
    public void productionHealthCheckData(Jongo jongo) {
        createDatasourceForAcceptanceTests(jongo);
    }

    private static void createExecutions(Jongo jongo) {
        Execution execution = new Execution();
        execution.setId("56c5ddf29759e577fc68ab7f");
        execution.setStartTime(new Date());
        execution.getProperties().put("team", "TAF");
        insert(jongo, EXECUTIONS_COLLECTION, execution);
    }

    private static void createPreferences(Jongo jongo) {
        PreferencesEntity preferences = aPreferencesEntity()
                .withUserId("taf")
                .withContextId("systemId-1")
                .build();
        insert(jongo, PREFERENCES_COLLECTION, preferences);
    }

    private static void createSampleDataSources(Jongo jongo) {
        DataSourceIdentityEntity deleted = dataSource(null, "DS0", "com.ericsson");
        deleted.setDeleted(true);
        List<DataSourceIdentityEntity> dataSources = newArrayList(
                deleted,
                dataSource("66c5fdd29759e577fc685b73", "DS1", "com.ericsson"),
                dataSource("76c5ddd29759e577fc68ab75", "DS2", "com.ericsson.taf"),
                dataSource("46c5ddd29759e577fc68ab78", "DS3", "com.ericsson.taf"),
                dataSource("86c5ddd29759e577fc68ab72", "DS4", "com.ericsson.tms"),
                dataSource("86c5ddd29759e577fc68ab82", "DS5", "host.netsim.111"),
                dataSource("86c5ddd29759e577fc99ab82", "DS6", "host.netsim.222")
        );
        insert(jongo, DATA_SOURCES_COLLECTION, dataSources);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceActions(dataSources));
    }

    private static DataSourceIdentityEntity dataSource(String id, String name, String group) {
        return aDataSourceIdentityEntity()
                .withId(id)
                .withInitialVersion()
                .withName(name)
                .withApprovalStatus(UNAPPROVED)
                .withGroup(group)
                .withContext(ASSURE_CONEXT)
                .withContextId(ASSURE_CONTEXT_ID)
                .withCreatedBy("ekonsla")
                .withCreateTimeNow()
                .build();
    }

    private static void createStarWarsDataSource(Jongo jongo) {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId(STAR_WARS_DS_ID)
                .withInitialVersion()
                .withName("Star wars planets")
                .withApprovalStatus(UNAPPROVED)
                .withGroup(GROUP)
                .withContext(ENM_CONTEXT)
                .withContextId(ENM_CONTEXT_ID)
                .withProperty(KEY_1, VALUE_1)
                .withProperty(KEY_2, VALUE_2)
                .withProperty(KEY_3, VALUE_3)
                .withCreatedBy(ENIKOAL)
                .withCreateTimeNow()
                .build();
        insert(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        List<DataRecordEntity> dataRecords = newArrayList(
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab75")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Alderaan")
                        .withValue(ROTATION_PERIOD, 24)
                        .withValue(ORBITAL_PERIOD, 364)
                        .withValue(DIAMETER, 12500)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "grasslands, mountains")
                        .withValue(SURFACE_WATER, 40)
                        .withValue(POPULATION, "2000000000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab76")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Yavin IV")
                        .withValue(ROTATION_PERIOD, 24)
                        .withValue(ORBITAL_PERIOD, 4818)
                        .withValue(DIAMETER, 10200)
                        .withValue(CLIMATE, "temperate, tropical")
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "jungle, rainforests")
                        .withValue(SURFACE_WATER, 8)
                        .withValue(POPULATION, "1000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab77")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Hoth")
                        .withValue(ROTATION_PERIOD, 23)
                        .withValue(ORBITAL_PERIOD, 549)
                        .withValue(DIAMETER, 7200)
                        .withValue(CLIMATE, "frozen")
                        .withValue(GRAVITY, "1.1 standard")
                        .withValue(TERRAIN, "tundra, ice caves, mountain ranges")
                        .withValue(SURFACE_WATER, 100)
                        .withValue(POPULATION, UNKNOWN)
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab78")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Dagobah")
                        .withValue(ROTATION_PERIOD, 23)
                        .withValue(ORBITAL_PERIOD, 341)
                        .withValue(DIAMETER, 8900)
                        .withValue(CLIMATE, "murky")
                        .withValue(GRAVITY, "N/A")
                        .withValue(TERRAIN, "swamp, jungles")
                        .withValue(SURFACE_WATER, 8)
                        .withValue(POPULATION, UNKNOWN)
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab79")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Bespin")
                        .withValue(ROTATION_PERIOD, 12)
                        .withValue(ORBITAL_PERIOD, 5110)
                        .withValue(DIAMETER, 118000)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, "1.5 (surface, 1 standard (Cloud City)")
                        .withValue(TERRAIN, "gas giant")
                        .withValue(SURFACE_WATER, 0)
                        .withValue(POPULATION, "6000000")
                        .build());
        insert(jongo, DATA_RECORDS_COLLECTION, dataRecords);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataRecordActions(dataSource, dataRecords));

        dataRecords = newArrayList(
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab7a")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Endor")
                        .withValue(ROTATION_PERIOD, 18)
                        .withValue(ORBITAL_PERIOD, 402)
                        .withValue(DIAMETER, 4900)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, "0.85 standard")
                        .withValue(TERRAIN, "forests, mountains, lakes")
                        .withValue(SURFACE_WATER, 8)
                        .withValue(POPULATION, "30000000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab7b")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Naboo")
                        .withValue(ROTATION_PERIOD, 26)
                        .withValue(ORBITAL_PERIOD, 312)
                        .withValue(DIAMETER, 12120)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "grassy hills, swamps, forests, mountains")
                        .withValue(SURFACE_WATER, 12)
                        .withValue(POPULATION, "4500000000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab7c")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Coruscant")
                        .withValue(ROTATION_PERIOD, 24)
                        .withValue(ORBITAL_PERIOD, 368)
                        .withValue(DIAMETER, 12240)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "cityscape, mountains")
                        .withValue(SURFACE_WATER, UNKNOWN)
                        .withValue(POPULATION, "1000000000000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab7d")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Kamino")
                        .withValue(ROTATION_PERIOD, 27)
                        .withValue(ORBITAL_PERIOD, 463)
                        .withValue(DIAMETER, 19720)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "ocean")
                        .withValue(SURFACE_WATER, 100)
                        .withValue(POPULATION, "1000000000")
                        .build(),
                aDataRecordEntity()
                        .withId("56c5ddd29759e577fc68ab7e")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Geonosis")
                        .withValue(ROTATION_PERIOD, 30)
                        .withValue(ORBITAL_PERIOD, 256)
                        .withValue(DIAMETER, 11370)
                        .withValue(CLIMATE, "temperate, arid")
                        .withValue(GRAVITY, "0.9 standard")
                        .withValue(TERRAIN, "rock, desert, mountain, barren")
                        .withValue(SURFACE_WATER, 5)
                        .withValue(POPULATION, "100000000000")
                        .build()
        );

        insert(jongo, DATA_SOURCE_ACTIONS, approvalDataSourceAction(dataSource, UNAPPROVED, dataRecords.size()));
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);

        dataSource.setVersion(dataSource.getVersion().incrementMinor());
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);

        insert(jongo, DATA_RECORDS_COLLECTION, dataRecords);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataRecordActions(dataSource, dataRecords));

        dataSource.getVersion().setSnapshot(false);
        insert(jongo, DATA_SOURCE_ACTIONS, approvalDataSourceAction(dataSource, APPROVED, dataRecords.size()));
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);

        dataSource.setVersion(dataSource.getVersion().incrementMinor());

        insert(jongo, DATA_SOURCE_ACTIONS, approvalDataSourceAction(dataSource, PENDING, dataRecords
                .size()));
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);

        dataSource.getVersion().setSnapshot(false);
        insert(jongo, DATA_SOURCE_ACTIONS, approvalDataSourceAction(dataSource, APPROVED, dataRecords.size()));
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);

        createNewVersion(jongo, dataSource);
    }

    private static void createDataSourceWithApproveHistory(Jongo jongo) {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId(PREVIOUSLY_APPROVED_DATA_SOURCE)
                .withInitialVersion()
                .withName("Approved data source")
                .withApprovalStatus(UNAPPROVED)
                .withGroup(GROUP)
                .withContext(ASSURE_CONEXT)
                .withContextId(ASSURE_CONTEXT_ID)
                .withProperty(KEY_1, VALUE_1)
                .withProperty(KEY_2, VALUE_2)
                .withProperty(KEY_3, VALUE_3)
                .withCreatedBy(ENIKOAL)
                .withCreateTimeNow()
                .build();
        insert(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        dataSource.setApprovalStatus(APPROVED);
        Version version1 = dataSource.getVersion().incrementMinor();
        version1.setSnapshot(false);
        dataSource.setVersion(version1);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        dataSource.setApprovalStatus(UNAPPROVED);
        Version version2 = dataSource.getVersion().incrementMinor();
        version2.setSnapshot(true);
        dataSource.setVersion(version2);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));
    }

    private static void createUnapprovedDataSource(Jongo jongo) {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId(UNAPPROVED_DATA_SOURCE)
                .withInitialVersion()
                .withName("Unapproved data source")
                .withApprovalStatus(UNAPPROVED)
                .withGroup(GROUP)
                .withContext(ASSURE_CONEXT)
                .withContextId(ASSURE_CONTEXT_ID)
                .withProperty(KEY_1, VALUE_1)
                .withProperty(KEY_2, VALUE_2)
                .withProperty(KEY_3, VALUE_3)
                .withCreatedBy(ENIKOAL)
                .withCreateTimeNow()
                .build();
        insert(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));
        List<DataRecordEntity> dataRecords = newArrayList(
                aDataRecordEntity()
                        .withId("56c5ddd29739e577fc68ab75")
                        .withDataSourceId(dataSource.getId())
                        .withValue("name", "Alderaan")
                        .withValue(ROTATION_PERIOD, 24)
                        .withValue(ORBITAL_PERIOD, 364)
                        .withValue(DIAMETER, 12500)
                        .withValue(CLIMATE, TEMPERATE)
                        .withValue(GRAVITY, STANDARD_1)
                        .withValue(TERRAIN, "grasslands, mountains")
                        .withValue(SURFACE_WATER, 40)
                        .withValue(POPULATION, "2000000000")
                        .build());
        insert(jongo, DATA_RECORDS_COLLECTION, dataRecords);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataRecordActions(dataSource, dataRecords));
    }

    private static void createDataSourceWithApproveHistoryWhereAllVersionsAreApproved(Jongo jongo) {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId(LAST_APPROVED_VERSION_IS_UNAPPROVED)
                .withInitialVersion()
                .withName("DS with approval history where last approved version is unapproved")
                .withApprovalStatus(UNAPPROVED)
                .withGroup(GROUP)
                .withContext(ASSURE_CONEXT)
                .withContextId(ASSURE_CONTEXT_ID)
                .withProperty(KEY_1, VALUE_1)
                .withProperty(KEY_2, VALUE_2)
                .withProperty(KEY_3, VALUE_3)
                .withCreatedBy(ENIKOAL)
                .withCreateTimeNow()
                .build();
        insert(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        dataSource.setApprovalStatus(APPROVED);
        Version version1 = dataSource.getVersion().incrementMinor();
        version1.setSnapshot(false);
        dataSource.setVersion(version1);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        dataSource.setApprovalStatus(UNAPPROVED);
        Version version2 = dataSource.getVersion().incrementMinor();
        version2.setSnapshot(true);
        dataSource.setVersion(version2);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        dataSource.setApprovalStatus(APPROVED);
        Version version3 = dataSource.getVersion().incrementMinor();
        version3.setSnapshot(false);
        dataSource.setVersion(version3);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));

        createNewVersion(jongo, dataSource);
    }

    private static void createDatasourceLabels(Jongo jongo) {
        DataSourceLabelEntity wookie = aDataSourceLabelEntity()
                .withId(null)
                .withName("wookie")
                .withVersion("0.0.3")
                .withDataSourceId(STAR_WARS_DS_ID)
                .withContextId(ENM_CONTEXT_ID)
                .build();

        insert(jongo, DATA_SOURCES_LABELS, wookie);
    }

    private static void createDatasourceForAcceptanceTests(final Jongo jongo) {
        DataSourceIdentityEntity dataSource = createDataSource(jongo);

        List<DataRecordEntity> dataRecords = createDataRecords(jongo, dataSource);

        int dataSourceActionsOrder = 2;
        sendForApproval(jongo, dataSource, dataSourceActionsOrder);
        dataSourceActionsOrder++;
        approve(jongo, dataSource, dataSourceActionsOrder);
        dataSourceActionsOrder++;
        createNextSnapshotVersion(jongo, dataSource, dataSourceActionsOrder);
        dataSourceActionsOrder++;

        modifyData(jongo, dataSource, dataRecords);

        dataSourceActionsOrder = sendForApproval(jongo, dataSource, dataSourceActionsOrder);
        dataSourceActionsOrder++;
        approve(jongo, dataSource, dataSourceActionsOrder);

    }

    private static void modifyData(final Jongo jongo, final DataSourceIdentityEntity dataSource,
            List<DataRecordEntity> dataRecords) {
        int dataRecordsActionsOrder = 5;
        DataSourceActionEntity deleteFirst = recordDelete(dataRecords.get(0), dataSource, dataRecordsActionsOrder);
        dataRecordsActionsOrder++;
        DataSourceActionEntity editSecond = recordEditSingleCell(dataRecords.get(1), dataSource, "sport", "gaelic",
                dataRecordsActionsOrder);
        dataRecordsActionsOrder++;
        DataSourceActionEntity deleteThird = recordDelete(dataRecords.get(2), dataSource, dataRecordsActionsOrder);
        dataRecordsActionsOrder++;
        DataSourceActionEntity editFourth = recordEditSingleCell(dataRecords.get(3), dataSource, "age", "40",
                dataRecordsActionsOrder);
        dataRecordsActionsOrder++;
        DataSourceActionEntity deleteFourth = recordDelete(dataRecords.get(3), dataSource, dataRecordsActionsOrder);
        dataRecordsActionsOrder++;
        DataSourceActionEntity deleteFifth = recordDelete(dataRecords.get(4), dataSource, dataRecordsActionsOrder);

        insert(jongo, DATA_SOURCE_ACTIONS, newArrayList(deleteFirst, editSecond, deleteThird, editFourth,
                deleteFifth));

        List<DataRecordEntity> newDataRecords = newArrayList(aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f67")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "golf")
                .withValue("age", 65)
                .build());

        insert(jongo, DATA_RECORDS_COLLECTION, newDataRecords);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataRecordActions(dataSource, newDataRecords));
    }

    private static List<DataRecordEntity> createDataRecords(final Jongo jongo,
            final DataSourceIdentityEntity dataSource) {
        List<DataRecordEntity> dataRecords = newArrayList(
                aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f62")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "football")
                .withValue("age", 24)
                .build(),
                aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f63")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "hurling")
                .withValue("age", 32)
                .build(),
                aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f64")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "rugby")
                .withValue("age", 28)
                .build(),
                aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f65")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "tennis")
                .withValue("age", 21)
                .build(),
                aDataRecordEntity()
                .withId("5af986e9e788f75d060b2f66")
                .withDataSourceId(dataSource.getId())
                .withValue("sport", "swimming")
                .withValue("age", 18)
                .build());

        insert(jongo, DATA_RECORDS_COLLECTION, dataRecords);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataRecordActions(dataSource, dataRecords));

        insert(jongo, DATA_SOURCE_ACTIONS, columnOrderChangeAction(dataSource, dataRecords));
        return dataRecords;
    }

    private static DataSourceIdentityEntity createDataSource(final Jongo jongo) {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId("5af986e9e788f75d060b2f4c")
                .withInitialVersion()
                .withName("acceptanceTestData")
                .withApprovalStatus(UNAPPROVED)
                .withGroup("group1")
                .withContext("System")
                .withContextId("systemId-1")
                .withCreatedBy("taf2")
                .withCreateTimeNow()
                .build();
        insert(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, initialDataSourceAction(dataSource));
        return dataSource;
    }


    /*-------------------------------- DataSourceActionEntity builders --------------------------------*/

    private static List<DataSourceActionEntity> initialDataSourceActions(List<DataSourceIdentityEntity> dataSources) {
        return dataSources.stream()
                .map(InitialChangelog::initialDataSourceAction)
                .collect(toList());
    }

    private static DataSourceActionEntity columnOrderChangeAction(final DataSourceIdentityEntity dataSource,
            final List<DataRecordEntity> dataRecords) {
        final DataSourceActionEntityBuilder builder = aDataSourceActionEntity()
                .withParentId(dataSource.getId())
                .withOrder(1)
                .withType(DataSourceActionType.COLUMN_ORDER_CHANGE);
        int order = 0;
        for (Entry<String, Object> entry : dataRecords
                .get(0)
                .getValues()
                .entrySet()) {
            builder.withValue(entry.getKey(), order);
        }
        return builder
                .withOrder(dataRecords.size())
                .withVersion(dataSource.getVersion())
                .withCreateTimeNow()
                .build();
    }

    private static int sendForApproval(final Jongo jongo, final DataSourceIdentityEntity dataSource,
            int dataSourceActionsOrder) {
        dataSource.setApprovalStatus(PENDING);
        dataSource.setReviewers(newArrayList("taf"));
        dataSource.setReviewRequester(dataSource.getCreatedBy());
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS,
                pendingDataSourceAction(dataSource, dataSourceActionsOrder));
        return dataSourceActionsOrder;
    }

    private static int approve(final Jongo jongo, final DataSourceIdentityEntity dataSource,
            int dataSourceActionsOrder) {
        dataSource.setApprovalStatus(APPROVED);
        Version version = new Version(dataSource.getVersion().toString());
        version.setSnapshot(false);
        dataSource.setVersion(version);
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS, approvalDataSourceAction(dataSource, APPROVED, dataSourceActionsOrder));
        return dataSourceActionsOrder;
    }

    private static int createNextSnapshotVersion(final Jongo jongo, final DataSourceIdentityEntity dataSource,
            int dataSourceActionsOrder) {
        dataSource.setApprovalStatus(UNAPPROVED);
        Version version = dataSource.getVersion().incrementMinor();
        version.setSnapshot(true);
        dataSource.setVersion(version);
        dataSource.setReviewers(newArrayList());
        dataSource.setReviewRequester("");
        dataSource.setApprover("");
        update(jongo, DATA_SOURCES_COLLECTION, dataSource);
        insert(jongo, DATA_SOURCE_ACTIONS,
                nextSnapShotVersionDataSourceAction(dataSource, dataSourceActionsOrder));
        return dataSourceActionsOrder;
    }

    /**
     * @see com.ericsson.cifwk.tdm.application.datasources.DataSourceActionsService
     */
    private static DataSourceActionEntity initialDataSourceAction(DataSourceIdentityEntity dataSource) {
        return aDataSourceActionEntity()
                .withParentId(dataSource.getId())
                .withType(DataSourceActionType.IDENTITY_INITIAL_CREATE)
                .withValues(dataSource.createValueMap())
                .withOrder(0)
                .withVersion(dataSource.getVersion())
                .withCreateTimeNow()
                .build();
    }

    private static DataSourceActionEntity approvalDataSourceAction(DataSourceIdentityEntity dataSource,
            ApprovalStatus status, int order) {
        final SortedMap<String, Object> approvalMap = ImmutableSortedMap.<String, Object>naturalOrder()
                .put(APPROVAL_STATUS, status.name())
                .put(APPROVER, "taf")
                .put(REVIEWERS, newArrayList("taf"))
                .put(COMMENT, "")
                .put(REVIEW_REQUESTER, dataSource.getCreatedBy())
                .put(VERSION, dataSource.getVersion())
                .build();
        return dataSourceApprovalStatusAction(dataSource, status, order, approvalMap);
    }

    private static DataSourceActionEntity dataSourceApprovalStatusAction(final DataSourceIdentityEntity dataSource,
            final ApprovalStatus status, final int order, final SortedMap<String, Object> approvalMap) {
        dataSource.setApprovalStatus(status);
        DataSourceActionEntity action = approvalStatus(dataSource, approvalMap);
        action.setAuditData(dataSource.getCreatedBy(), new Date());
        action.setOrder(order);
        action.setVersion(dataSource.getVersion());
        return action;
    }

    private static DataSourceActionEntity pendingDataSourceAction(final DataSourceIdentityEntity dataSource,
            final int dataSourceActionsOrder) {
        final SortedMap<String, Object> approvalMap = ImmutableSortedMap.<String, Object>naturalOrder()
                .put(APPROVAL_STATUS, PENDING)
                .put(REVIEWERS, newArrayList("taf"))
                .put(COMMENT, "")
                .put(REVIEW_REQUESTER, dataSource.getCreatedBy())
                .put(VERSION, dataSource.getVersion())
                .build();
        return dataSourceApprovalStatusAction(dataSource, PENDING, dataSourceActionsOrder, approvalMap);
    }

    private static DataSourceActionEntity nextSnapShotVersionDataSourceAction(final DataSourceIdentityEntity dataSource,
            final int dataSourceActionsOrder) {
        final SortedMap<String, Object> approvalMap = ImmutableSortedMap.<String, Object>naturalOrder()
                .put(APPROVAL_STATUS, UNAPPROVED)
                .put(APPROVER, "")
                .put(VERSION, dataSource.getVersion())
                .build();
        return dataSourceApprovalStatusAction(dataSource, UNAPPROVED, dataSourceActionsOrder, approvalMap);
    }

    private static List<DataSourceActionEntity> initialDataRecordActions(DataSourceIdentityEntity dataSource,
                                                                  List<DataRecordEntity> records) {
        AtomicInteger order = new AtomicInteger();

        return records.stream()
                .map(DataSourceActionEntity::recordAdd)
                .peek(a -> a.setVersion(dataSource.getVersion()))
                .peek(a -> a.setOrder(order.getAndIncrement()))
                .peek(a -> a.setAuditData(dataSource.getCreatedBy(), dataSource.getCreateTime()))
                .collect(toList());
    }

    private static void createNewVersion(Jongo jongo, DataSourceIdentityEntity identity) {
        Version newVersion = identity.getVersion();
        newVersion = newVersion.incrementMinor();
        newVersion.setSnapshot(true);
        identity.setVersion(newVersion);
        identity.setApprovalStatus(UNAPPROVED);

        SortedMap<String, Object> newValues = newTreeMap();
        newValues.put(APPROVAL_STATUS, UNAPPROVED);
        newValues.put(REVIEWERS, new ArrayList());
        newValues.put(APPROVER, "");
        DataSourceActionEntity newAction = approvalStatus(identity, newValues);
        newAction.setVersion(newVersion);
        newAction.setAuditData(identity.getCreatedBy(), new Date());
        newAction.apply(identity, emptySortedMap(), new AppliedDataSourceActionAccumulator());

        update(jongo, DATA_SOURCES_COLLECTION, identity);
        insert(jongo, DATA_SOURCE_ACTIONS, newAction);
    }

    public UserSessionEntity userSession(String username, Date date) {
        UserSessionEntity userSessionEntity = new UserSessionEntity();
        userSessionEntity.setUsername(username);
        userSessionEntity.setCreatedAt(date);
        userSessionEntity.setSessionId(UUID.randomUUID().toString());
        return userSessionEntity;
    }

    /*-------------------------------- Jongo --------------------------------*/

    private static void insert(Jongo jongo, String collection, Object object) {
        jongo.getCollection(collection).insert(object);
    }

    private static void insert(Jongo jongo, String collection, List<?> list) {
        jongo.getCollection(collection).insert(list.toArray());
    }

    private static void update(Jongo jongo, String collection, Object object) {
        jongo.getCollection(collection).save(object);
    }

    private static void createIndex(Jongo jongo, String collection, String index) {
        jongo.getCollection(collection).ensureIndex(index);
    }

    private static void createUniqueIndex(Jongo jongo, String collection, String index, String options) {
        jongo.getCollection(collection).ensureIndex(index, options);
    }
}
