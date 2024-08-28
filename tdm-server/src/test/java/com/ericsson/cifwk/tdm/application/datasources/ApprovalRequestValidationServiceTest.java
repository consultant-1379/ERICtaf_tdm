package com.ericsson.cifwk.tdm.application.datasources;

import com.ericsson.cifwk.tdm.api.model.ContextRole;
import java.util.ArrayList;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.assertThatThrownBy;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import static com.ericsson.cifwk.tdm.CustomAssertions.assertThatNoExceptionsThrownBy;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprovalRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERROR_REJECTED_WITHOUT_COMMENT;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_APPROVAL_ILLEGAL_TRANSITION;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_REQUEST_APPROVAL_NO_REVIEWERS;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_REQUEST_APPROVAL_REVIEWER_UNAUTHORIZED;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_REQUEST_APPROVAL_SUBMITTER_REVIEWER;
import static com.ericsson.cifwk.tdm.model.DataSourceIdentityEntityBuilder.aDataSourceIdentityEntity;
import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.AuthenticationStatus;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.application.user.UserService;
import com.ericsson.cifwk.tdm.infrastructure.security.SecurityService;
import com.ericsson.cifwk.tdm.model.DataSourceIdentityEntity;

@RunWith(MockitoJUnitRunner.class)
public class ApprovalRequestValidationServiceTest {

    @Spy
    @InjectMocks
    private ApprovalRequestValidationService service;

    @Mock
    private SecurityService securityService;

    @Mock
    private UserService userService;

    private DataSourceIdentityEntity dataSource;
    private ApprovalRequest request;

    @Before
    public void setUp() throws Exception {
        dataSource = aDataSourceIdentityEntity()
                .withId("dataSourceId")
                .withContextId("contextId")
                .build();
        request = anApprovalRequest().build();
    }

    @Test
    public void validate_verifyThatAllValidationsAreCalled() throws Exception {
        doNothing().when(service).validateStatusTransition(dataSource, request);
        doNothing().when(service).validateReviewers(dataSource, request);
        doNothing().when(service).validateComment(request);

        service.validate(dataSource, request);

        verify(service).validate(dataSource, request);
        verify(service).validateStatusTransition(dataSource, request);
        verify(service).validateReviewers(dataSource, request);
        verify(service).validateComment(request);

        verifyNoMoreInteractions(service);
        verifyZeroInteractions(securityService, userService);
    }

