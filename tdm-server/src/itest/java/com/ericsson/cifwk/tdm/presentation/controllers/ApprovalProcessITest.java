package com.ericsson.cifwk.tdm.presentation.controllers;

import static java.lang.String.format;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aCancelRequest;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aReject;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.aRequestApproval;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anApprove;
import static com.ericsson.cifwk.tdm.api.model.ApprovalRequestBuilder.anUnApprove;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.APPROVED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.PENDING;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.REJECTED;
import static com.ericsson.cifwk.tdm.api.model.ApprovalStatus.UNAPPROVED;
import static com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder.aDataSourceAction;
import static com.ericsson.cifwk.tdm.api.model.UserBuilder.anUser;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERROR_REJECTED_WITHOUT_COMMENT;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_APPROVAL_ILLEGAL_TRANSITION;
import static com.ericsson.cifwk.tdm.application.datasources.ApprovalRequestValidationService.ERR_REQUEST_APPROVAL_NO_REVIEWERS;

import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseListFile;
import static com.ericsson.cifwk.tdm.application.util.JsonParser.parseObject;
import static com.ericsson.cifwk.tdm.configuration.ITestsProfiles.MOCK_REST_REPOSITORIES;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.MANAGER_USER;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_2;
import static com.ericsson.cifwk.tdm.configuration.MockRestRepositoriesConfiguration.TAF_USER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.ericsson.cifwk.tdm.api.model.ApprovalRequest;
import com.ericsson.cifwk.tdm.api.model.ApprovalStatus;
import com.ericsson.cifwk.tdm.api.model.DataRecord;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction;
import com.ericsson.cifwk.tdm.api.model.DataSourceAction.DataSourceActionBuilder;
import com.ericsson.cifwk.tdm.api.model.DataSourceIdentity;
import com.ericsson.cifwk.tdm.api.model.Records;
import com.ericsson.cifwk.tdm.api.model.User;
import com.ericsson.cifwk.tdm.api.model.UserCredentials;
import com.ericsson.cifwk.tdm.api.model.validation.FieldError;
import com.ericsson.cifwk.tdm.api.model.validation.ValidationError;
import com.ericsson.cifwk.tdm.application.contexts.TceContextRepository;
import com.ericsson.cifwk.tdm.application.datasources.DataSourceActionType;
import com.ericsson.cifwk.tdm.db.MongoBee;
import com.ericsson.cifwk.tdm.infrastructure.ScheduledTasks;
import com.ericsson.cifwk.tdm.presentation.exceptions.ErrorMessage;
import com.ericsson.gic.tms.presentation.dto.ContextBean;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles({INTEGRATION_TEST, MOCK_REST_REPOSITORIES})
@MongoBee(location = "com.ericsson.cifwk.tdm.infrastructure.changelogs", invokeCleanBeforeMethod = true)
public class ApprovalProcessITest extends AbstractControllerITest {

    private static final String VALID_REJECT_COMMENT = "Reject reason";
    private final UserCredentials reviewRequester = new UserCredentials(TAF_USER, "taf");

    @MockBean
    private TceContextRepository contextClient;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Before
    public void setUp() {
        List<ContextBean> contextList = parseListFile("contexts/contexts.json", ContextBean.class);
        when(contextClient.getContexts()).thenReturn(contextList);

        scheduledTasks.runJobLoadContexts();
    }

    @Test
    public void handleApproval_shouldFail_whenDataSourceId_null() throws Exception {
        ApprovalRequest request = aRequestApproval(tceManager()).build();

        MockHttpServletResponse response = handleApprovalExpectingFailure(null, request);

        assertThat(response.getStatus()).isEqualTo(METHOD_NOT_ALLOWED.value());
    }

    @Test
    public void handleApproval_shouldFail_whenDataSourceId_doesNotExist() throws Exception {
        ApprovalRequest request = aRequestApproval(tceManager()).build();

        MockHttpServletResponse response = handleApprovalExpectingFailure("123456789abcdef123456789", request);

        assertThat(response.getStatus()).isEqualTo(NOT_FOUND.value());
    }

    @Test
    public void handleApproval_shouldFailRequestValidation_whenApprovalStatus_null() throws Exception {
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = aRequestApproval(tceManager())
                .withStatus(null)
                .build();

        MockHttpServletResponse response = handleApprovalExpectingFailure(dataSourceId, request);

        ValidationError validationError = parseObject(response.getContentAsString(), ValidationError.class);
        assertThat(validationError.getFieldErrors()).containsExactly(
                new FieldError("status", "may not be null")
        );
    }

