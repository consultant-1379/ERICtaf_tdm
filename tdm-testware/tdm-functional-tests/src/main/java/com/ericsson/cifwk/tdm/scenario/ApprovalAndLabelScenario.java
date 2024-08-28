package com.ericsson.cifwk.tdm.scenario;

import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.APPROVER;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.DATA_SOURCE_NAME;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.EXPECTED_APPROVAL_TOAST_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.EXPECTED_LABEL_TOAST_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.EXPECTED_REJECTED_TOAST_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.EXPECTED_REQUEST_TOAST_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.EXPECTED_UNAPPROVAL_TOAST_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.LABEL;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.UN_APPROVAL_MESSAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.APPROVE_DATASOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.CHANGE_TO_LATEST_APPROVED_VERSION;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.EDIT_DATASOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.EDIT_LABEL;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.GO_TO_LIST_PAGE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.OPEN_DATASOURCE_BY_NAME;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.REJECT_DATASOURCE;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.REQUEST_APPROVAL;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.UNAPPROVE_DATASOURCE;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.Parameters.PASSWORD;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.Parameters.USERNAME;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.StepIds.LOGIN_WITH_USER;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.StepIds.LOGOUT;
import static com.ericsson.de.scenariorx.api.RxApi.flow;
import static com.ericsson.de.scenariorx.api.RxApi.runner;
import static com.ericsson.de.scenariorx.api.RxApi.scenario;
import static com.ericsson.de.scenariorx.api.TafRxScenarios.annotatedMethod;

import javax.inject.Inject;
import java.util.Locale;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.tdm.HostResolver;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps;
import com.ericsson.cifwk.tdm.steps.LoginTestSteps;
import com.ericsson.de.scenariorx.api.RxFlow;
import com.ericsson.de.scenariorx.api.RxScenario;
import com.ericsson.de.scenariorx.api.RxTestStep;

public class ApprovalAndLabelScenario extends TafTestBase {

    public static final String APPROVED_DS = "Approved data source";
    public static final String STAR_WARS_DS = "Star wars planets";
    public static final String UNAPPROVED_DS = "Unapproved data source";
    public static final String MY_LABEL = "ethomev";
    public static final String EMPTY_LABEL = "";
    public static final String LABEL_HAS_BEEN_SAVED = "Label has been saved";
    public static final String FAILED_TO_SAVE_LABEL = "Failed to save label";
    public static final String LABEL_HAS_BEEN_DELETED = "Label has been deleted";
    public static final String APPROVAL_STATUS_SUCCESSFULLY_UPDATED = "Approval status successfully updated";
    public static final String USER_1 = "taf";
    public static final String USER_2 = "TAF2";
    public static final String WOOKIE_LABEL = "wookie";

    @Inject
    private TDMOperator operator;

    @Inject
    private LoginTestSteps loginTestSteps;

    @Inject
    private DataSourceViewTestSteps dataSourceViewTestSteps;

    @BeforeMethod
    public void setUp() {
        operator.init(HostResolver.resolve());
    }

    @Test
    @TestId(id = "TAF_TDM_010")
    public void approvalAndLabelTest() {
        runner()
                .withDebugLogEnabled()
                .build()
                .run(approvalAndLabelScenario());
    }

    public RxScenario approvalAndLabelScenario() {
        return scenario("DataSource Approval and Label Verification Scenario")
                .addFlow(rejectionFlow())
                .addFlow(approvalFlow())
                .addFlow(labelFlow())
                .addFlow(editDataSourceFlow())
                .addFlow(approvalFlow())
                .addFlow(unapprovalFlow())
                .build();
    }

    private RxFlow editDataSourceFlow() {
        return flow("edit datasource to create new snapshot version")
                .addTestStep(login("taf"))
                .addTestStep(goHome())
                .addTestStep(openDataSource(UNAPPROVED_DS))
                .addTestStep(editDataSource())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }

    private RxFlow unapprovalFlow() {
        return flow("Unapproval flow")
                .addTestStep(login("taf"))
                .addTestStep(goHome())
                .addTestStep(openDataSource(UNAPPROVED_DS))
                .addTestStep(changeToLatestApprovedVersion())
                .addTestStep(unapproveDataSource())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }

    private RxTestStep unapproveDataSource() {
        return annotatedMethod(dataSourceViewTestSteps, UNAPPROVE_DATASOURCE)
                .withParameter(EXPECTED_UNAPPROVAL_TOAST_MESSAGE).value(APPROVAL_STATUS_SUCCESSFULLY_UPDATED)
                .withParameter(UN_APPROVAL_MESSAGE).value("wtf");
    }

