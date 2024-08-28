package com.ericsson.cifwk.tdm.application.common.repository;

import static java.util.regex.Pattern.compile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;

import java.util.List;
import java.util.stream.Stream;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.jackson.oid.MongoId;
import org.jongo.marshall.jackson.oid.MongoObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import com.ericsson.cifwk.tdm.infrastructure.ITestDbConfiguration;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@RunWith(SpringRunner.class)
@ActiveProfiles(INTEGRATION_TEST)
@ContextConfiguration(classes = ITestDbConfiguration.class,
        initializers = ConfigFileApplicationContextInitializer.class)
public class BaseRepositoryITest {

    @Autowired
    private Jongo jongo;

    private BaseRepository<Entity> repository = new BaseRepository<>("entities", Entity.class);
    private MongoCollection collection;

    @Before
    public void setUp() throws Exception {
        ReflectionTestUtils.setField(repository, "jongo", jongo);
        collection = repository.getCollection();
    }

    @After
    public void tearDown() throws Exception {
        collection.drop();
    }

    @Test
    public void findById() throws Exception {
        Entity foo = new Entity("foo");
        collection.save(foo);
        Entity result = repository.findById(foo.getId());

        assertThat(result.getName()).isEqualTo("foo");
    }

    @Test
    public void findOne() throws Exception {
        insertEntities("foo", "bar", "baz");

        Entity result = repository.findOne("{name:#}", "foo");

        assertThat(result.getName()).isEqualTo("foo");
    }

    @Test
    public void findOne_notExists() throws Exception {
        Entity result = repository.findOne("{name:#}", "notExists");

        assertThat(result).isNull();
    }

    @Test
    public void findAll() throws Exception {
        insertEntities("foo", "bar", "baz");

        List<Entity> result = repository.findAll();

        assertThat(result)
                .extracting(Entity::getName)
                .containsExactly("foo", "bar", "baz");
    }

    @Test
    public void findAll_empty() throws Exception {
        List<Entity> result = repository.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    public void find() throws Exception {
        insertEntities("foo", "bar", "baz");

        List<Entity> result = repository.find("{name:#}", compile("^b.*"));

        assertThat(result)
                .extracting(Entity::getName)
                .containsExactly("bar", "baz");
    }

    @Test
    public void update_new() throws Exception {
        Entity entity = new Entity("foo");

        boolean result = repository.update(entity);

        assertThat(result).isFalse();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    public void update_existing() throws Exception {
        Entity entity = new Entity("foo");
        repository.update(entity);

        boolean result = repository.update(entity);

        assertThat(result).isTrue();
        assertThat(entity.getId()).isNotNull();
    }

    @Test
    public void insert_one() throws Exception {
        Entity entity = new Entity("foo");

        repository.insert(entity);

        Entity result = repository.findOne("{name:#}", entity.getName());
        assertThat(result.getId()).isNotNull();
    }

    @Test
    public void insert_many() throws Exception {
        List<Entity> entities = newArrayList(new Entity("foo"), new Entity("bar"), new Entity("baz"));

        repository.insert(entities);

        List<Entity> result = repository.findAll();
        assertThat(result)
                .extracting(Entity::getId)
                .hasSize(entities.size())
                .doesNotContainNull();
    }

    @Test
    public void removeById() throws Exception {
        Entity entity = new Entity("foo");
        repository.update(entity);

        repository.removeById(entity.getId());

        Entity result = repository.findById(entity.getId());
        assertThat(result).isNull();
    }

    @Test
    public void remove() throws Exception {
        insertEntities("foo", "bar", "baz");

        repository.remove("{name:#}", compile("^b.*"));

        List<Entity> result = repository.findAll();
        assertThat(result)
                .extracting(Entity::getName)
                .containsExactly("foo");
    }

    private void insertEntities(String... names) {
        Stream.of(names).map(Entity::new).forEach(collection::insert);
    }

    private static class Entity {

        @MongoId
        @MongoObjectId
        private String id;
        private String name;

        @JsonCreator
        @SuppressWarnings("unused")
        Entity(@MongoId String id, @JsonProperty("name") String name) {
            this.id = id;
            this.name = name;
        }

        public Entity(String name) {
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }
    }
}
