package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.RecordPredicate;
import org.assertj.core.api.ListAssert;
import org.jongo.Jongo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.tdm.application.datasources.DataRecordRepository.DATA_RECORDS_COLLECTION;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.STAR_WARS_DS_ID;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Lists.newArrayList;

/**
 * All test data has been selected from {@code InitialChangelog} change set "mockData"
 *
 * @see com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog#mockData(Jongo)
 */
@ContextConfiguration(classes = DataRecordRepository.class)
public class DataRecordRepositoryITest extends QueryExecutionStatisticsITest {

    private static final String IDX_DATA_SOURCE = "dataSourceId_1";

    private static final String ALDERAAN_ID = "56c5ddd29759e577fc68ab75";

    private static final String[] planetsNames = new String[]{
            "Alderaan", "Yavin IV", "Hoth", "Dagobah", "Bespin",
            "Endor", "Naboo", "Coruscant", "Kamino", "Geonosis"};
    private static final String[] planetIds = {
            "56c5ddd29759e577fc68ab75", "56c5ddd29759e577fc68ab76", "56c5ddd29759e577fc68ab77",
            "56c5ddd29759e577fc68ab78", "56c5ddd29759e577fc68ab79", "56c5ddd29759e577fc68ab7a",
            "56c5ddd29759e577fc68ab7b", "56c5ddd29759e577fc68ab7c", "56c5ddd29759e577fc68ab7d",
            "56c5ddd29759e577fc68ab7e"};

    @Autowired
    private DataRecordRepository repository;

    public DataRecordRepositoryITest() {
        super(DATA_RECORDS_COLLECTION);
    }

    @Test
    public void find() throws Exception {
        Execution<Map<String, DataRecordEntity>> execution = explain(repository,
                repo -> repo.find(STAR_WARS_DS_ID, emptyList(), emptyList()));

        Map<String, DataRecordEntity> result = execution.result;
        assertThat(result.keySet())
                .containsExactlyInAnyOrder(planetIds);
        assertThat(result.values())
                .extracting(DataRecordEntity::getValues)
                .extracting(map -> map.get("name"))
                .containsExactlyInAnyOrder(planetsNames);
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_DATA_SOURCE);
    }

    @Test
    public void find_predicate() throws Exception {
        RecordPredicate predicate = new RecordPredicate("name", "$eq", "Alderaan");

        Execution<Map<String, DataRecordEntity>> execution = explain(repository,
                repo -> repo.find(STAR_WARS_DS_ID, newArrayList(predicate), emptyList()));

        Map<String, DataRecordEntity> result = execution.result;
        assertThat(result).containsOnlyKeys(ALDERAAN_ID);
        assertThat(result.get(ALDERAAN_ID).getValues()).containsOnly(
                entry("name", "Alderaan"),
                entry("rotation_period", 24),
                entry("orbital_period", 364),
                entry("diameter", 12500),
                entry("climate", "temperate"),
                entry("gravity", "1 standard"),
                entry("terrain", "grasslands, mountains"),
                entry("surface_water", 40),
                entry("population", "2000000000"));
        assertThatQueryUtilizedIndex(execution.stats, IDX_DATA_SOURCE);
    }

    @Test
    public void find_column() throws Exception {
        Execution<Map<String, DataRecordEntity>> execution = explain(repository,
                repo -> repo.find(STAR_WARS_DS_ID, emptyList(), newArrayList("name")));

        Map<String, DataRecordEntity> result = execution.result;
        assertThat(result.keySet()).containsExactlyInAnyOrder(planetIds);
        ListAssert<Map> recordValueMaps = assertThat(result.values()).extracting(DataRecordEntity::getValues);
        recordValueMaps.flatExtracting(Map::keySet).containsOnly("name");
        recordValueMaps.flatExtracting(Map::values).containsExactlyInAnyOrder(planetsNames);
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_DATA_SOURCE);
    }

    @Test
    public void find_predicate_column() throws Exception {
        RecordPredicate predicate = new RecordPredicate("name", "$eq", "Alderaan");

        Execution<Map<String, DataRecordEntity>> execution = explain(repository,
                repo -> repo.find(STAR_WARS_DS_ID, newArrayList(predicate), newArrayList("name")));

        Map<String, DataRecordEntity> result = execution.result;
        assertThat(result).containsOnlyKeys(ALDERAAN_ID);
        assertThat(result.get(ALDERAAN_ID).getValues()).containsOnly(entry("name", "Alderaan"));
        assertThatQueryUtilizedIndex(execution.stats, IDX_DATA_SOURCE);
    }

    @Test
    public void findIds() throws Exception {
        Execution<List<String>> execution = explain(repository, repo -> repo.findIds(STAR_WARS_DS_ID));

        assertThat(execution.result).containsExactly(planetIds);
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_DATA_SOURCE);
    }
}