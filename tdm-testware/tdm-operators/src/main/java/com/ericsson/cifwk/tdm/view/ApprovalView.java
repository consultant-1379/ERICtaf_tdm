package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;

public class ApprovalView extends AbstractUiComponent{

    @UiComponentMapping("#dsRequestApproval-Approve")
    private Button approveButton;

    @UiComponentMapping("#dsRequestApproval-UnApprove")
    private Button unApproveButton;

    @UiComponentMapping("#dsRequestApproval-Reject")
    private Button rejectButton;

    @UiComponentMapping(".ng-valid")
    private TextBox unApprovalTextBox;

    public void approve() {
        approveButton.click();
    }

    public void unApprove() {
        unApproveButton.click();
    }

    public void reject() {rejectButton.click(); }

    public void enterUnApprovalMessage(final String unApprovalMessage) {
        unApprovalTextBox.setText(unApprovalMessage);
    }
}
