package com.ericsson.cifwk.tdm.application.notification;

import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_PENDING_BODY_EN;

import java.util.List;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.MsgTemplate;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;

/**
 * The input for the email message when the notification is for datasource approval request.
 */
class PendingMessageInput implements MessageInput {

    private final MsgTemplate bodyTemplate;
    private final List<User> reviewerList;
    private final String reviewRequester;

    PendingMessageInput(final ApprovalRequest request, final SecurityService securityService) {
        bodyTemplate = NOTIFY_DS_PENDING_BODY_EN;
        reviewerList = request.getReviewers();
        final AuthenticationStatus currentUser = securityService.getCurrentUser();
        reviewRequester = currentUser.getUsername();
    }

    @Override
    public MsgTemplate getBodyTemplate() {
        return bodyTemplate;
    }

    @Override
    public List<User> getTo() {
        return reviewerList;
    }

    @Override
    public String getInstigator() {
        return reviewRequester;
    }
}
