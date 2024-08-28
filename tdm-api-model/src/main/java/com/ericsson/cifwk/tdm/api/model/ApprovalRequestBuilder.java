package com.ericsson.cifwk.tdm.api.model;

import java.util.Collections;
import java.util.List;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.CANCELLED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.google.common.collect.Lists.newArrayList;

public final class ApprovalRequestBuilder {

    private ApprovalStatus status;
    private List<User> reviewers = newArrayList();
    private String comment;
    private String approver;

    private ApprovalRequestBuilder() {
    }

    public static ApprovalRequestBuilder aRequestApproval(User... reviewers) {
        return aRequestApproval(newArrayList(reviewers));
    }

    public static ApprovalRequestBuilder aRequestApproval(List<User> reviewers) {
        return anApprovalRequest()
                .withStatus(PENDING)
                .withReviewers(reviewers)
                .withDefaultApprover();
    }

    public static ApprovalRequestBuilder aCancelRequest() {
        return anApprovalRequest()
                .withStatus(CANCELLED)
                .withoutReviewers()
                .withDefaultApprover();
    }

    public static ApprovalRequestBuilder aReject(String comment) {
        return anApprovalRequest()
                .withStatus(REJECTED)
                .withoutReviewers()
                .withComment(comment)
                .withDefaultApprover();

    }

    public static ApprovalRequestBuilder anApprove() {
        return anApprovalRequest()
                .withStatus(APPROVED)
                .withoutReviewers()
                .withDefaultApprover();
    }

    public static ApprovalRequestBuilder anUnApprove() {
        return anApprovalRequest()
                .withStatus(UNAPPROVED)
                .withoutReviewers()
                .withDefaultApprover();
    }

    public static ApprovalRequestBuilder anApprovalRequest() {
        return new ApprovalRequestBuilder();
    }

    public ApprovalRequestBuilder withStatus(ApprovalStatus status) {
        this.status = status;
        return this;
    }

    public ApprovalRequestBuilder withComment(String comment) {
        this.comment = comment;
        return this;
    }

    public ApprovalRequestBuilder withApprover(String aprover) {
        this.approver = aprover;
        return this;
    }

    public ApprovalRequestBuilder withoutReviewers() {
        return withReviewers(Collections.<User>emptyList());
    }

    public ApprovalRequestBuilder withReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
        return this;
    }

    public ApprovalRequestBuilder withReviewer(UserBuilder builder) {
        return withReviewer(builder.build());
    }

    public ApprovalRequestBuilder withReviewer(User reviewer) {
        reviewers.add(reviewer);
        return this;
    }

    public ApprovalRequestBuilder withDefaultApprover() {
        String username = "username";
        approver = username;
        return this;
    }

    public ApprovalRequest build() {
        ApprovalRequest approvalRequest = new ApprovalRequest();
        approvalRequest.setStatus(status);
        approvalRequest.setReviewers(reviewers);
        approvalRequest.setComment(comment);
        approvalRequest.setApprover(approver);
        return approvalRequest;
    }
}
