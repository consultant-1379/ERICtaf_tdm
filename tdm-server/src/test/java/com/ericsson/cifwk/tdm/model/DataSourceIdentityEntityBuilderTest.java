package com.ericsson.cifwk.tdm.model;

import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.ericsson.cifwk.tdm.model.Version.INITIAL_VERSION;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;
import static java.util.Collections.singletonMap;

public class DataSourceIdentityEntityBuilderTest {

    private DataSourceIdentityEntity dataSource;

    @Test
    public void build_empty() throws Exception {
        dataSource = aDataSourceIdentityEntity().build();

        assertThat(dataSource.getId()).isNull();
        assertThat(dataSource.getVersion()).isSameAs(INITIAL_VERSION);
        assertThat(dataSource.getName()).isNull();
        assertThat(dataSource.getApprovalStatus()).isSameAs(UNAPPROVED);
        assertThat(dataSource.getReviewers()).isEmpty();
        assertThat(dataSource.getGroup()).isNull();
        assertThat(dataSource.getTestIdColumnName()).isNull();
        assertThat(dataSource.getContext()).isNull();
        assertThat(dataSource.getContextId()).isNull();

        assertThat(dataSource.getProperties()).isEmpty();

        assertThat(dataSource.getCreatedBy()).isNull();
        assertThat(dataSource.getUpdatedBy()).isNull();
        assertThat(dataSource.getCreateTime()).isNull();
        assertThat(dataSource.getUpdateTime()).isNull();
    }

    @Test
    public void build_full() throws Exception {
        Version version = new Version(4, 2, 0);
        List<String> reviewers = newArrayList("reviewer");
        Map<String, Object> properties = singletonMap("key", (Object) "value");

        Date createTime = new Date();
        Date updateTime = new Date();

        dataSource = aDataSourceIdentityEntity()
                .withId("id")
                .withVersion(version)
                .withName("name")
                .withApprovalStatus(PENDING)
                .withReviewers(reviewers)
                .withGroup("group")
                .withTestIdColumnName("testIdColumnName")
                .withContext("context")
                .withContextId("contextId")

                .withProperties(properties)

                .withCreatedBy("createdBy")
                .withUpdatedBy("updatedBy")
                .withCreateTime(createTime)
                .withUpdateTime(updateTime)
                .build();

        assertThat(dataSource.getId()).isEqualTo("id");
        assertThat(dataSource.getVersion()).isSameAs(version);
        assertThat(dataSource.getName()).isEqualTo("name");
        assertThat(dataSource.getApprovalStatus()).isSameAs(PENDING);
        assertThat(dataSource.getReviewers()).isSameAs(reviewers);
        assertThat(dataSource.getGroup()).isEqualTo("group");
        assertThat(dataSource.getTestIdColumnName()).isEqualTo("testIdColumnName");
        assertThat(dataSource.getContext()).isEqualTo("context");
        assertThat(dataSource.getContextId()).isEqualTo("contextId");

        assertThat(dataSource.getProperties()).isSameAs(properties);

        assertThat(dataSource.getCreatedBy()).isEqualTo("createdBy");
        assertThat(dataSource.getUpdatedBy()).isEqualTo("updatedBy");
        assertThat(dataSource.getCreateTime()).isEqualTo(createTime);
        assertThat(dataSource.getUpdateTime()).isEqualTo(updateTime);
    }

    @Test
    public void withInitialVersion() throws Exception {
        dataSource = aDataSourceIdentityEntity()
                .withInitialVersion()
                .build();

        assertThat(dataSource.getVersion()).isSameAs(INITIAL_VERSION);
    }

    @Test
    public void withVersion() throws Exception {
        dataSource = aDataSourceIdentityEntity()
                .withVersion(4, 2, 0)
                .build();

        assertThat(dataSource.getVersion()).isEqualTo(new Version(4, 2, 0));
    }

    @Test
    public void withReviewer() throws Exception {
        dataSource = aDataSourceIdentityEntity()
                .withReviewer("reviewer1")
                .withReviewer("reviewer2")
                .withReviewer("reviewer3")
                .build();

        assertThat(dataSource.getReviewers()).containsExactly(
                "reviewer1", "reviewer2", "reviewer3"
        ).inOrder();
    }

    @Test
    public void withProperty() throws Exception {
        dataSource = aDataSourceIdentityEntity()
                .withProperty("key1", "value1")
                .withProperty("key2", "value2")
                .withProperty("key3", "value3")
                .build();

        assertThat(dataSource.getProperties()).containsExactly(
                "key1", "value1",
                "key2", "value2",
                "key3", "value3"
        ).inOrder();
    }
}