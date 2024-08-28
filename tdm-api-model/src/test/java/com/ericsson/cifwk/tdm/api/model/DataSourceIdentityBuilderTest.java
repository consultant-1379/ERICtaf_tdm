package com.ericsson.cifwk.tdm.api.model;

import static java.util.Collections.singletonMap;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.DataSourceIdentityBuilder.aDataSourceIdentity;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.truth.Truth.assertThat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

public class DataSourceIdentityBuilderTest {

    private DataSourceIdentity dataSource;

    @Test
    public void build_empty() throws Exception {
        dataSource = aDataSourceIdentity().build();

        assertThat(dataSource.getId()).isNull();
        assertThat(dataSource.getVersion()).isNull();
        assertThat(dataSource.getName()).isNull();
        assertThat(dataSource.getApprovalStatus()).isNull();
        assertThat(dataSource.getReviewers()).isEmpty();
        assertThat(dataSource.getApprover()).isNull();
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
        List<String> reviewers = newArrayList("reviewer");
        Map<String, Object> properties = singletonMap("key", (Object) "value");

        Date createTime = new Date();
        Date updateTime = new Date();

        dataSource = aDataSourceIdentity()
                .withId("id")
                .withVersion("version")
                .withName("name")
                .withApprovalStatus(UNAPPROVED)
                .withReviewers(reviewers)
                .withApprover("username")
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
        assertThat(dataSource.getVersion()).isEqualTo("version");
        assertThat(dataSource.getName()).isEqualTo("name");
        assertThat(dataSource.getApprovalStatus()).isEqualTo(UNAPPROVED);
        assertThat(dataSource.getReviewers()).isEqualTo(reviewers);
        assertThat(dataSource.getApprover()).isEqualTo("username");
        assertThat(dataSource.getGroup()).isEqualTo("group");
        assertThat(dataSource.getTestIdColumnName()).isEqualTo("testIdColumnName");
        assertThat(dataSource.getContext()).isEqualTo("context");
        assertThat(dataSource.getContextId()).isEqualTo("contextId");
        assertThat(dataSource.getProperties()).isEqualTo(singletonMap("key", (Object) "value"));
        assertThat(dataSource.getCreatedBy()).isEqualTo("createdBy");
        assertThat(dataSource.getUpdatedBy()).isEqualTo("updatedBy");
        assertThat(dataSource.getCreateTime()).isSameAs(createTime);
        assertThat(dataSource.getUpdateTime()).isSameAs(updateTime);
    }


    @Test
    public void withReviewer() throws Exception {
        dataSource = aDataSourceIdentity()
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
        dataSource = aDataSourceIdentity()
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
