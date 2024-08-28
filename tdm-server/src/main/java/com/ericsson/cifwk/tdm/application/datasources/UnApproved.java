package com.ericsson.cifwk.tdm.application.datasources;

import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

/**
 * When in the {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#UNAPPROVED} state a datasource can only
 * transition to {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#PENDING}.
 */
class UnApproved extends DataSourceStateTemplate implements DataSourceState {

    private final DataSourceIdentityEntity dataSource;
    private final ApprovalRequest request;
    private final SecurityService securityService;

    UnApproved(final DataSourceIdentityEntity dataSource, final ApprovalRequest request,
            final SecurityService securityService) {
        super();
        this.dataSource = dataSource;
        this.request = request;
        this.securityService = securityService;
    }

    @Override
    public void transition() {
        values.put(APPROVAL_STATUS, request.getStatus());
        values.put(APPROVER, null);
        dataSource.setReviewRequester(securityService.getCurrentUser().getUsername());
        reviewers.addAll(dataSource.getReviewers());
    }

}
