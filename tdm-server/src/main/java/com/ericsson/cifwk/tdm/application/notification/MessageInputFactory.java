package com.ericsson.cifwk.tdm.application.notification;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
class MessageInputFactory {

    @Autowired
    private UserService userService;

    @Autowired
    private SecurityService securityService;

    MessageInput getMessageInput(final DataSourceIdentityEntity dataSource, final ApprovalRequest request) {
        final ApprovalStatus status = request.getStatus();
        switch (status) {
            case PENDING:
                return new PendingMessageInput(request, securityService);
            case APPROVED:
                return new ApprovedMessageInput(dataSource, userService);
            case REJECTED:
                return new RejectedMessageInput(dataSource, userService);
            case CANCELLED:
                return new CancelledMessageInput(dataSource, userService);
            default:
                return null;
        }
    }
    @SuppressWarnings("PMD")
    MessageInput getMessageInput(final DataSourceIdentityEntity dataSource, final NotificationType type) {
        switch (type) {
            case DELETION:
                return new DeletedMessageInput(dataSource, securityService);
            default:
                return null;
        }
    }
}