    @Test
    public void handleApproval_shouldFailRequestValidation_whenReviewers_null() throws Exception {
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = aRequestApproval((List<User>) null).build();

        MockHttpServletResponse response = handleApprovalExpectingFailure(dataSourceId, request);

        ValidationError validationError = parseObject(response.getContentAsString(), ValidationError.class);
        assertThat(validationError.getFieldErrors()).containsExactly(
                new FieldError("reviewers", "may not be null")
        );
    }

    @Test
    public void handleApproval_shouldFailRequestValidation_whenReviewersMissingFields() throws Exception {
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = aRequestApproval(
                anUser().withId(null).withUsername("username").build(),
                anUser().withId(42L).withUsername("").build()
        ).build();

        MockHttpServletResponse response = handleApprovalExpectingFailure(dataSourceId, request);

        ValidationError validationError = parseObject(response.getContentAsString(), ValidationError.class);
        assertThat(validationError.getFieldErrors()).contains(
                new FieldError("reviewers[0].id", "may not be null"),
                new FieldError("reviewers[1].username", "may not be empty")
        );
    }

    @Test
    public void handleApproval_shouldFailBusinessValidation_whenIllegalStatusTransition() throws Exception {
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = anApprove().build();

        ErrorMessage message = dataSourceControllerClient
                .getResponseAs(handleApprovalExpectingFailure(dataSourceId, request), ErrorMessage.class);

        assertThat(message.getMessage()).isEqualTo(
                format(ERR_APPROVAL_ILLEGAL_TRANSITION, dataSourceId, UNAPPROVED, APPROVED)
        );
    }

    @Test
    public void handleApproval_shouldReturnErrorOnUnApproveIfDataSourceNotFound() throws Exception {
        handleApprovalExpectingFailure("non-existing-ds", anUnApprove().build());
    }

    @Test
    public void handleApproval_shouldReturnErrorOnApproveIfDataSourceNotFound() throws Exception {
        handleApprovalExpectingFailure("non-existing-ds", anApprove().build());
    }

    @Test
    public void edit_shouldReturnBadRequest_whenApprovalStatus_PENDING() throws Exception {
        String dataSourceId = createDataSource().getId();
        requestApproval(dataSourceId, tceManager());

        DataSourceAction action = aDataSourceAction()
                .withId(dataSourceId)
                .withType(DataSourceActionType.IDENTITY_NAME_EDIT.name())
                .withNewValue("newDataSourceName")
                .withVersion("0.0.1")
                .build();

        dataSourceControllerClient.tryEdit(dataSourceId, action)
                                  .andExpect(status().isBadRequest());
    }

    @Test
    public void handleApproval_shouldFailBusinessValidation_whenNoReviewers() throws Exception {
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = aRequestApproval().withoutReviewers().build();

        ErrorMessage message = dataSourceControllerClient
                .getResponseAs(handleApprovalExpectingFailure(dataSourceId, request), ErrorMessage.class);

        assertThat(message.getMessage()).isEqualTo(ERR_REQUEST_APPROVAL_NO_REVIEWERS);
    }

    @Test
    public void handleApproval_shouldFailBusinessValidation_whenRejectedWithoutComment() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();
        ApprovalRequest request = aReject(null).build();

        requestApproval(dataSourceId, tceManager());

        ErrorMessage message = dataSourceControllerClient.getResponseAs(
                handleApprovalExpectingFailure(dataSourceId, request), ErrorMessage.class);

