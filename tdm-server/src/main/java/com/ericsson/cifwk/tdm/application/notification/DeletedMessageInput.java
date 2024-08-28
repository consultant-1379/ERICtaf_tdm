package com.ericsson.cifwk.tdm.application.notification;

import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.email.MsgTemplate;
import static com.ericsson.cifwk.tdm.application.email.MsgTemplate.NOTIFY_DS_DELETED_BODY_EN;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import java.util.List;

/**
 * The input for the email message when the notification is for datasource deletion.
 */
class DeletedMessageInput implements MessageInput {

    private final MsgTemplate bodyTemplate;
    private final List<User> createdBy;
    private final String deletionInstigator;
    DeletedMessageInput(final DataSourceIdentityEntity dataSource, final SecurityService securityService) {
        bodyTemplate = NOTIFY_DS_DELETED_BODY_EN;
        createdBy =  securityService.findUser(dataSource.getCreatedBy());
        final AuthenticationStatus currentUser = securityService.getCurrentUser();
        deletionInstigator = currentUser.getUsername();
    }

    @Override
    public MsgTemplate getBodyTemplate() {
        return bodyTemplate;
    }

    @Override
    public List<User> getTo() {
        return createdBy;
    }

    @Override
    public String getInstigator() {
        return deletionInstigator;
    }
}
