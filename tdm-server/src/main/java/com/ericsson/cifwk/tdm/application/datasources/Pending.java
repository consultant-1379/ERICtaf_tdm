package com.ericsson.cifwk.tdm.application.datasources;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.CANCELLED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVAL_STATUS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.APPROVER;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

/**
 * When in the {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#PENDING} state a datasource can transition to
 * one of three states:
 * 1. {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#CANCELLED}
 * 2. {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#APPROVED}
 * 3. {@link com.ericsson.cifwk.tdm.api.model.ApprovalStatus#REJECTED}
 */
class Pending extends DataSourceStateTemplate implements DataSourceState {

    private final DataSourceIdentityEntity dataSource;
    private final ApprovalRequest request;

    Pending(final DataSourceIdentityEntity dataSource, final ApprovalRequest request) {
        super();
        this.dataSource = dataSource;
        this.request = request;
    }

    @Override
    public void transition() {
        if (CANCELLED == request.getStatus()) {
            cancelRequest();
        } else if (APPROVED == request.getStatus()) {
            approve();
        } else if (REJECTED == request.getStatus()) {
            reject();
        }  else if (PENDING == request.getStatus()) {
            addMoreReviewers();
        }
    }

    private void cancelRequest() {
        values.put(APPROVAL_STATUS, UNAPPROVED.name());
    }

    private void approve() {
        values.put(APPROVAL_STATUS, request.getStatus());
        values.put(APPROVER, request.getApprover());
        reviewers.addAll(dataSource.getReviewers());
        dataSource
                .getVersion()
                .setSnapshot(false);
    }

    private void reject() {
        values.put(APPROVAL_STATUS, request.getStatus());
        values.put(APPROVER, request.getApprover());
        reviewers.addAll(dataSource.getReviewers());
    }

    private void addMoreReviewers() {
        values.put(APPROVAL_STATUS, request.getStatus());
        reviewers.addAll(dataSource.getReviewers());
    }
}
