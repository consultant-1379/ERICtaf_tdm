package com.ericsson.cifwk.tdm.application.common.repository;

import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.infrastructure.ITestDbConfiguration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.jongo.Distinct;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.bson.Bson;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.Unmarshaller;
import org.jongo.query.Query;
import org.junit.runner.RunWith;
import org.mockito.stubbing.Answer;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;
import java.util.function.Function;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.base.MoreObjects.toStringHelper;
import static java.util.Arrays.copyOfRange;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * An abstract parent class for testing repositories extending {@link BaseRepository},
 * that allows checking query execution statistics, such as:
 * <ul>
 * <li>Number of documents that match the query condition</li>
 * <li>Total time in milliseconds required for query execution</li>
 * <li>Number of index entries scanned</li>
 * <li>Number of documents scanned</li>
 * <li>Name of the index used by the query</li>
 * </ul>
 * Child classes <strong>must</strong> provide a respective {@link BaseRepository}
 * collection name via {@code super("collectionName")} call inside of their constructors
 *
 * @see QueryExecutionStatisticsITest#explain(BaseRepository, Function)
 * @see ExecutionStats
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
@ContextConfiguration(classes = ITestDbConfiguration.class,
        initializers = ConfigFileApplicationContextInitializer.class)
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs")
public abstract class QueryExecutionStatisticsITest {

    protected static final String IDX_ID = "_id_";

    @SpyBean
    private Jongo jongo;

    private String collection;

    public QueryExecutionStatisticsITest(String collection) {
        this.collection = collection;
    }

    /**
     * <p>Intercepts a {@code repository} method call for query execution statistics gathering, by running <br/>
     * {@code db.collection.find(...).explain();} instead of a regular {@code db.collection.find(...);}.</p>
     * <p>Usage example:</p>
     * <pre>
     *     Execution&lt;Entity> execution = explain(repository, repo -> repo.findByName("foo"));
     *
     *     Entity entity = execution.result;
     *     assertThat(entity.getName()).isEqualTo("foo");
     *
     *     ExecutionStatistic stats = execution.stats;
     *     assertThat(stats.success).isTrue();
     *     assertThat(stats.numReturned).isEqualTo(1);
     *     assertThat(stats.executionMillis).isLessThan(10);
     * </pre>
     *
     * @param repository Repository object which method call query execution statistics will be gathered
     * @param methodCall Lambda expression for executing one of the {@code repository} methods
     *                   or a method reference to the {@code repository} method
     * @param <T>        {@code repository} type extending {@link BaseRepository}
     * @param <U>        {@code repository} method return type, which will be wrapped inside {@link Execution}
     * @return {@link Execution}
     * @see <a href="https://docs.mongodb.com/manual/reference/explain-results/">
     * MongoDB Manual: Query Optimization > Explain Results</a>
     */
    protected <T extends BaseRepository, U> Execution<U> explain(T repository, Function<T, U> methodCall) {
        ExecutionStats[] buffer = new ExecutionStats[1];
        spyMongoCollection(savingExecutionStatsTo(buffer));
        U result = methodCall.apply(repository);
        ExecutionStats stats = buffer[0];
        return new Execution<>(result, stats);
    }

    private void spyMongoCollection(Answer savingExecutionStats) {
        MongoCollection mongoCollection = spy(jongo.getCollection(collection));
        doReturn(mongoCollection).when(jongo).getCollection(collection);
        doAnswer(savingExecutionStats).when(mongoCollection).findOne(anyString(), anyVararg());
        doAnswer(savingExecutionStats).when(mongoCollection).find(anyString(), anyVararg());
        doAnswer(savingExecutionStats).when(mongoCollection).count(anyString(), anyVararg());
        spyMongoCollectionDistinct(savingExecutionStats, mongoCollection);
    }

    private void spyMongoCollectionDistinct(Answer savingExecutionStats, MongoCollection mongoCollection) {
        doAnswer(invocation -> {
            Distinct distinct = spy((Distinct) invocation.callRealMethod());
            doAnswer(savingExecutionStats).when(distinct).query(anyString(), anyVararg());
            return distinct;
        }).when(mongoCollection).distinct(anyString());
    }

    private Answer savingExecutionStatsTo(ExecutionStats[] buffer) {
        return invocation -> {
            Query query = createQuery(invocation.getArguments());
            buffer[0] = gatherExecutionStats(query);
            return invocation.callRealMethod();
        };
    }

    private Query createQuery(Object[] arguments) {
        String query = (String) arguments[0];
        Object[] parameters = copyOfRange(arguments, 1, arguments.length);
        return jongo.createQuery(query, parameters);
    }

    private ExecutionStats gatherExecutionStats(Query query) {
        DBCollection dbCollection = jongo.getCollection(collection).getDBCollection();
        DBObject explainResult = dbCollection.find(query.toDBObject()).explain();
        DBObject executionStats = (DBObject) explainResult.get("executionStats");
        BsonDocument document = Bson.createDocument(executionStats);
        Unmarshaller unmarshaller = jongo.getMapper().getUnmarshaller();
        return unmarshaller.unmarshall(document, ExecutionStats.class);
    }