        assertThat(message.getMessage()).isEqualTo(ERROR_REJECTED_WITHOUT_COMMENT);
        dataSourceControllerClient.logout();
    }

    @Test
    public void handleApproval_happyPath_requestApproval() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();

        DataSourceIdentity dataSource = requestApproval(dataSourceId, tceManager());

        assertThat(dataSource.getApprovalStatus()).isEqualTo(PENDING);
        assertThat(dataSource.getReviewers()).containsExactly(MANAGER_USER);
        assertThat(dataSource.getComment()).isEmpty();
        assertThat(dataSource.getReviewRequester()).matches(TAF_USER);
        dataSourceControllerClient.logout();
    }

    @Test
    public void handleApproval_happyPath_cancelRequest() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();
        requestApproval(dataSourceId, tceManager());

        DataSourceIdentity dataSource = cancelRequest(dataSourceId);

        assertThat(dataSource.getApprovalStatus()).isEqualTo(UNAPPROVED);
        assertThat(dataSource.getReviewers()).isEmpty();
        assertThat(dataSource.getComment()).isEmpty();
        dataSourceControllerClient.logout();
    }

    @Test
    public void handleApproval_happyPath_reject() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();
        requestApproval(dataSourceId, taf2());
        dataSourceControllerClient.logout();

        dataSourceControllerClient.login(new UserCredentials(TAF_2, "taf2"));
        DataSourceIdentity dataSource = reject(dataSourceId);

        assertThat(dataSource.getApprovalStatus()).isEqualTo(REJECTED);
        assertThat(dataSource
                .getReviewers()
                .size()).isEqualTo(1);
        assertThat(dataSource.getComment()).isEqualTo(VALID_REJECT_COMMENT);
        assertThat(dataSource.getReviewRequester()).matches(TAF_USER);
        dataSourceControllerClient.logout();
    }

    @Test
    public void handleApproval_happyPath_approve() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();
        approvalHappyPath(dataSourceId);
        dataSourceControllerClient.logout();
    }

    private void approvalHappyPath(final String dataSourceId) throws Exception {
        requestApproval(dataSourceId, taf2());
        dataSourceControllerClient.logout();

        dataSourceControllerClient.login(new UserCredentials(TAF_2, "taf2"));
        DataSourceIdentity dataSource = approve(dataSourceId);

        assertThat(dataSource.getApprovalStatus()).isEqualTo(APPROVED);
        assertThat(dataSource
                .getReviewers()).hasSize(1);
        assertThat(dataSource.getApprover()).isNotEmpty();
        assertThat(dataSource.getComment()).isEmpty();
        assertThat(dataSource.getReviewRequester()).isNotEmpty();

        assertVersionApprovalStatus(dataSourceId, "0.0.1", APPROVED);
        assertVersionReviewRequester(dataSourceId, "0.0.1", "taf");
    }

    @Test
    public void dataSourceStillApprovedOnceNextSnapshotVersionIsCreated() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();
        approvalHappyPath(dataSourceId);
        DataSourceAction action = aDataSourceAction().withId(dataSourceId)
                                                     .withType(DataSourceActionType.RECORD_ADD.name())
                                                     .withKey("name")
                                                     .withNewValue("Alpha Laputa IV")
                                                     .withVersion("0.0.2-SNAPSHOT")
                                                     .withLocalTimestamp(1460009673453L)
                                                     .build();
        DataSourceAction newVersion = newVersionAction(dataSourceId, "0.0.2-SNAPSHOT");
        dataSourceControllerClient.edit(dataSourceId, newVersion, action);
        assertVersionApprovalStatus(dataSourceId, "0.0.1", APPROVED);
        dataSourceControllerClient.logout();
    }

    @Test
    public void handleApproval_shouldStoreApprovalStatus_forAllVersions() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSourceWithApprovalHistory().getId();

        assertVersionApprovalStatus(dataSourceId, "0.0.3-SNAPSHOT", PENDING);
        assertVersionApprovalStatus(dataSourceId, "0.0.2", APPROVED);
        assertVersionApprovalStatus(dataSourceId, "0.0.1", APPROVED);
    }

    @Test
    public void review_shouldOnlyShowDeletedRecordsSinceLastApprovedVersion() throws Exception {
        dataSourceControllerClient.login(reviewRequester);
        String dataSourceId = createDataSource().getId();

        requestApproval(dataSourceId, taf2());
        dataSourceControllerClient.logout();

        dataSourceControllerClient.login(new UserCredentials(TAF_2, "taf2"));

        Records records = dataSourceControllerClient.getRecords(dataSourceId, true);
        assertThat(records.getData())
                .hasSize(10)
                .extracting(DataRecord::isDeleted)
                .doesNotContain(true);

        approve(dataSourceId);

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(reviewRequester);

        deleteARecord(dataSourceId, "0.0.2-SNAPSHOT");

        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();

        dataSourceControllerClient.login(new UserCredentials(TAF_2, "taf2"));

        records = dataSourceControllerClient.getRecords(dataSourceId, true);
        assertThat(records.getData())
                .hasSize(10)
                .extracting(DataRecord::isDeleted)
                .filteredOn(aBoolean -> aBoolean.equals(true))
                .hasSize(1);

        approve(dataSourceId);

        dataSourceControllerClient.logout();
        dataSourceControllerClient.login(reviewRequester);

        deleteARecord(dataSourceId, "0.0.3-SNAPSHOT");

        requestApproval(dataSourceId, taf2());

        dataSourceControllerClient.logout();

        dataSourceControllerClient.login(new UserCredentials(TAF_2, "taf2"));

        records = dataSourceControllerClient.getRecords(dataSourceId, true);
        assertThat(records.getData())
                .hasSize(9)
                .extracting(DataRecord::isDeleted)
                .filteredOn(aBoolean -> aBoolean.equals(true))
                .hasSize(1);
    }

    private void assertVersionApprovalStatus(String id, String version, ApprovalStatus status) throws Exception {
        assertThat(dataSourceControllerClient.getById(id, version).getApprovalStatus()).isEqualTo(status);
    }

    private void assertVersionReviewRequester(String id, String version, String reviewRequester) throws Exception {
        assertThat(dataSourceControllerClient
                .getById(id, version)
                .getReviewRequester()).matches(reviewRequester);
    }

    private User taf2() {
        return anUser()
                .withId(1134L)
                .withUsername(TAF_2)
                .build();
    }

    private MockHttpServletResponse handleApprovalExpectingFailure(String dataSourceId,
            ApprovalRequest request) throws Exception {
        return dataSourceControllerClient.tryHandleApproval(dataSourceId, request)
                                         .andExpect(status().is4xxClientError())
                                         .andReturn().getResponse();
    }

    private DataSourceIdentity cancelRequest(String dataSourceId) throws Exception {
        return handleApprovalExpectingSuccess(dataSourceId, aCancelRequest());
    }

    private DataSourceIdentity reject(String dataSourceId) throws Exception {
        return handleApprovalExpectingSuccess(dataSourceId, aReject(VALID_REJECT_COMMENT));
    }

    private DataSourceIdentity createDataSourceWithApprovalHistory() throws Exception {
        String dataSourceId = createDataSource().getId();
        DataSourceActionBuilder actionBuilder = aDataSourceAction()
                .withType(DataSourceActionType.IDENTITY_NAME_EDIT.name());

        dataSourceControllerClient.edit(dataSourceId,
                actionBuilder
                        .withNewValue("Star Wars Planets")
                        .withVersion("0.0.1-SNAPSHOT")
                        .build());
        requestApproval(dataSourceId, tceManager());
        approve(dataSourceId);

        dataSourceControllerClient.edit(dataSourceId,
                actionBuilder
                        .withNewValue("Planets of Star Wars")
                        .withVersion("0.0.2-SNAPSHOT")
                        .build(),
                newVersionAction(dataSourceId, "0.0.2-SNAPSHOT"));
        requestApproval(dataSourceId, tceManager());
        reject(dataSourceId);

        dataSourceControllerClient.edit(dataSourceId,
                actionBuilder
                        .withNewValue("The Star Wars Planets")
                        .withVersion("0.0.2-SNAPSHOT")
                        .build(),
                newVersionAction(dataSourceId, "0.0.2-SNAPSHOT"));
        requestApproval(dataSourceId, tceManager());
        approve(dataSourceId);

        dataSourceControllerClient.edit(dataSourceId,
                actionBuilder
                        .withNewValue("SWP")
                        .withVersion("0.0.3-SNAPSHOT")
                        .build(),
                newVersionAction(dataSourceId, "0.0.3-SNAPSHOT"));
        return requestApproval(dataSourceId, tceManager());
    }

    @Test
    public void testUnApproveApproved() throws Exception {
        dataSourceControllerClient.login(new UserCredentials(TAF_USER, "taf"));
        DataSourceIdentity dataSourceIdentity = createDataSourceWithApprovalHistory();
        approve(dataSourceIdentity.getId());

        unapprove(dataSourceIdentity.getId());
        DataSourceIdentity unapproved = dataSourceControllerClient.getById(dataSourceIdentity.getId());
        assertThat(unapproved.getVersion()).isEqualTo("0.0.3-SNAPSHOT");
    }
}
