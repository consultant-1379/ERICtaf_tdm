package com.ericsson.cifwk.tdm.application.common.repository;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.Lists;

public class BaseRepositoryTest {

    private static List<String> REQUIRED_FIELDS = Lists.newArrayList("test");
    private static List<String> VISIBLE_COLUMNS = Lists.newArrayList("test","name","climate",null);
    private static String REQUIRED_COLUMN = "value";

    private BaseRepository<Entity> repository = new BaseRepository<>("entities", Entity.class);

    @Test
    public void joinIntoProjection_HappyPath() {
        String projection = repository.joinIntoProjection(REQUIRED_FIELDS, VISIBLE_COLUMNS, REQUIRED_COLUMN);
        Assertions.assertThat(projection).isEqualTo("{'test': 1,'value.name': 1,'value.climate': 1}");
    }

    @Test
    public void joinIntoProjection_Should_ReturnEmptyArray_When_VisibleColumnsEmpty() {
        String projection = repository.joinIntoProjection(REQUIRED_FIELDS,  Lists.newArrayList(), REQUIRED_COLUMN);
        Assertions.assertThat(projection).isEqualTo("{}");
    }

    private static class Entity {
    }
}
