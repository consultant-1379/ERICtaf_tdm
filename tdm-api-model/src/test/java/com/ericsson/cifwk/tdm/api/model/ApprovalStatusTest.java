package com.ericsson.cifwk.tdm.api.model;

import org.junit.Test;

import java.util.Set;

import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.CANCELLED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.google.common.collect.Sets.newHashSet;
import static com.google.common.truth.Truth.assertThat;

public class ApprovalStatusTest {

    @Test
    public void shouldAllowTransitionFrom_UNAPPROVED_onlyTo_PENDING() throws Exception {
        assertValidTransitionsBetween(UNAPPROVED, PENDING);
    }

    @Test
    public void shouldAllowTransitionFrom_PENDING_anywhereExceptFrom_PENDING() throws Exception {
        assertValidTransitionsBetween(PENDING, UNAPPROVED, REJECTED, APPROVED, CANCELLED);
    }

    @Test
    public void shouldAllowTransitionFrom_REJECTED_onlyTo_UNAPPROVED() throws Exception {
        assertValidTransitionsBetween(REJECTED, UNAPPROVED);
    }

    @Test
    public void shouldAllowTransitionFrom_APPROVED_onlyTo_UNAPPROVED() throws Exception {
        assertValidTransitionsBetween(APPROVED, UNAPPROVED);
    }

    private void assertValidTransitionsBetween(ApprovalStatus from, ApprovalStatus... transitionsTo) {
        Set<ApprovalStatus> validTransitions = newHashSet(transitionsTo);
        for (ApprovalStatus to : ApprovalStatus.values()) {
            assertThat(from.canTransitionTo(to)).isEqualTo(validTransitions.contains(to));
        }
    }
}