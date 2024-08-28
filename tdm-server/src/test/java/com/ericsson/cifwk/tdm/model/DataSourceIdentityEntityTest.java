package com.ericsson.cifwk.tdm.model;

import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.infrastructure.mapping.MapperFacadeProvider;
import ma.glasnost.orika.MapperFacade;
import org.jongo.Mapper;
import org.jongo.bson.BsonDocument;
import org.jongo.marshall.jackson.JacksonMapper;
import org.junit.Before;
import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.DataSourceIdentityBuilder.aDataSourceIdentity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 31/03/2016
 */
public class DataSourceIdentityEntityTest {

    private MapperFacade mapperFacade;

    @Before
    public void setUp() {
        mapperFacade = new MapperFacadeProvider().mapperFacade();
    }

    @Test
    public void orika_shouldMap_dataSourceIdentityEntity_toDataSourceIdentity() {
        DataSourceIdentityEntity dataSourceIdentityEntity = aDataSourceIdentityEntity()
                .withId("id-1")
                .withGroup("com.group.id")
                .withName("name-1")
                .withApprovalStatus(PENDING)
                .withVersion(1, 0, 0)
                .withTestIdColumnName("col-1")
                .withProperty("prop-1", "value-1")
                .withProperty("prop-2", 2)
                .build();

        DataSourceIdentity dataSourceIdentity = mapperFacade.map(dataSourceIdentityEntity, DataSourceIdentity.class);

        assertThat(dataSourceIdentity.getId()).isEqualTo("id-1");
        assertThat(dataSourceIdentity.getGroup()).isEqualTo("com.group.id");
        assertThat(dataSourceIdentity.getName()).isEqualTo("name-1");
        assertThat(dataSourceIdentity.getApprovalStatus()).isEqualTo(PENDING);
        assertThat(dataSourceIdentity.getVersion()).isEqualTo("1.0.0-SNAPSHOT");
        assertThat(dataSourceIdentity.getTestIdColumnName()).isEqualTo("col-1");

        assertThat(dataSourceIdentity.getProperties()).containsEntry("prop-1", "value-1");
        assertThat(dataSourceIdentity.getProperties()).containsEntry("prop-2", 2);
    }

    @Test
    public void orika_shouldMap_dataSourceIdentity_toDataSourceIdentityEntity() {
        DataSourceIdentity dataSourceIdentity = aDataSourceIdentity()
                .withId("id-1")
                .withGroup("com.group.id")
                .withName("name-1")
                .withApprovalStatus(PENDING)
                .withVersion("1.0.0-SNAPSHOT")
                .withTestIdColumnName("col-1")
                .withProperty("prop-1", "value-1")
                .withProperty("prop-2", 2)
                .build();

        DataSourceIdentityEntity dataSourceIdentityEntity = mapperFacade.map(dataSourceIdentity, DataSourceIdentityEntity.class);

        assertThat(dataSourceIdentityEntity.getId()).isEqualTo("id-1");
        assertThat(dataSourceIdentityEntity.getGroup()).isEqualTo("com.group.id");
        assertThat(dataSourceIdentityEntity.getName()).isEqualTo("name-1");
        assertThat(dataSourceIdentityEntity.getApprovalStatus()).isEqualTo(PENDING);
        assertThat(dataSourceIdentityEntity.getVersion()).isEqualTo(new Version(1, 0, 0));
        assertThat(dataSourceIdentityEntity.getTestIdColumnName()).isEqualTo("col-1");

        assertThat(dataSourceIdentityEntity.getProperties()).containsEntry("prop-1", "value-1");
        assertThat(dataSourceIdentityEntity.getProperties()).containsEntry("prop-2", 2);
    }

    @Test
    public void jongo_shouldMarshall_andUnMarshall_dataSourceIdentityEntity() throws Exception {
        DataSourceIdentityEntity dataSourceIdentityEntity = aDataSourceIdentityEntity()
                .withGroup("com.group.id")
                .withName("name-1")
                .withApprovalStatus(PENDING)
                .withVersion(1, 0, 0)
                .withTestIdColumnName("col-1")
                .withProperty("prop-1", "value-1")
                .withProperty("prop-2", 2)
                .build();

        Mapper mapper = new JacksonMapper.Builder().build();
        BsonDocument bsonDocument = mapper.getMarshaller().marshall(dataSourceIdentityEntity);
        DataSourceIdentityEntity result = mapper.getUnmarshaller().unmarshall(bsonDocument, DataSourceIdentityEntity.class);

        assertThat(result.getGroup()).isEqualTo(dataSourceIdentityEntity.getGroup());
        assertThat(result.getName()).isEqualTo(dataSourceIdentityEntity.getName());
        assertThat(result.getApprovalStatus()).isEqualTo(dataSourceIdentityEntity.getApprovalStatus());
        assertThat(result.getVersion()).isEqualTo(dataSourceIdentityEntity.getVersion());
        assertThat(result.getTestIdColumnName()).isEqualTo(dataSourceIdentityEntity.getTestIdColumnName());
        assertThat(result.getProperties()).isEqualTo(dataSourceIdentityEntity.getProperties());
    }
}
