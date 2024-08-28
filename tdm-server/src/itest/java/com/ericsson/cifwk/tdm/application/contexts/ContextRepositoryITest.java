package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.application.common.repository.QueryExecutionStatisticsITest;
import com.ericsson.cifwk.tdm.model.ContextEntity;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.contexts.ContextRepository.CONTEXTS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

@ContextConfiguration(classes = ContextRepository.class)
public class ContextRepositoryITest extends QueryExecutionStatisticsITest {

    private static final String IDX_SYSTEM_ID = "systemId_1";
    private static final String IDX_PARENT_ID = "parentId_1";
    private static final String IDX_NAME = "name_1";

    @Autowired
    private ContextRepository repository;

    public ContextRepositoryITest() {
        super(CONTEXTS);
    }

    @After
    public void tearDown() throws Exception {
        repository.remove("{}");
    }

    @Test
    public void findBySystemId() throws Exception {
        List<ContextEntity> contexts = newArrayList(
                context("foo", "systemId-1"),
                context("bar", "systemId-2"),
                context("baz", "systemId-3"));
        repository.insert(contexts);

        Execution<ContextEntity> execution = explain(repository, repo -> repo.findBySystemId("systemId-2"));

        assertThat(execution.result.getName()).isEqualTo("bar");
        assertThat(execution.result.getSystemId()).isEqualTo("systemId-2");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_SYSTEM_ID);
    }

    @Test
    public void findByName() throws Exception {
        List<ContextEntity> contexts = newArrayList(
                context("foo", "systemId-1"),
                context("bar", "systemId-2"),
                context("baz", "systemId-3"));
        repository.insert(contexts);

        Execution<ContextEntity> execution = explain(repository, repo -> repo.findByName("bar"));

        assertThat(execution.result.getName()).isEqualTo("bar");
        assertThat(execution.result.getSystemId()).isEqualTo("systemId-2");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_NAME);
    }

    @Test
    public void findByParentIdAndName_sameParentId() throws Exception {
        ContextEntity parent = context("parent", "systemId-0");
        repository.update(parent);
        ContextEntity child1 = context("child1", "systemId-1", parent);
        ContextEntity child2 = context("child2", "systemId-2", parent);
        ContextEntity child3 = context("child3", "systemId-3", parent);
        repository.insert(newArrayList(child1, child2, child3));

        Execution<ContextEntity> execution = explain(repository,
                repo -> repo.findByParentIdAndName(parent.getId(), "child2"));

        assertThat(execution.result.getName()).isEqualTo("child2");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_NAME);
    }

    @Test
    public void findByParentIdAndName_sameName() throws Exception {
        String name = "context";
        ContextEntity context1 = context(name, "systemId-1");
        repository.update(context1);
        ContextEntity context2 = context(name, "systemId-2", context1);
        repository.update(context2);
        ContextEntity context3 = context(name, "systemId-3", context2);
        repository.update(context3);

        Execution<ContextEntity> execution = explain(repository,
                repo -> repo.findByParentIdAndName(context3.getParentId(), name));

        assertThat(execution.result.getSystemId()).isEqualTo("systemId-3");
        assertThatQueryUtilizedIndexOptimally(execution.stats, IDX_PARENT_ID);
    }

    private ContextEntity context(String name, String systemId, ContextEntity parent) {
        ContextEntity entity = context(name, systemId);
        entity.setParentId(parent.getId());
        return entity;
    }

    private ContextEntity context(String name, String systemId) {
        ContextEntity entity = new ContextEntity();
        entity.setName(name);
        entity.setSystemId(systemId);
        return entity;
    }
}