    @Test
    public void validateStatusTransition_shouldThrowException_whenIllegalStatusTransition() throws Exception {
        dataSource.setApprovalStatus(UNAPPROVED);
        request.setStatus(APPROVED);

        assertThatThrownBy(() -> service.validateStatusTransition(dataSource, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                    ERR_APPROVAL_ILLEGAL_TRANSITION,
                        dataSource.getId(), UNAPPROVED, APPROVED
                );
    }

    @Test
    public void validateReviewers_shouldNotValidate_whenNotRequestForApproval() throws Exception {
        requestForApprovalIs(false);
        request.setReviewers(emptyList());

        assertThatNoExceptionsThrownBy(() -> service.validateReviewers(dataSource, request));
    }

    @Test
    public void validateReviewers_shouldThrowException_whenNoReviewers() throws Exception {
        requestForApprovalIs(true);
        request.setReviewers(emptyList());

        assertThatThrownBy(() -> service.validateReviewers(dataSource, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERR_REQUEST_APPROVAL_NO_REVIEWERS);
    }

    @Test
    public void validateReviewers_shouldThrowException_whenSubmitterAmongReviewers() throws Exception {
        requestForApprovalIs(true);
        currentUserIs("user");
        reviewersAre("reviewer1", "user", "reviewer2");

        assertThatThrownBy(() -> service.validateReviewers(dataSource, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERR_REQUEST_APPROVAL_SUBMITTER_REVIEWER);
    }

    @Test
    public void validateReviewers_whenSubmitterIsTestManagerAndReviewer() throws Exception {
    requestForApprovalIs(true);
    currentTestManagerIs("authorized1");
    reviewersAre("authorized1", "authorized2");
    authorizedReviewersAre("authorized1", "authorized2", "authorized3");

    assertThatNoExceptionsThrownBy(() -> service.validateReviewers(dataSource, request));
    }

    @Test
    public void validateReviewers_shouldThrowException_whenReviewerNotAuthorized() throws Exception {
        requestForApprovalIs(true);
        currentUserIs("user");
        reviewersAre("authorized1", "unauthorized", "authorized2");
        authorizedReviewersAre("authorized1", "authorized2");

        assertThatThrownBy(() -> service.validateReviewers(dataSource, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERR_REQUEST_APPROVAL_REVIEWER_UNAUTHORIZED);
        verify(userService).findByContextId(dataSource.getContextId());
    }

    @Test
    public void validateReviewers_happyPath() throws Exception {
        requestForApprovalIs(true);
        currentUserIs("user");
        reviewersAre("authorized1", "authorized2");
        authorizedReviewersAre("authorized1", "authorized2", "authorized3");

        assertThatNoExceptionsThrownBy(() -> service.validateReviewers(dataSource, request));
    }

    @Test
    public void isRequestForApproval_shouldReturnTrue_whenFrom_UNAPPROVED_andTo_PENDING() throws Exception {
        assertThat(service.isRequestForApproval(UNAPPROVED, PENDING)).isTrue();
    }

    @Test
    public void isRequestForApproval_shouldReturnFalse_whenFrom_not_UNAPPROVED_or_PENDING() throws Exception {
        for (ApprovalStatus from : allExcept(UNAPPROVED, PENDING)) {
            assertThat(service.isRequestForApproval(from, PENDING)).isFalse();
        }
    }

    @Test
    public void isRequestForApproval_shouldReturnFalse_whenTo_not_PENDING() throws Exception {
        for (ApprovalStatus to : allExcept(PENDING)) {
            assertThat(service.isRequestForApproval(UNAPPROVED, to)).isFalse();
        }
    }

    @Test
    public void validateComment_shouldThrowException_whenRejectWithNullComment() throws Exception {
        request.setStatus(REJECTED);
        request.setComment(null);

        assertThatThrownBy(() -> service.validateComment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERROR_REJECTED_WITHOUT_COMMENT);
    }

    @Test
    public void validateComment_shouldThrowException_whenRejectWithEmptyComment() throws Exception {
        request.setStatus(REJECTED);
        request.setComment("");

        assertThatThrownBy(() -> service.validateComment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(ERROR_REJECTED_WITHOUT_COMMENT);
    }

    @Test
    public void validateComment_shouldPass_whenNotReject() throws Exception {
        request.setComment(null);
        for (ApprovalStatus status : allExcept(REJECTED)) {
            request.setStatus(status);

            assertThatNoExceptionsThrownBy(() -> service.validateComment(request));
        }
    }

    @Test
    public void validateComment_shouldPass_whenNonEmptyComment() throws Exception {
        request.setStatus(REJECTED);
        request.setComment("comment");

        assertThatNoExceptionsThrownBy(() -> service.validateComment(request));
    }

    private void requestForApprovalIs(boolean condition) {
        doReturn(condition).when(service).isRequestForApproval(
                any(ApprovalStatus.class), any(ApprovalStatus.class)
        );
    }

    private void currentUserIs(String userId) {
        doReturn(new AuthenticationStatus(userId, true, emptyList(),false))
                .when(securityService).getCurrentUser();
    }
    private void currentTestManagerIs(String userId) {
        doReturn(new AuthenticationStatus(userId, true, asList(new ContextRole(null,"ROLE_TEST_MANAGER")),false))
                .when(securityService).getCurrentUser();
    }

    private void reviewersAre(String... userNames) {
        request.setReviewers(createUsers(userNames));
    }

    private void authorizedReviewersAre(String... userNames) {
        doReturn(createUsers(userNames)).when(userService).findByContextId(anyString());
    }

    private List<User> createUsers(String... userNames) {
        return stream(userNames)
                .map(userName -> anUser().withUsername(userName).build())
                .collect(toList());
    }

    private Collection<ApprovalStatus> allExcept(ApprovalStatus... exception) {
        Set<ApprovalStatus> statuses = newHashSet(ApprovalStatus.values());
        for(ApprovalStatus status : exception) {
            statuses.remove(status);
        }
        return statuses;
    }
}
