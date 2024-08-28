package com.ericsson.cifwk.tdm.steps;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.view.CopyDataSourceModal;
import com.ericsson.cifwk.tdm.view.DataSourceDetailsView;
import com.ericsson.cifwk.tdm.view.DataSourceEditPage;
import com.ericsson.cifwk.tdm.view.DataSourcesListPage;
import com.ericsson.cifwk.tdm.view.DatasourceViewPage;
import com.ericsson.cifwk.tdm.view.Dialogs;
import com.google.inject.Inject;

import java.util.List;
import java.util.Set;

import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.Parameters.*;
import static com.ericsson.cifwk.tdm.steps.DataSourceViewTestSteps.StepIds.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
@Operator
public class DataSourceViewTestSteps {

    private static final String SNAPSHOT = "snapshot";
    @Inject
    private TDMOperator tdmOperator;

    @TestStep(id = OPEN_DATASOURCE_BY_NAME)
    public void openDatasourceByName(@Input(DATA_SOURCE_NAME) final String dataSourceName) {
        tdmOperator.openDataSource(dataSourceName);
        DatasourceViewPage viewPage = tdmOperator.getDatasourceViewPage();
        assertThat(viewPage.isDataSourceName(dataSourceName));
    }
    
    @TestStep(id = EDIT_DATASOURCE)
    public void editDataSource() {
        DatasourceViewPage page = tdmOperator.getDatasourceViewPage();
        page.getDataSourceDetailsView().editDataSource();
        DataSourceEditPage editPage = tdmOperator.getDataSourceEditPAge();
        editPage.saveChanges();
        page.waitUntilPageLoaded();
        assertThat(page.getDataSourceDetailsView().getApprovalStatus()).isEqualTo("Unapproved");
        assertThat(page.isRequestApprovalButtonDisplayed()).isTrue();
        assertThat(page.getDataSourceDetailsView().getSelectedVersion().toLowerCase())
                .contains(SNAPSHOT);
    }

    @TestStep(id = COPY_DATASOURCE)
    public void copyDataSource(@Input(NEW_DATA_SOURCE_NAME) String newDataSourceName,
            @Input(NEW_DATA_SOURCE_GROUP) String newDataSourceGroup,
            @Input(NEW_DATA_SOURCE_CONTEXT) String newDataSourceContext, @Input(VERSION_TO_COPY) String versionToCopy,
            @Output(EXPECTED_COPY_TOAST_MESSAGE) final String expectedToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        DataSourceDetailsView dataSourceDetailsView = datasourceViewPage.getDataSourceDetailsView();

        dataSourceDetailsView.copyDataSource();


        CopyDataSourceModal copyModal = datasourceViewPage.getCopyModal();
        copyModal.waitUntilInitialized();

        copyModal.setTitle(newDataSourceName);
        copyModal.setGroup(newDataSourceGroup);
        String finalNewDataSourceContext = newDataSourceContext.substring(newDataSourceContext.lastIndexOf("/") + 1);
        copyModal.selectContext(finalNewDataSourceContext);
        copyModal.selectVersion(versionToCopy);

        copyModal.pressCopy();
        Dialogs toast = tdmOperator.getDialogs();
        assertThat(toast.verifyToastContains(expectedToastMessage)).isTrue();
        tdmOperator.closeToast();
    }

    @TestStep(id = VERIFY_DATASOURCE_ATTRIBUTES)
    public void verifyDataSourceAttributes(@Input(DATA_SOURCE_NAME) String dataSourceName,
                                           @Input(DATA_SOURCE_GROUP) String dataSourceGroup,
                                           @Input(DATA_SOURCE_CONTEXT) String dataSourceContext,
                                           @Input(DATA_SOURCE_VERSIONS) List<String> dataSourceVersions,
                                            @Output(NUMBER_OF_DATA_RECORDS) final int numberOfDataRecords) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        DataSourceDetailsView dataSourceDetailsView = datasourceViewPage.getDataSourceDetailsView();

        String displayedDataSourceName = dataSourceDetailsView.getDataSourceName();
        assertThat(displayedDataSourceName).isEqualTo(dataSourceName);

        String displayedDataSourceGroup = dataSourceDetailsView.getDataSourceGroup();
        assertThat(displayedDataSourceGroup).isEqualTo(dataSourceGroup);

        String displayedDataSourceContext = dataSourceDetailsView.getContext();
        assertThat(displayedDataSourceContext).isEqualTo(dataSourceContext);

