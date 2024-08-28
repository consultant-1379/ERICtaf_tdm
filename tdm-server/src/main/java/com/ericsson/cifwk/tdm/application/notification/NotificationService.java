package com.ericsson.cifwk.tdm.application.notification;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.EmailService;
import com.ericsson.cifwk.tdm.application.email.MailMessageBean;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.google.common.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import static com.ericsson.cifwk.tdm.application.email.MailMessageBean.MailMessageBuilder.aMailMessageBean;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_CHANGE_SUBJ_EN;
import static java.lang.String.format;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

@Service
public class NotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationService.class);
    public static final String DS_URL_TEMPLATE = "http://%s/#/contexts/%s/datasources/%s";
    private static final String DS_NAME = "ds_name";
    private static final String DS_VERSION = "ds_version";

    @Value("${server.socket}")
    private String socket;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Autowired
    private MessageInputFactory messageInputFactory;

    public void notifyUsers(DataSourceIdentityEntity dataSource, NotificationType type) {
        MessageInput messageInput = messageInputFactory.getMessageInput(dataSource, type);
        if (messageInput == null) {
            LOGGER.debug("No predefined Message for Notification Type", type);
            return;
        }
        MailMessageBean msg = prepareMessage(messageInput, dataSource);
        emailService.sendAsync(msg);

    }

    public void notifyDataSourceChange(DataSourceIdentityEntity dataSource, ApprovalRequest request) {
        MessageInput messageInput = messageInputFactory.getMessageInput(dataSource, request);
        if (messageInput == null) {
            LOGGER.debug("not any notification for data source with [{}] status", request.getStatus());
            return;
        }

        MailMessageBean msg = prepareMessage(messageInput, dataSource, request);
        emailService.sendAsync(msg);
    }
    @VisibleForTesting
    MailMessageBean prepareMessage(MessageInput messageInput, DataSourceIdentityEntity dataSource) {
        Set<String> recipients = messageInput.getTo().stream()
                .map(User::getEmail)
                .collect(toSet());

        User requester = userService.findByUsername(messageInput.getInstigator());

        return aMailMessageBean()
                .withTo(recipients)
                .withCc(requester.getEmail())
                .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
                .withSubjectParam(DS_NAME, dataSource.getName())
                .withSubjectParam(DS_VERSION, dataSource.getVersion())
                .withBodyTemplate(messageInput.getBodyTemplate())
                .withBodyParam("context", dataSource.getContext())
                .withBodyParam(DS_NAME, dataSource.getName())
                .withBodyParam(DS_VERSION, dataSource.getVersion())
                .withBodyParam("createdBy", dataSource.getCreatedBy())
                .withBodyParam("updatedBy", dataSource.getUpdatedBy())
                .withBodyParam("requestedBy", requester.getUsername())
                .build();
    }

    @VisibleForTesting
    MailMessageBean prepareMessage(MessageInput messageInput, DataSourceIdentityEntity dataSource,
                                   ApprovalRequest request) {

        String url;
        String hostname = request.getHostname();
        final String formattedContext = formatContextForUrl(dataSource);
        if (hostname == null || hostname.isEmpty()) {
            url = format(DS_URL_TEMPLATE, socket, formattedContext, dataSource.getId());
        } else {
            url = format(DS_URL_TEMPLATE, hostname, formattedContext, dataSource.getId());
        }

        Set<String> recipients = messageInput.getTo().stream()
            .map(User::getEmail)
            .collect(toSet());

        String reviewers = messageInput.getTo().stream()
            .map(User::getFullname)
            .collect(joining(" / "));

        User requester = userService.findByUsername(messageInput.getInstigator());

        return aMailMessageBean()
            .withTo(recipients)
            .withCc(requester.getEmail())
            .withSubjTemplate(NOTIFY_DS_CHANGE_SUBJ_EN)
            .withSubjectParam(DS_NAME, dataSource.getName())
            .withSubjectParam(DS_VERSION, dataSource.getVersion())
            .withBodyTemplate(messageInput.getBodyTemplate())
            .withBodyParam("reviewers", reviewers)
            .withBodyParam("url", url)
            .withBodyParam("context", dataSource.getContext())
            .withBodyParam(DS_NAME, dataSource.getName())
            .withBodyParam(DS_VERSION, dataSource.getVersion())
            .withBodyParam("createdBy", dataSource.getCreatedBy())
            .withBodyParam("updatedBy", dataSource.getUpdatedBy())
            .withBodyParam("requestedBy", requester.getUsername())
            .withBodyParam("comment", request.getComment())
            .build();
    }

    private static String formatContextForUrl(final DataSourceIdentityEntity dataSource) {
        try {
            return UriUtils.encode(dataSource.getContext(), StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            LOGGER.error("Could not encode [{}] for the link in the email. Putting it as is into the link in the email",
                    dataSource.getContext(), e);
            return dataSource.getContext();
        }
    }
}
