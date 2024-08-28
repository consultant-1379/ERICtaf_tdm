package com.ericsson.cifwk.tdm.application.notification;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.MsgTemplate;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_CANCELLED_BODY_EN;

/**
 * The input for the email message when the notification is for datasource approval rejection.
 */
class CancelledMessageInput implements MessageInput {

    private final MsgTemplate bodyTemplate;
    private final List<User> reviewerList;
    private final String reviewRequester;

    CancelledMessageInput(final DataSourceIdentityEntity dataSource, final UserService userService) {
        bodyTemplate = NOTIFY_DS_CANCELLED_BODY_EN;
        reviewerList = userService.findUsers(dataSource.getReviewers());
        reviewRequester = dataSource.getReviewRequester();
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
