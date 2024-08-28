package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import org.jongo.Jongo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.datasources.DataSourceRepository.DATA_SOURCES_COLLECTION;
import static com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog.STAR_WARS_DS_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

/**
 * All test data has been selected from {@code InitialChangelog} change set "mockData"
 *
 * @see com.ericsson.cifwk.tdm.infrastructure.changelogs.InitialChangelog#mockData(Jongo)
 */
@ContextConfiguration(classes = DataSourceRepository.class)
public class DataSourceRepositoryITest extends QueryExecutionStatisticsITest {

    private static final String IDX_CONTEXT_ID_NAME = "contextId_1_name_1";
    private static final String IDX_DELETED = "deleted_1";

    @Autowired
    private DataSourceRepository repository;

    public DataSourceRepositoryITest() {
        super(DATA_SOURCES_COLLECTION);
    }

    @Test
    public void findById() throws Exception {
        Execution<DataSourceIdentityEntity> execution =
                explain(repository, repo -> repo.findById(STAR_WARS_DS_ID));

        assertThat(execution.result.getName()).isEqualTo("Star wars planets");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_ID);
    }

    @Test
    public void findAll() throws Exception {
        Execution<List<DataSourceIdentityEntity>> execution = explain(repository, DataSourceRepository::findAll);

        assertThat(execution.result).hasSize(11);
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_DELETED);
    }

    @Test
    public void findByContextIds() throws Exception {
        Execution<List<DataSourceIdentityEntity>> execution = explain(repository,
                repo -> repo.findByContextIds(newArrayList("da608d9f-905d-45a2-bdba-bb187aa94271")));

        assertThat(execution.result)
                .extracting(DataSourceIdentityEntity::getName)
                .containsExactly("Star wars planets");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_CONTEXT_ID_NAME);
    }

    @Test
    public void findByContextIdAndName() throws Exception {
        Execution<DataSourceIdentityEntity> execution = explain(repository,
                repo -> repo.findByContextIdAndName("b9192db0-e487-4dc0-bc05-712da4b53345", "DS1"));

        assertThat(execution.result)
                .extracting(DataSourceIdentityEntity::getId)
                .containsExactly("66c5fdd29759e577fc685b73");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_CONTEXT_ID_NAME);
    }
}
