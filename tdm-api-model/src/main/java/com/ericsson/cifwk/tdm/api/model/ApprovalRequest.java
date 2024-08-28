package com.ericsson.cifwk.tdm.api.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;

public class ApprovalRequest {

    @NotNull
    private ApprovalStatus status;

    @Valid
    @NotNull
    private List<User> reviewers = newArrayList();

    private String comment;

    private String approver;

    private String hostname;

    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    public List<User> getReviewers() {
        return reviewers;
    }

    public void setReviewers(List<User> reviewers) {
        this.reviewers = reviewers;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getApprover() {
        return approver;
    }

    public void setApprover(String approver) {
        this.approver = approver;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("status", status)
                .add("reviewers", reviewers)
                .add("comment", comment)
                .add("approver", approver)
                .toString();
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}
