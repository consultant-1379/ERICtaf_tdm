package com.ericsson.cifwk.tdm.application.datasources;

import static com.google.common.collect.Maps.newTreeMap;
import static java.util.stream.Collectors.toList;

import static com.ericsson.cifwk.tdm.model.DataSourceActionEntity.approvalStatus;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.COMMENT;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEWERS;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.REVIEW_REQUESTER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity.Attributes.VERSION;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.model.DataSourceActionEntity;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;
import com.google.common.collect.ImmutableSet;

class DataSourceStateTemplate {

    protected final SortedMap<String, Object> values = newTreeMap();
    protected List<String> reviewers = new ArrayList<>();

    public DataSourceActionEntity createDataSourceAction(final DataSourceIdentityEntity identity,
            final ApprovalRequest request) {
        reviewers.addAll(userNames(request.getReviewers()));
        values.put(REVIEWERS, ImmutableSet.copyOf(reviewers).asList());
        values.put(COMMENT, request.getComment());
        values.put(REVIEW_REQUESTER, identity.getReviewRequester());
        values.put(VERSION, identity.getVersion());

        return approvalStatus(identity, values);
    }

    private static List<String> userNames(final List<User> reviewers) {
        return reviewers
                .stream()
                .map(User::getUsername)
                .collect(toList());
    }
}
