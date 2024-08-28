package com.ericsson.cifwk.tdm.model;

import org.junit.Test;

import java.util.Map;
import java.util.SortedMap;

import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.approvalStatus;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.truth.Truth.assertThat;

public class DataSourceActionEntityTest {

    @Test
    public void approvalStatus_shouldCreateAction_withType_IDENTITY_APPROVAL_STATUS() throws Exception {
        DataSourceIdentityEntity dataSource = aDataSourceIdentityEntity()
                .withId("dataSourceId")
                .withInitialVersion()
                .build();
        SortedMap<String, Object> values = newTreeMap();

        DataSourceActionEntity action = approvalStatus(dataSource, values);

        assertThat(action.getParentId()).isEqualTo(dataSource.getId());
        assertThat(action.getType()).isEqualTo(IDENTITY_APPROVAL_STATUS);
        assertThat(action.getValues()).containsExactlyEntriesIn(values);
        assertThat(action.getVersion()).isSameAs(dataSource.getVersion());
    }
}