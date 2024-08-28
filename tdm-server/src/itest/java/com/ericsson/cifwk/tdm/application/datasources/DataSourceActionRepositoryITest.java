package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.Version;
import org.jongo.Jongo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionRepository.DATA_SOURCE_ACTIONS;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_INITIAL_CREATE;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.STAR_WARS_DS_ID;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

/**
 * All test data has been selected from {@code InitialChangelog} change set "mockData"
 *
 * @see com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog#mockData(Jongo)
 */
@ContextConfiguration(classes = DataSourceActionRepository.class)
public class DataSourceActionRepositoryITest extends QueryExecutionStatisticsITest {

    private static final String IDX_PARENT_ID_TYPE = "parentId_1_type_1";

    @Autowired
    private DataSourceActionRepository repository;

    public DataSourceActionRepositoryITest() {
        super(DATA_SOURCE_ACTIONS);
    }

    @Test
    public void find_dataSource() throws Exception {
        Execution<List<DataSourceActionEntity>> execution = explain(repository,
                repo -> repo.find(newArrayList(STAR_WARS_DS_ID), "0.0.1"));

        assertThat(execution.result)
                .extracting(DataSourceActionEntity::getType)
                .containsExactly(IDENTITY_INITIAL_CREATE, IDENTITY_APPROVAL_STATUS);
        assertThatQueryUtilizedIndex(execution.stats, IDX_PARENT_ID_TYPE);
    }

    @Test
    public void find_dataRecords() throws Exception {
        Execution<List<DataSourceActionEntity>> execution = explain(repository,
                repo -> repo.find(newArrayList(
                        "56c5ddd29759e577fc68ab75",
                        "56c5ddd29759e577fc68ab76",
                        "56c5ddd29759e577fc68ab77",
                        "56c5ddd29759e577fc68ab78",
                        "56c5ddd29759e577fc68ab79"), "0.0.1"));

        assertThat(execution.result)
                .extracting(DataSourceActionEntity::getValues)
                .extracting(map -> map.get("name"))
                .containsExactlyInAnyOrder("Alderaan", "Yavin IV", "Hoth", "Dagobah", "Bespin");
        assertThatQueryUtilizedIndex(execution.stats, IDX_PARENT_ID_TYPE);
        assertThat(execution.stats.totalDocsExamined).isEqualTo(execution.stats.numReturned);
    }

    @Test
    public void numberOfActions() throws Exception {
        Execution<Integer> execution = explain(repository,
                repo -> repo.numberOfActions(STAR_WARS_DS_ID, new Version(0, 0, 1)));

        assertThat(execution.result).isEqualTo(2);
        assertThatQueryUtilizedIndex(execution.stats, IDX_PARENT_ID_TYPE);
    }

    @Test
    public void findVersions_dataSource() throws Exception {
        Execution<List<Version>> execution = explain(repository,
                repo -> repo.findVersions(newHashSet(STAR_WARS_DS_ID)));

        assertThat(execution.result)
                .extracting(Version::toString)
                .containsExactlyInAnyOrder("0.0.1-SNAPSHOT", "0.0.2", "0.0.3-SNAPSHOT", "0.0.3", "0.0.4-SNAPSHOT");
        assertThat(execution.stats).isNotNull();
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_PARENT_ID_TYPE);
    }

}
