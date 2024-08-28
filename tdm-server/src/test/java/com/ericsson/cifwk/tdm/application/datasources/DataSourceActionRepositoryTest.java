package com.ericsson.cifwk.tdm.application.datasources;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;

public class DataSourceActionRepositoryTest {

    private static List<String> VISIBLE_COLUMNS = Lists.newArrayList("test",null);

    @Test
    public void buildVisibleColumnsProjectionTest_HappyPath() {
        DataSourceActionRepository repository = new DataSourceActionRepository();
        String buildVisibleColumnsProjection = repository.buildVisibleColumnsProjection(VISIBLE_COLUMNS);
        Assertions.assertThat(buildVisibleColumnsProjection).isEqualTo("{'id': 1,'type': 1,'parentId': 1,'values.test': 1}");
    }

    @Test
    public void buildVisibleColumnsProjectionTest_ShouldReturnEmptyString() {
        DataSourceActionRepository repository = new DataSourceActionRepository();
        String buildVisibleColumnsProjection = repository.buildVisibleColumnsProjection(Lists.newArrayList());
        Assertions.assertThat(buildVisibleColumnsProjection).isEqualTo("");
    }
}
