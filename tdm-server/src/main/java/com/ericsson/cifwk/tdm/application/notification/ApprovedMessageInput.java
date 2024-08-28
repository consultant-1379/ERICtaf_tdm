package com.ericsson.cifwk.tdm.application.notification;

import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_APPROVED_BODY_EN;

import java.util.List;

import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.MsgTemplate;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

/**
 * The input for the email message when the notification is for datasource version approval.
 */
class ApprovedMessageInput implements MessageInput {

    private final MsgTemplate bodyTemplate;
    private final List<User> reviewerList;
    private final String reviewRequester;

    ApprovedMessageInput(final DataSourceIdentityEntity dataSource, final UserService userService) {
        bodyTemplate = NOTIFY_DS_APPROVED_BODY_EN;
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
