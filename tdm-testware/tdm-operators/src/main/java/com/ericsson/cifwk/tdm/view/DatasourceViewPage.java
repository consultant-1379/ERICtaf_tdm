package com.ericsson.cifwk.tdm.view;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.slf4j.Logger;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Label;
import com.ericsson.cifwk.taf.ui.sdk.Option;
import com.ericsson.cifwk.taf.ui.sdk.Select;
import com.ericsson.cifwk.tdm.Timeouts;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
public class DatasourceViewPage extends GenericViewModel {

    private static final Logger LOGGER = getLogger(DatasourceViewPage.class);

    @UiComponentMapping(".dsView-Details")
    private DataSourceDetailsView dataSourceDetailsView;

    @UiComponentMapping(".dsCopyModal")
    private CopyDataSourceModal copyModal;

    @UiComponentMapping("#requestApprovalButton")
    private Button requestApproval;

    @UiComponentMapping(".dsRequestApproval")
    private RequestApprovalView requestApprovalView;

    @UiComponentMapping(".dsCommentApproval")
    private ApprovalView approvalView;

    @UiComponentMapping("#approveButton")
    private Button approveButton;

    @UiComponentMapping("#rejectButton")
    private Button rejectButton;

    @UiComponentMapping("#unApproveButton")
    private Button unApproveButton;

    @UiComponentMapping(".dsForm-Name")
    private Label dataSourceName;

    @UiComponentMapping("#dataSourceVersionSelect")
    private Select dataSourceVersionSelect;

    @UiComponentMapping(".popover-wrapper")
    private Button labelButton;

    @UiComponentMapping(".editable-wrap")
    private EditLabel editLabel;

    @UiComponentMapping("#reviewButton")
    private Button reviewButton;

    @UiComponentMapping(".dsGrid-Table")
    private DataRecordsTable dataRecordsTable;

    @Override
    public boolean isCurrentView() {
        return dataSourceDetailsView.isDisplayed();
    }

    public int numberOfDataRecords(){
        return dataRecordsTable.getDataRecords().size();
    }

    public void waitUntilPageLoaded() {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return isCurrentView();
            }
        }, Timeouts.MEDIUM_TIMEOUT);
    }

    public DataSourceDetailsView getDataSourceDetailsView() {
        return dataSourceDetailsView;
    }

    public CopyDataSourceModal getCopyModal() {
        return copyModal;
    }

    public boolean requestApproval(final String approver) {
        requestApproval.click();
        waitUntilComponentIsDisplayed(requestApprovalView);
        requestApprovalView.sendRequest(approver);
        waitUntilComponentIsHidden(requestApprovalView);
        return true;
    }

    public void openReviewPage() {
        reviewButton.click();
        waitUntilComponentIsDisplayed(approveButton);
    }

    public boolean approveDatasource() {
        approveButton.click();
        waitUntilComponentIsDisplayed(approvalView);
        approvalView.approve();
        waitUntilComponentIsHidden(approvalView);
        return true;
    }

    public boolean rejectDatasource() {
        rejectButton.click();
        waitUntilComponentIsDisplayed(approvalView);
        approvalView.enterUnApprovalMessage("rejected");
        approvalView.reject();
        waitUntilComponentIsHidden(approvalView);
        return true;
    }

    public boolean isRequestApprovalButtonDisplayed(){
        return requestApproval.isDisplayed();
    }

    public boolean isUnapprovedButtonDisplayed(){
        return unApproveButton.isDisplayed();
    }

    public boolean unApproveDatasource(final String unApprovalMessage) {
        unApproveButton.click();
        waitUntilComponentIsDisplayed(approvalView);
        approvalView.enterUnApprovalMessage(unApprovalMessage);
        approvalView.unApprove();
        waitUntilComponentIsHidden(approvalView);
        return true;
    }

    public boolean isDataSourceName(final String expectedDataSourceName){
        return dataSourceName.getText().equalsIgnoreCase(expectedDataSourceName);
    }

    public boolean changeVersionTo(final String version) {
        dataSourceVersionSelect.selectByTitle(version);
        return dataSourceVersionSelect.getText().equalsIgnoreCase(version);
    }

    public boolean changeToLatestApprovedVersion() {
        List<Option> allOptions = dataSourceVersionSelect.getAllOptions();
        LOGGER.info("Selecting latest approved version");
        for(Option option: allOptions){
            if(!option.getTitle().contains("-SNAPSHOT")){
                LOGGER.info("Clicking {}", option.getTitle());
                option.click();
                return true;
            }
        }
        return false;
    }

    public boolean editLabel(final String label) {
        labelButton.click();
        waitUntilComponentIsDisplayed(editLabel);
        editLabel.setLabel(label);
        waitUntilComponentIsHidden(editLabel);
        return true;
    }
}