    private RxFlow labelFlow() {
        return flow("Datasource label flow")
                .addTestStep(login("taf"))
                .addTestStep(changeToLatestApprovedVersion())
                .addTestStep(addLabel(MY_LABEL, LABEL_HAS_BEEN_SAVED))
                .addTestStep(goHome())
                .addTestStep(openDataSource(APPROVED_DS))
                .addTestStep(changeToLatestApprovedVersion())
                .addTestStep(addLabel(MY_LABEL, FAILED_TO_SAVE_LABEL))
                .addTestStep(goHome())
                .addTestStep(openDataSource(STAR_WARS_DS))
                .addTestStep(changeToLatestApprovedVersion())
                .addTestStep(addLabel(MY_LABEL, LABEL_HAS_BEEN_SAVED))
                .addTestStep(goHome()).alwaysRun()
                .addTestStep(openDataSource(UNAPPROVED_DS)).alwaysRun()
                .addTestStep(changeToLatestApprovedVersion()).alwaysRun()
                .addTestStep(addLabel(EMPTY_LABEL, LABEL_HAS_BEEN_DELETED)).alwaysRun()
                .addTestStep(goHome()).alwaysRun()
                .addTestStep(openDataSource(STAR_WARS_DS)).alwaysRun()
                .addTestStep(changeToLatestApprovedVersion()).alwaysRun()
                .addTestStep(addLabel(WOOKIE_LABEL, LABEL_HAS_BEEN_SAVED)).alwaysRun()
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }

    private RxTestStep goHome() {
        return annotatedMethod(dataSourceViewTestSteps, GO_TO_LIST_PAGE);
    }

    private RxTestStep addLabel(final String label, final String expectedToastMessage) {
        return annotatedMethod(dataSourceViewTestSteps, EDIT_LABEL)
                .withParameter(LABEL).value(label)
                .withParameter(EXPECTED_LABEL_TOAST_MESSAGE).value(expectedToastMessage);
    }

    private RxTestStep changeToLatestApprovedVersion(){
        return annotatedMethod(dataSourceViewTestSteps, CHANGE_TO_LATEST_APPROVED_VERSION);
    }

    RxFlow rejectionFlow() {
        return flow("Rejection Flow")
                .addTestStep(login(USER_1))
                .addTestStep(openDataSource(UNAPPROVED_DS))
                .addTestStep(requestDatasourceApproval())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .addTestStep(login(USER_2))
                .addTestStep(rejectDataSource())
                .addTestStep(editDataSource())
                .addTestStep(goHome())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }

    private RxTestStep editDataSource() {
        return annotatedMethod(dataSourceViewTestSteps, EDIT_DATASOURCE);
    }

    private RxTestStep rejectDataSource() {
        return annotatedMethod(dataSourceViewTestSteps, REJECT_DATASOURCE)
                .withParameter(EXPECTED_REJECTED_TOAST_MESSAGE)
                .value(APPROVAL_STATUS_SUCCESSFULLY_UPDATED);
    }

    public RxFlow approvalFlow() {
        return flow("Datasource approval flow")
                .addTestStep(login(USER_1))
                .addTestStep(goHome())
                .addTestStep(openDataSource(UNAPPROVED_DS))
                .addTestStep(requestDatasourceApproval())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .addTestStep(login(USER_2))
                .addTestStep(approveDatasource())
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }

    private RxTestStep approveDatasource() {
        return annotatedMethod(dataSourceViewTestSteps, APPROVE_DATASOURCE)
                .withParameter(EXPECTED_APPROVAL_TOAST_MESSAGE)
                .value(APPROVAL_STATUS_SUCCESSFULLY_UPDATED);
    }

    private RxTestStep requestDatasourceApproval() {
        return annotatedMethod(dataSourceViewTestSteps, REQUEST_APPROVAL)
                .withParameter(APPROVER)
                .value(USER_2)
                .withParameter(EXPECTED_REQUEST_TOAST_MESSAGE)
                .value(APPROVAL_STATUS_SUCCESSFULLY_UPDATED);
    }

    private RxTestStep openDataSource(final String datasourceName) {
        return annotatedMethod(dataSourceViewTestSteps, OPEN_DATASOURCE_BY_NAME)
                .withParameter(DATA_SOURCE_NAME)
                .value(datasourceName);
    }

    private RxTestStep login(final String user) {
        return annotatedMethod(loginTestSteps, LOGIN_WITH_USER)
                .withParameter(USERNAME)
                .value(user)
                .withParameter(PASSWORD)
                .value(user.toLowerCase(Locale.ENGLISH));
    }
}
