package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.model.DataRecordEntity;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import org.junit.Before;
import org.junit.Test;
import java.util.SortedMap;
import java.util.TreeMap;

import java.util.Map;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType.IDENTITY_APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.approvalStatus;
import static com.ericsson.cifwk.tdm.model.DataSourceActionEntityBuilder.aDataSourceActionEntity;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.COMMENT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEWERS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Maps.newTreeMap;
import static com.google.common.truth.Truth.assertThat;

public class DataSourceActionTypeTest {

    private DataSourceIdentityEntity dataSource;
    private SortedMap<String, DataRecordEntity> dataRecords;
    private DataSourceActionEntity action;
    private AppliedDataSourceActionAccumulator accumulator;

    @Before
    public void setUp() throws Exception {
        dataSource = aDataSourceIdentityEntity().build();
        dataRecords = newTreeMap();
        action = aDataSourceActionEntity().build();
        accumulator = new AppliedDataSourceActionAccumulator();
    }

    @Test
    public void apply_IDENTITY_APPROVAL_STATUS_shouldUpdateDataSourceApprovalData() throws Exception {
        dataSource.setApprovalStatus(UNAPPROVED);
        dataSource.setReviewers(newArrayList());
        dataSource.setComment(null);

        SortedMap<String, Object> values = ImmutableSortedMap.of(
                APPROVAL_STATUS, PENDING.name(),
                REVIEWERS, newArrayList("user1", "user2"),
                COMMENT, "comment"
        );
        action = approvalStatus(dataSource, values);

        IDENTITY_APPROVAL_STATUS.apply(dataSource, dataRecords, action, accumulator);

        assertThat(dataSource.getApprovalStatus()).isEqualTo(PENDING);
        assertThat(dataSource.getReviewers()).containsExactly("user1", "user2");
        assertThat(dataSource.getComment()).isEqualTo("comment");
    }
}