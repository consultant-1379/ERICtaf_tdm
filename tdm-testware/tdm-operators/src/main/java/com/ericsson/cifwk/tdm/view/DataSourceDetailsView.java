package com.ericsson.cifwk.tdm.view;

import com.beust.jcommander.internal.Sets;
import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.Label;
import com.ericsson.cifwk.taf.ui.sdk.Option;
import com.ericsson.cifwk.taf.ui.sdk.Select;

import java.util.List;
import java.util.Set;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
public class DataSourceDetailsView extends AbstractUiComponent {

    @UiComponentMapping(".dsForm-Name")
    private UiComponent dataSourceName;

    @UiComponentMapping("#copyDatasourceButton")
    private Button copyDataSourceButton;

    @UiComponentMapping("#editDatasourceButton")
    private Button editDataSourceButton;

    @UiComponentMapping("#dataSourceVersionSelect")
    private Select versionSelect;

    @UiComponentMapping("#groupId")
    private Label groupId;

    @UiComponentMapping("#dsPending")
    private Label pendingApprovalStatus;

    @UiComponentMapping("#dsRejected")
    private Label rejectedApprovalStatus;

    @UiComponentMapping("#dsUnapproved")
    private Label unapprovedApprovalStatus;

    @UiComponentMapping("#dsApproved")
    private Label approvedApprovalStatus;

    @UiComponentMapping("#dataSourceContext")
    private Label dataSourceContext;

    public String getDataSourceName() {
        return dataSourceName.getText();
    }

    public String getDataSourceGroup() {
        return groupId.getText();
    }

    public void copyDataSource() {
        copyDataSourceButton.click();
    }

    public void editDataSource() {
        editDataSourceButton.click();
    }

    public void selectVersion(int i) {
        versionSelect.selectByTitle("ver. " + i);
    }

    public Set<String> getVersionCount() {
        List<Option> allOptions = versionSelect.getAllOptions();
        Set<String> versions = Sets.newHashSet();
        for (Option allOption : allOptions) {
            versions.add(allOption.getTitle().replace("ver. ", ""));
        }
        return versions;
    }

    public String getApprovalStatus() {
        String approvalStatus = "";
        if(pendingApprovalStatus.isDisplayed()) {
            approvalStatus = pendingApprovalStatus.getText();
        } else if(rejectedApprovalStatus.isDisplayed()) {
            approvalStatus = rejectedApprovalStatus.getText();
        } else if(unapprovedApprovalStatus.isDisplayed()){
            approvalStatus = unapprovedApprovalStatus.getText();
        } else if(approvedApprovalStatus.isDisplayed()){
            approvalStatus = approvedApprovalStatus.getText();
        }
        return approvalStatus;
    }

    public String getSelectedVersion() {
        return versionSelect.getText();
    }

    public String getContext() {
        return dataSourceContext.getText();
    }
}
