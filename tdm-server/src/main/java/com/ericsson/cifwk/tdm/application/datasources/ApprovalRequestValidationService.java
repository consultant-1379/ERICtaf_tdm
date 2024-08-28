package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.ContextRole;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

@Service
public class ApprovalRequestValidationService {

    public static final String ERR_APPROVAL_ILLEGAL_TRANSITION = "Cannot transition Data Source %s approval status " +
        "from %s to %s";
    public static final String ERR_REQUEST_APPROVAL_NO_REVIEWERS = "Request approval must have at least one reviewer";
    public static final String ERR_REQUEST_APPROVAL_SUBMITTER_REVIEWER = "Only test managers can be approval " +
            "submitters and reviewers at the same time";
    public static final String ERR_REQUEST_APPROVAL_REVIEWER_UNAUTHORIZED = "Reviewers are not authorized for making " +
        "approval decisions";

    public static final String ERROR_REJECTED_WITHOUT_COMMENT = "Data Source cannot be rejected without a comment";

    @Autowired
    private SecurityService securityService;

    @Autowired
    private UserService userService;

    public void validate(DataSourceIdentityEntity dataSource, ApprovalRequest request) {
        validateStatusTransition(dataSource, request);
        validateReviewers(dataSource, request);
        validateComment(request);
    }

    void validateStatusTransition(DataSourceIdentityEntity dataSource, ApprovalRequest request) {
        ApprovalStatus oldStatus = dataSource.getApprovalStatus();
        ApprovalStatus newStatus = request.getStatus();
        if (!PENDING.equals(oldStatus)) {
            checkArgument(oldStatus.canTransitionTo(newStatus),
                    ERR_APPROVAL_ILLEGAL_TRANSITION,
                    dataSource.getId(), oldStatus, newStatus);
        }
    }

    void validateReviewers(DataSourceIdentityEntity dataSource, ApprovalRequest request) {
        if (isRequestForApproval(dataSource.getApprovalStatus(), request.getStatus())) {
            List<User> reviewers = request.getReviewers();
            checkArgument(!reviewers.isEmpty(), ERR_REQUEST_APPROVAL_NO_REVIEWERS);

            validateSubmitterAmongReviewers(reviewers);
            validateReviewerAuthorization(dataSource.getContextId(), reviewers);
        }
    }

    boolean isRequestForApproval(ApprovalStatus from, ApprovalStatus to) {
        return (UNAPPROVED.equals(from) || PENDING.equals(from))  && PENDING.equals(to);
    }

    private void validateSubmitterAmongReviewers(List<User> reviewers) {
        String userId = securityService.getCurrentUser().getUsername();
        boolean requesterAmongReviewers = reviewers.stream()
                .anyMatch(reviewer -> userId.equalsIgnoreCase(reviewer.getUsername()));
        List<ContextRole> roles = securityService.getCurrentUser().getRoles();
        if (requesterAmongReviewers) {
            for (ContextRole role : roles) {
                if ("ROLE_TEST_MANAGER".equals(role.getRole()) && requesterAmongReviewers) {
                    requesterAmongReviewers = !requesterAmongReviewers;
                }
            }
        }
        checkArgument(!requesterAmongReviewers, ERR_REQUEST_APPROVAL_SUBMITTER_REVIEWER);
    }

    private void validateReviewerAuthorization(String contextId, List<User> reviewers) {
        List<User> authorizedReviewers = userService.findByContextId(contextId);
        checkArgument(authorizedReviewers.containsAll(reviewers), ERR_REQUEST_APPROVAL_REVIEWER_UNAUTHORIZED);
    }

    void validateComment(ApprovalRequest request) {
        checkArgument(!rejectedWithoutComment(request), ERROR_REJECTED_WITHOUT_COMMENT);
    }

    private static boolean rejectedWithoutComment(ApprovalRequest request) {
        return REJECTED.equals(request.getStatus()) && isNullOrEmpty(request.getComment());
    }
}
