package com.ericsson.cifwk.tdm.application.email;

import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.application.email.MailMessageBean.MailMessageBuilder.aMailMessageBean;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_APPROVED_BODY_EN;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_CHANGE_SUBJ_EN;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.util.Maps.newHashMap;

public class MailMessageBeanTest {

    @Test
    public void build_empty() {
        assertThatThrownBy(() -> aMailMessageBean().build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("At least one receiver email must be provided");
    }

    @Test
    public void build_with_empty_subject() {
        assertThatThrownBy(() -> aMailMessageBean().withTo(newHashSet("robot@ericsson.se")).build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Mail subject template must be defined");
    }

    @Test
    public void build_with_empty_body() {
        assertThatThrownBy(() -> aMailMessageBean()
            .withTo(newHashSet("robot@ericsson.se"))
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .build())
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Mail body template must be defined");
    }

    @Test
    public void build_full() {
        MailMessageBean msg = aMailMessageBean()
            .withTo(newHashSet("robot@ericsson.se"))
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .withSubjectParams(newHashMap("version", 12))
            .withSubjectParam("name", "NAM data source")
            .withBodyTemplate(NOTIFY_DS_APPROVED_BODY_EN)
            .withBodyParams(newHashMap("status", APPROVED.name()))
            .withBodyParam("createdBy", "John")
            .build();

        assertThat(msg)
            .isNotNull();
        assertThat(msg.getTo()).contains("robot@ericsson.se");

        assertThat(msg.getSubjTemplate())
            .isEqualTo(NOTIFY_DS_CHANGE_SUBJ_EN);
        assertThat(msg.getSubjectParams())
            .containsOnly(
                entry("version", 12),
                entry("name", "NAM data source"));

        assertThat(msg.getBodyTemplate())
            .isEqualTo(NOTIFY_DS_APPROVED_BODY_EN);
        assertThat(msg.getBodyParams())
            .containsOnly(
                entry("status", APPROVED.name()),
                entry("createdBy", "John"));
    }
}
