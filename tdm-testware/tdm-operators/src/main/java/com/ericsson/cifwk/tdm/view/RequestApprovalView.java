package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;

public class RequestApprovalView extends AbstractUiComponent{

    @UiComponentMapping(".input")
    private TextBox approverTextField;

    @UiComponentMapping("#dsRequestApproval-Send")
    private Button makeRequest;

    @UiComponentMapping(".suggestion-item")
    private UiComponent suggestion;

    public void sendRequest(final String approver) {
        approverTextField.setText(approver);
        waitUntil(suggestion, UiComponent::isDisplayed);
        waitUntil(suggestion, input -> input.getText().contains(approver));
        suggestion.click();
        makeRequest.click();
    }
}