    /**
     * Assert that collection against which a query has been executed
     * was indexed properly, so that no collection scan has been performed,
     * as well as the least possible amount of index keys have been examined
     *
     * @param stats     {@code ExecutionStats} object received from
     *                  {@link QueryExecutionStatisticsITest#explain(BaseRepository, Function)}
     * @param indexName Collection index name
     * @see QueryExecutionStatisticsITest#assertThatQueryUtilizedIndex(ExecutionStats, String)
     */
    protected void assertThatQueryUtilizedIndexOptimally(ExecutionStats stats, String indexName) {
        assertThatQueryUtilizedIndex(stats, indexName);
        assertThat(stats.totalKeysExamined)
                .as("Number of keys examined is the same as the number of records returned")
                .isEqualTo(stats.numReturned);
    }

    /**
     * Assert that collection against which a query has been executed
     * was indexed properly, so that no collection scan has been performed
     *
     * @param stats     {@code ExecutionStats} object received from
     *                  {@link QueryExecutionStatisticsITest#explain(BaseRepository, Function)}
     * @param indexName Collection index name
     */
    protected void assertThatQueryUtilizedIndex(ExecutionStats stats, String indexName) {
        assertThat(stats.success).as("Query has been successfully executed").isEqualTo(true);
        assertThat(stats.numReturned).as("Query returned at least one record").isPositive();

        assertThat(stats.noCollectionScan()).as("No collection scan has been performed").isEqualTo(true);
        assertThat(stats.indexUsed()).as("Index has been used").isEqualTo(true);
        assertThat(stats.indexName).as("Query utilized index '%s'", indexName).isEqualTo(indexName);
    }

    protected enum Stage {
        COLLSCAN, IXSCAN, FETCH
    }

    /**
     * Immutable Java Bean consisting of:
     * <ul>
     * <li>query execution result ({@code T result})</li>
     * <li>query execution statistics ({@link ExecutionStats} {@code stats})</li>
     * </ul>
     *
     * @param <T> type of the query execution result
     */
    protected static class Execution<T> {

        public final T result;
        public final ExecutionStats stats;

        Execution(T result, ExecutionStats stats) {
            this.result = result;
            this.stats = stats;
        }
    }

    /**
     * Immutable Java Bean wrapper around a subset of MongoDB Execution Statistics properties
     *
     * @see <a href="https://docs.mongodb.com/manual/reference/explain-results/#executionstats">
     * MongoDB Manual: Query Optimization > Explain Results > executionStats</a>
     */
    @SuppressWarnings("WeakerAccess")
    protected static class ExecutionStats {

        public final boolean success;
        public final int numReturned;
        public final int executionMillis;
        public final int totalKeysExamined;
        public final int totalDocsExamined;

        public final Stage stage;
        public final Stage inputStage;
        public final String indexName;

        @JsonCreator
        public ExecutionStats(@JsonProperty("executionSuccess") boolean success,
                              @JsonProperty("nReturned") int numReturned,
                              @JsonProperty("executionTimeMillis") int executionMillis,
                              @JsonProperty("totalKeysExamined") int totalKeysExamined,
                              @JsonProperty("totalDocsExamined") int totalDocsExamined,
                              @JsonProperty("executionStages") Map<String, Object> stages) {
            this.success = success;
            this.numReturned = numReturned;
            this.executionMillis = executionMillis;
            this.totalKeysExamined = totalKeysExamined;
            this.totalDocsExamined = totalDocsExamined;

            this.stage = getStage(stages);
            if (stages.containsKey("inputStage")) {
                @SuppressWarnings("unchecked")
                Map<String, Object> inputStage = (Map<String, Object>) stages.get("inputStage");
                this.inputStage = getStage(inputStage);
                this.indexName = getIndexName(inputStage);
            } else {
                this.inputStage = null;
                this.indexName = null;
            }
        }

        private Stage getStage(Map<String, Object> map) {
            return Stage.valueOf((String) map.get("stage"));
        }

        private String getIndexName(Map<String, Object> inputStage) {
            return Stage.IXSCAN.equals(this.inputStage) ? (String) inputStage.get("indexName") : null;
        }

        private boolean noCollectionScan() {
            return !Stage.COLLSCAN.equals(stage);
        }

        private boolean indexUsed() {
            return Stage.FETCH.equals(stage) && Stage.IXSCAN.equals(inputStage);
        }

        @Override
        public String toString() {
            return toStringHelper(this)
                    .omitNullValues()
                    .add("success", success)
                    .add("numReturned", numReturned)
                    .add("executionMillis", executionMillis)
                    .add("totalKeysExamined", totalKeysExamined)
                    .add("totalDocsExamined", totalDocsExamined)
                    .add("stage", stage)
                    .add("inputStage", inputStage)
                    .add("indexName", indexName)
                    .toString();
        }
    }
}
