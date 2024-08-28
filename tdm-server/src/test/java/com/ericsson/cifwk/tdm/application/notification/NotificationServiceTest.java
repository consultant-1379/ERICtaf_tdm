package com.ericsson.cifwk.tdm.application.notification;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.EmailService;
import com.ericsson.cifwk.tdm.application.email.MailMessageBean;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_DELETED_BODY_EN;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_APPROVED_BODY_EN;
import static com.ericsson.cifwk.tdm.model.Version.INITIAL_VERSION;
import static com.google.common.collect.Lists.newArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class NotificationServiceTest {

    @InjectMocks
    private NotificationService service;

    @Mock
    private EmailService emailService;

    @Mock
    private UserService userService;

    private DataSourceIdentityEntity dataSource;

    private List<User> reviewerList;

    private ApprovalRequest request;

    @Mock
    private SecurityService securityService;

    @Mock
    private MessageInputFactory messageInputFactory;

    private User user;

    private User requester;

    private Authentication auth;

    @Before
    public void setUp() {
        doNothing().when(emailService).sendAsync(any());
        ReflectionTestUtils.setField(service, "socket", "localhost:8080");
        user = new User();
        user.setEmail("no-reply@ericsson.se");
        user.setFirstName("Matilda");
        user.setUsername("manager");
        user.setLastName("Eliason");
        requester = new User();
        requester.setEmail("i.am.the.walrus@ericsson.com");
        requester.setUsername("Zeus");
        doReturn(newArrayList(user)).when(userService).findUsers(newArrayList(user.getUsername()));

        auth = new AnonymousAuthenticationToken(
                "key", "Zeus", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));

        AuthenticationStatus authenticationStatus =
                new AuthenticationStatus(auth.getName(), true, newArrayList(),false);

        doReturn(authenticationStatus).when(securityService).getCurrentUser();
        doReturn(requester).when(userService).findByUsername(authenticationStatus.getUsername());
        doReturn(newArrayList(user)).when(userService).findUsers(anyListOf(String.class));
        doReturn(newArrayList(user)).when(securityService).findUser("manager");
        reviewerList = newArrayList(user);

        request = new ApprovalRequest();
        request.setReviewers(reviewerList);

        dataSource = new DataSourceIdentityEntity();
        dataSource.setName("NAM data source");
        dataSource.setVersion(INITIAL_VERSION);
        dataSource.setCreatedBy("manager");
        dataSource.setUpdatedBy("Charles");
        dataSource.setContext("NAM TMS");
        dataSource.setId("system-1");
        dataSource.setReviewRequester(requester.getUsername());
    }

    @Test
    public void verify_pending_data_source_notified() {
        doReturn(new PendingMessageInput(request, securityService)).when(messageInputFactory).getMessageInput
                (dataSource, request);
        assertStatusChangeNotification(PENDING, 1);
    }

    @Test
    public void verify_approved_data_source_notified() {
        doReturn(new ApprovedMessageInput(dataSource, userService)).when(messageInputFactory).getMessageInput
                (dataSource, request);
        request.setReviewers(null); // no reviewers
        dataSource.setReviewers(newArrayList(user.getUsername()));
        assertStatusChangeNotification(APPROVED, 1);
    }
    @Test
    public void verify_deletion_notified(){
        doReturn(new DeletedMessageInput(dataSource, securityService)).when(messageInputFactory).getMessageInput
                (dataSource,NotificationType.DELETION );
        assertOtherNotification(NotificationType.DELETION,1);
    }
    @Test
    public void verify_rejected_data_source_notified() {
        doReturn(new RejectedMessageInput(dataSource, userService)).when(messageInputFactory).getMessageInput
                (dataSource, request);
        request.setReviewers(null); // no reviewers
        dataSource.setReviewers(newArrayList(user.getUsername()));
        assertStatusChangeNotification(REJECTED, 1);
    }

    @Test
    public void verify_default_data_source_skip_notification() {
        assertStatusChangeNotification(UNAPPROVED, 0);
    }

    @Test
    public void verify_prepared_message() {
        MessageInput messageInput = new ApprovedMessageInput(dataSource, userService);
        ApprovalRequest request = new ApprovalRequest();
        String comment = "I am a comment!";
        request.setHostname("localhost:8080");
        request.setComment(comment);

        MailMessageBean msg = service.prepareMessage(messageInput, dataSource, request);
        assertThat(msg).isNotNull();
        assertThat(msg.getTo())
                .containsExactly(user.getEmail());
        assertThat(msg.getCc())
                .matches(requester.getEmail());
        assertThat(msg.getBodyTemplate())
                .isEqualTo(NOTIFY_DS_APPROVED_BODY_EN);
        assertThat(msg.getBodyParams())
            .containsOnly(
                entry("updatedBy", dataSource.getUpdatedBy()),
                entry("createdBy", dataSource.getCreatedBy()),
                entry("context", dataSource.getContext()),
                entry("context", dataSource.getContext()),
                entry("ds_name", dataSource.getName()),
                entry("ds_version", dataSource.getVersion()),
                entry("reviewers", user.getFullname()),
                entry("requestedBy", requester.getUsername()),
                entry("comment", comment),
                entry("url", "http://localhost:8080/#/contexts/NAM%20TMS/datasources/system-1")
            );
    }
    @Test
    public void verify_prepared_message_from_deletion() {
        MessageInput messageInput = new DeletedMessageInput(dataSource,securityService);
        ApprovalRequest request = new ApprovalRequest();
        request.setHostname("localhost:8080");

        MailMessageBean msg = service.prepareMessage(messageInput, dataSource);
        assertThat(msg).isNotNull();
        assertThat(msg.getTo())
                .containsExactly(user.getEmail());
        assertThat(msg.getCc())
                .matches(requester.getEmail());
        assertThat(msg.getBodyTemplate())
                .isEqualTo(NOTIFY_DS_DELETED_BODY_EN);
        assertThat(msg.getBodyParams())
            .containsOnly(
                entry("updatedBy", dataSource.getUpdatedBy()),
                entry("createdBy", dataSource.getCreatedBy()),
                entry("context", dataSource.getContext()),
                entry("context", dataSource.getContext()),
                entry("ds_name", dataSource.getName()),
                entry("ds_version", dataSource.getVersion()),
                entry("requestedBy", requester.getUsername())
            );
    }
    @Test
    public void verify_prepared_message_with_no_address() {
        MessageInput messageInput = new ApprovedMessageInput(dataSource, userService);
        ApprovalRequest request = new ApprovalRequest();
        request.setHostname(null);
        MailMessageBean msg = service.prepareMessage(messageInput, dataSource, request);
        assertThat(msg).isNotNull();
        assertThat(msg.getTo())
                .containsExactly(user.getEmail());
        assertThat(msg.getCc())
                .matches(requester.getEmail());
        assertThat(msg.getBodyTemplate())
                .isEqualTo(NOTIFY_DS_APPROVED_BODY_EN);
        assertThat(msg.getBodyParams())
                .contains(
                        entry("url", "http://localhost:8080/#/contexts/NAM%20TMS/datasources/system-1")
                );
    }

    private void assertStatusChangeNotification(ApprovalStatus status, int times) {
        request.setStatus(status);
        service.notifyDataSourceChange(dataSource, request);
        verify(emailService, times(times)).sendAsync(any());
    }
    private void assertOtherNotification(NotificationType type, int times) {
        service.notifyUsers(dataSource, type);
        verify(emailService, times(times)).sendAsync(any());
    }
}
