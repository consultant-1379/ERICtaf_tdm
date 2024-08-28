package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.ericsson.cifwk.tdm.model.Version;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder.aDataSourceActionEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class VersioningServiceTest {

    @Spy
    private VersioningService service = new VersioningService();

    @Test
    public void incrementVersion_shouldIncrementVersion_forDataSourceIdentity_andActionEntities() throws Exception {
        Version oldVersion = new Version(1, 0, 0);
        DataSourceIdentityEntity identity = aDataSourceIdentityEntity().withVersion(oldVersion).build();
        DataSourceActionEntity actionEntity1 = aDataSourceActionEntity().withVersion(oldVersion).build();
        DataSourceActionEntity actionEntity2 = aDataSourceActionEntity().withVersion(oldVersion).build();
        DataSourceActionEntity actionEntity3 = aDataSourceActionEntity().withVersion(oldVersion).build();
        List<DataSourceActionEntity> actionEntities = newArrayList(actionEntity1, actionEntity2, actionEntity3);
        Version newVersion = new Version(2, 0, 0);
        doReturn(newVersion).when(service).getVersionIfRequired(any(Version.class), anyListOf(DataSourceActionEntity.class));

        service.incrementVersion(identity, actionEntities);

        assertThat(identity.getVersion()).isEqualTo(newVersion);
        assertThat(actionEntity1.getVersion()).isEqualTo(newVersion);
        assertThat(actionEntity2.getVersion()).isEqualTo(newVersion);
        assertThat(actionEntity3.getVersion()).isEqualTo(newVersion);
        verify(service).getVersionIfRequired(oldVersion, actionEntities);
    }

    @Test
    public void increment_shouldSet_minorVersion_whenNoSpecificVersionChange() throws Exception {
        Version version = new Version(1, 0, 0);
        DataSourceActionEntity actionEntity1 = aDataSourceActionEntity()
                .withType(DataSourceActionType.IDENTITY_APPROVAL_STATUS)
                .build();
        DataSourceActionEntity actionEntity2 = aDataSourceActionEntity()
                .withId("1")
                .withType(DataSourceActionType.IDENTITY_VERSION_EDIT)
                .withVersion(1,1,0)
                .build();

        DataSourceActionEntity actionEntity3 = aDataSourceActionEntity()
                .withType(DataSourceActionType.RECORD_VALUE_EDIT)
                .build();

        Version result = service.getVersionIfRequired(version, newArrayList(actionEntity1, actionEntity2, actionEntity3));

        assertThat(result).isEqualTo(new Version(1, 1, 0));
    }
//
    @Test
    public void increment_shouldKeep_minorVersion_whenNoSpecificVersionChange() throws Exception {
        Version version = new Version(1, 0, 0);
        DataSourceActionEntity actionEntity1 = aDataSourceActionEntity()
                .withType(DataSourceActionType.IDENTITY_APPROVAL_STATUS)
                .build();
        DataSourceActionEntity actionEntity2 = aDataSourceActionEntity()
                .withType(DataSourceActionType.RECORD_VALUE_EDIT)
                .build();

        DataSourceActionEntity actionEntity3 = aDataSourceActionEntity()
                .withType(DataSourceActionType.RECORD_VALUE_EDIT)
                .build();

        Version result = service.getVersionIfRequired(version, newArrayList(actionEntity1, actionEntity2, actionEntity3));

        assertThat(result).isEqualTo(new Version(1, 0, 0));
    }

    @Test
    public void increment_shouldKeep_minorVersion_whenChangeType_null() throws Exception {
        DataSourceActionEntity entity = dataSourceActionEntityWithChangeType(null);
        Version version = new Version(1, 0, 0);
        Version result = service.getVersionIfRequired(version, newArrayList(entity));
        assertThat(result).isEqualTo(new Version(1, 0, 0));
    }

    private DataSourceActionEntity dataSourceActionEntityWithChangeType(DataSourceChangeType changeType) {
        DataSourceActionType type = mock(DataSourceActionType.class);
        doReturn(changeType).when(type).getType();
        return aDataSourceActionEntity().withType(type).build();
    }
}