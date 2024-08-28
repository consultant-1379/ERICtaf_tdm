package com.ericsson.cifwk.tdm.application.email;

import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.CANCELLED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.application.email.MailMessageBean.MailMessageBuilder.aMailMessageBean;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_APPROVED_BODY_EN;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_CANCELLED_BODY_EN;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_CHANGE_SUBJ_EN;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_PENDING_BODY_EN;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_REJECTED_BODY_EN;
import static com.ericsson.cifwk.tdm.application.notification.NotificationService.DS_URL_TEMPLATE;
import static com.ericsson.cifwk.tdm.application.util.DataParser.readTextFile;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(INTEGRATION_TEST)
public class EmailServiceITest {

    private static final String RECIPIENT1 = "taftest1@ericsson.com";
    private static final String RECIPIENT2 = "taftest2@ericsson.com";
    private static final String RECIPIENT3 = "taftest3@ericsson.com";
    private static final String CC_RECIPIENT = "requester@ericsson.com";

    @Autowired
    private EmailService emailService;

    @MockBean
    private JavaMailSender mailSender;

    @MockBean
    private TceContextRepository contextClient;

    private Set<String> recipients;
    private Map<String, Object> bodyParams;
    private Map<String, Object> subjParams;

    private String subjectMsgResult;
    private String approvedMsgResult;
    private String pendingMsgResult;
    private String rejectedMsgResult;
    private String cancelledMsgResult;

    @Before
    public void setUp() throws Exception {
        recipients = newHashSet(RECIPIENT1, RECIPIENT2, RECIPIENT3);

        subjectMsgResult = readResource("notify_ds_change_subj_en.msg");
        approvedMsgResult = readResource("notify_ds_approved_body_en.msg");
        pendingMsgResult = readResource("notify_ds_pending_body_en.msg");
        rejectedMsgResult = readResource("notify_ds_rejected_body_en.msg");
        cancelledMsgResult = readResource("notify_ds_cancelled_body_en.msg");

        subjParams = ImmutableMap.of("ds_name", "NAM data source", "ds_version", 12);
        bodyParams = newHashMap();
        bodyParams.put("requestedBy", "Adrian");
        bodyParams.put("reviewers", "Adam / Ellis");
        bodyParams.put("context", "SON");
        bodyParams.put("url", String.format(DS_URL_TEMPLATE, "localhost", "SON", "ds"));
        bodyParams.put("createdBy", "John");
        bodyParams.put("updatedBy", "Karl");
        bodyParams.putAll(subjParams);
    }

    private String readResource(String resourceName) throws IOException {
        return readTextFile("messages/" + resourceName);
    }

    @Test
    public void verify_message_received_for_review() throws Exception {
        bodyParams.put("status", PENDING.name());

        MailMessageBean msg = aMailMessageBean()
            .withTo(recipients)
            .withCc(CC_RECIPIENT)
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .withSubjectParams(subjParams)
            .withBodyTemplate(NOTIFY_DS_PENDING_BODY_EN)
            .withBodyParams(bodyParams)
            .build();
        verifyMessageParts(msg, pendingMsgResult);
    }

    @Test
    public void verify_cancelled_message_received() throws Exception {
        bodyParams.put("status", CANCELLED.name());

        MailMessageBean msg = aMailMessageBean()
                .withTo(recipients)
                .withCc(CC_RECIPIENT)
                .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
                .withSubjectParams(subjParams)
                .withBodyTemplate(NOTIFY_DS_CANCELLED_BODY_EN)
                .withBodyParams(bodyParams)
                .build();
        verifyMessageParts(msg, cancelledMsgResult);
    }

    @Test
    public void verify_approved_message_received() throws Exception {
        bodyParams.put("status", APPROVED.name());

        MailMessageBean msg = aMailMessageBean()
            .withTo(recipients)
            .withCc(CC_RECIPIENT)
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .withSubjectParams(subjParams)
            .withBodyTemplate(NOTIFY_DS_APPROVED_BODY_EN)
            .withBodyParams(bodyParams)
            .build();
        verifyMessageParts(msg, approvedMsgResult);
    }

    @Test
    public void verify_rejected_message_received() throws Exception {
        bodyParams.put("status", REJECTED.name());

        MailMessageBean msg = aMailMessageBean()
            .withTo(recipients)
            .withCc(CC_RECIPIENT)
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .withSubjectParams(subjParams)
            .withBodyTemplate(NOTIFY_DS_REJECTED_BODY_EN)
            .withBodyParams(bodyParams)
            .build();
        verifyMessageParts(msg, rejectedMsgResult);
    }

    private void verifyMessageParts(MailMessageBean msg, String expectedBody) {
        emailService.sendSync(msg);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());

        assertThat(captor.getValue()).isNotNull();
        assertThat(captor.getValue().getTo())
            .as("Receivers")
            .contains(RECIPIENT1, RECIPIENT2, RECIPIENT3);
        assertThat(captor.getValue().getCc())
                .as("CC").containsExactly(CC_RECIPIENT);
        assertThat(captor.getValue().getBcc())
            .as("BCC")
            .isNullOrEmpty();
        assertThat(captor.getValue().getBcc())
            .as("CC")
            .isNullOrEmpty();
        assertThat(captor.getValue().getSubject())
            .as("Subject")
            .isEqualTo(subjectMsgResult);
        assertThat(captor.getValue().getText())
            .as("Body")
            .isEqualTo(expectedBody);
    }
}
