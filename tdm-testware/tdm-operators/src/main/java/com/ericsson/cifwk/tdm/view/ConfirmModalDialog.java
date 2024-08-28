package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;

public class ConfirmModalDialog extends AbstractUiComponent {

    @UiComponentMapping(selector = "#confirmModal-confirm")
    private Button confirmButton;

    @UiComponentMapping(selector = "#confirmModal-cancel")
    private Button cancelButton;

    public void confirm() {
        confirmButton.click();
    }

    public void cancel() {
        cancelButton.click();
    }
}
