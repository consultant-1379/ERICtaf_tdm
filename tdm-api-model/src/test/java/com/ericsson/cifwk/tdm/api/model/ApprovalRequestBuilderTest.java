package com.ericsson.cifwk.tdm.api.model;

import org.junit.Test;

import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aCancelRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aReject;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aRequestApproval;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprovalRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprove;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anUnApprove;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.CANCELLED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.google.common.truth.Truth.assertThat;

public class ApprovalRequestBuilderTest {

    @Test
    public void build_empty() throws Exception {
        ApprovalRequest request = anApprovalRequest().build();

        assertThat(request.getStatus()).isNull();
        assertThat(request.getReviewers()).isEmpty();
        assertThat(request.getComment()).isNull();
    }

    @Test
    public void build_full() throws Exception {
        User reviewer = anUser().build();

        ApprovalRequest request = anApprovalRequest()
                .withStatus(PENDING)
                .withReviewer(reviewer)
                .withDefaultApprover()
                .build();

        assertThat(request.getStatus()).isSameAs(PENDING);
        assertThat(request.getReviewers()).containsExactly(reviewer);
        assertThat(request.getComment()).isNull();
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void build_aRequestApproval() throws Exception {
        User reviewer = anUser().build();

        ApprovalRequest request = aRequestApproval(reviewer).build();

        assertThat(request.getStatus()).isSameAs(PENDING);
        assertThat(request.getReviewers()).containsExactly(reviewer);
        assertThat(request.getComment()).isNull();
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void build_aCancelRequest() throws Exception {
        ApprovalRequest request = aCancelRequest().build();

        assertThat(request.getStatus()).isSameAs(CANCELLED);
        assertThat(request.getReviewers()).isEmpty();
        assertThat(request.getComment()).isNull();
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void build_aReject() throws Exception {
        ApprovalRequest request = aReject("Reject reason").build();

        assertThat(request.getStatus()).isSameAs(REJECTED);
        assertThat(request.getReviewers()).isEmpty();
        assertThat(request.getComment()).isEqualTo("Reject reason");
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void build_anApprove() throws Exception {
        ApprovalRequest request = anApprove().build();

        assertThat(request.getStatus()).isSameAs(APPROVED);
        assertThat(request.getReviewers()).isEmpty();
        assertThat(request.getComment()).isNull();
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void build_anUnApprove() throws Exception {
        ApprovalRequest request = anUnApprove().build();

        assertThat(request.getStatus()).isSameAs(UNAPPROVED);
        assertThat(request.getReviewers()).isEmpty();
        assertThat(request.getComment()).isNull();
        assertThat(request.getApprover()).isEqualTo("username");
    }

    @Test
    public void withoutReviewers() throws Exception {
        ApprovalRequest request = anApprovalRequest()
                .withReviewer(anUser())
                .withoutReviewers()
                .build();

        assertThat(request.getReviewers()).isEmpty();
    }

    @Test
    public void withReviewer() throws Exception {
        ApprovalRequest request = anApprovalRequest()
                .withReviewer(anUser()
                        .withId(42L)
                        .withUsername("username")
                        .withFirstName("firstName")
                        .withLastName("lastName")
                        .withEmail("email"))
                .build();

        User reviewer = request.getReviewers().iterator().next();

        assertThat(reviewer.getId()).isEqualTo(42L);
        assertThat(reviewer.getUsername()).isEqualTo("username");
        assertThat(reviewer.getFirstName()).isEqualTo("firstName");
        assertThat(reviewer.getLastName()).isEqualTo("lastName");
        assertThat(reviewer.getEmail()).isEqualTo("email");
    }
}