        Set<String> versionCount = dataSourceDetailsView.getVersionCount();
        assertThat(versionCount).containsExactlyElementsIn(dataSourceVersions);

        assertThat(datasourceViewPage.numberOfDataRecords()).isEqualTo(numberOfDataRecords);
    }

    @TestStep(id = VERIFY_DATASOURCE_COPIED_ATTRIBUTES)
    public void verifyDataSourceCopiedAttributes(@Input(NEW_DATA_SOURCE_NAME) String newDataSourceName,
            @Input(NEW_DATA_SOURCE_GROUP) String newDataSourceGroup,
            @Input(NEW_DATA_SOURCE_CONTEXT) final String dataSourceContext,
            @Output(NUMBER_OF_DATA_RECORDS) final int numberOfDataRecords) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        DataSourceDetailsView dataSourceDetailsView = datasourceViewPage.getDataSourceDetailsView();

        String displayedDataSourceName = dataSourceDetailsView.getDataSourceName();
        assertThat(displayedDataSourceName).isEqualTo(newDataSourceName);

        String displayedDataSourceGroup = dataSourceDetailsView.getDataSourceGroup();
        assertThat(displayedDataSourceGroup).isEqualTo(newDataSourceGroup);

        String displayedDataSourceContext = dataSourceDetailsView.getContext();
        assertThat(displayedDataSourceContext).isEqualTo(dataSourceContext);

        assertThat(datasourceViewPage.numberOfDataRecords()).isEqualTo(numberOfDataRecords);
    }

    @TestStep(id = REQUEST_APPROVAL)
    public void requestApproval(@Input(APPROVER) final String approver, @Output(EXPECTED_REQUEST_TOAST_MESSAGE) final String expectedRequestToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        assertThat(datasourceViewPage.requestApproval(approver)).isTrue();
        Dialogs dialogs = tdmOperator.getDialogs();
        assertThat(dialogs.verifyToastContains(expectedRequestToastMessage)).isTrue();
        dialogs.closeToast();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getApprovalStatus()).isEqualTo("Pending");
    }

    @TestStep(id = APPROVE_DATASOURCE)
    public void approveDatasource(@Output(EXPECTED_APPROVAL_TOAST_MESSAGE) final String expectedApprovalToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        datasourceViewPage.openReviewPage();
        assertThat(datasourceViewPage.approveDatasource()).isTrue();
        Dialogs dialogs = tdmOperator.getDialogs();
        assertThat(dialogs.verifyToastContains(expectedApprovalToastMessage)).isTrue();
        dialogs.closeToast();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getApprovalStatus()).isEqualTo("Approved");
        assertThat(datasourceViewPage.isUnapprovedButtonDisplayed()).isTrue();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getSelectedVersion().toLowerCase())
                .doesNotContain(SNAPSHOT);
    }

    @TestStep(id = REJECT_DATASOURCE)
    public void rejectDatasource(@Output(EXPECTED_REJECTED_TOAST_MESSAGE) final String expectedRejectedToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        datasourceViewPage.openReviewPage();
        assertThat(datasourceViewPage.rejectDatasource()).isTrue();
        Dialogs dialogs = tdmOperator.getDialogs();
        assertThat(dialogs.verifyToastContains(expectedRejectedToastMessage)).isTrue();
        dialogs.closeToast();
        assertThat(datasourceViewPage.isRequestApprovalButtonDisplayed()).isFalse();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getApprovalStatus()).isEqualTo("Rejected");
    }

    @TestStep(id = UNAPPROVE_DATASOURCE)
    public void unapproveDatasource(@Input(UN_APPROVAL_MESSAGE) final String unApprovalMessage,
            @Output(EXPECTED_UNAPPROVAL_TOAST_MESSAGE) final String expectedUnapprovalToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        assertThat(datasourceViewPage.unApproveDatasource(unApprovalMessage)).isTrue();
        Dialogs dialogs = tdmOperator.getDialogs();
        assertThat(dialogs.verifyToastContains(expectedUnapprovalToastMessage)).isTrue();
        dialogs.closeToast();
        assertThat(datasourceViewPage.isRequestApprovalButtonDisplayed()).isTrue();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getApprovalStatus()).isEqualTo("Unapproved");
        assertThat(datasourceViewPage.getDataSourceDetailsView().getSelectedVersion().toLowerCase())
                .contains(SNAPSHOT);
    }

    @TestStep(id = CHANGE_VERSION)
    public void changeVersion(@Input(VERSION) final String version){
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        assertThat(datasourceViewPage.changeVersionTo(version)).isTrue();
    }

    @TestStep(id = CHANGE_TO_LATEST_APPROVED_VERSION)
    public void changeToLatestApprovedVersion() {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        assertThat(datasourceViewPage.changeToLatestApprovedVersion()).isTrue();
        assertThat(datasourceViewPage.getDataSourceDetailsView().getSelectedVersion().toLowerCase())
                .doesNotContain(SNAPSHOT);
        assertThat(datasourceViewPage.isUnapprovedButtonDisplayed()).isTrue();
    }

    @TestStep(id = EDIT_LABEL)
    public void editLabel(@Input(LABEL) final String label, @Output(EXPECTED_LABEL_TOAST_MESSAGE) final String expectedLabelToastMessage) {
        DatasourceViewPage datasourceViewPage = tdmOperator.getDatasourceViewPage();
        datasourceViewPage.editLabel(label);
        Dialogs dialogs = tdmOperator.getDialogs();
        assertThat(dialogs.verifyToastContains(expectedLabelToastMessage)).isTrue();
        dialogs.closeToast();
    }

    @TestStep(id = GO_TO_LIST_PAGE)
    public void goToListPage() {
        tdmOperator.goToHomePage();
        final DataSourcesListPage datasourcesListPage = tdmOperator.getDatasourcesListPage();
        datasourcesListPage.waitUntilPageLoaded();
    }

    public static final class StepIds {
        public static final String COPY_DATASOURCE = "copyDataSource";
        public static final String VERIFY_DATASOURCE_ATTRIBUTES = "verifyDataSourceAttributes";
        public static final String VERIFY_DATASOURCE_COPIED_ATTRIBUTES = "verifyDataSourceCopiedAttributes";
        public static final String REQUEST_APPROVAL = "requestApproval";
        public static final String APPROVE_DATASOURCE = "approveDatasource";
        public static final String REJECT_DATASOURCE = "rejectDatasource";
        public static final String OPEN_DATASOURCE_BY_NAME = "openDatasourceByName";
        public static final String CHANGE_VERSION = "changeVersion";
        public static final String CHANGE_TO_LATEST_APPROVED_VERSION = "changeToLatestApprovedVersion";
        public static final String EDIT_LABEL = "editLabel";
        public static final String GO_TO_LIST_PAGE = "goToListPage";
        public static final String UNAPPROVE_DATASOURCE = "unapproveDatasource";
        public static final String EDIT_DATASOURCE = "editDataSource";

        private StepIds() {}
    }

    public static final class Parameters {
        public static final String DATA_SOURCE_NAME = "dataSourceName";
        public static final String NEW_DATA_SOURCE_NAME = "newDataSourceName";
        public static final String NEW_DATA_SOURCE_GROUP = "newDataSourceGroup";
        public static final String NEW_DATA_SOURCE_CONTEXT = "newDataSourceContext";
        public static final String VERSION_TO_COPY = "versionToCopy";
        public static final String DATA_SOURCE_GROUP = "dataSourceGroup";
        public static final String DATA_SOURCE_VERSIONS = "dataSourceVersions";
        public static final String APPROVER = "approver";
        public static final String EXPECTED_REQUEST_TOAST_MESSAGE = "expectedRequestToastMessage";
        public static final String EXPECTED_APPROVAL_TOAST_MESSAGE = "expectedApprovalToastMessage";
        public static final String EXPECTED_REJECTED_TOAST_MESSAGE = "expectedRejectedToastMessage";
        public static final String VERSION = "version";
        public static final String LABEL = "label";
        public static final String EXPECTED_LABEL_TOAST_MESSAGE = "expectedLabelToastMessage";
        public static final String EXPECTED_UNAPPROVAL_TOAST_MESSAGE = "expectedUnapprovalToastMessage";
        public static final String UN_APPROVAL_MESSAGE = "unApprovalMessage";
        public static final String EXPECTED_COPY_TOAST_MESSAGE = "expectedCopyToastMessage";
        public static final String DATA_SOURCE_CONTEXT = "dataSourceContext";
        public static final String NUMBER_OF_DATA_RECORDS = "numberOfDataRecords";

        private Parameters() {}
    }
}
