package com.ericsson.cifwk.tdm.view;

import static org.slf4j.LoggerFactory.getLogger;

import org.slf4j.Logger;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

public class Dialogs extends GenericViewModel {

    private static final Logger LOGGER = getLogger(Dialogs.class);

    @UiComponentMapping(selector = ".confirmModal")
    private ConfirmModalDialog confirmModalDialog;

    @UiComponentMapping(".toast")
    private UiComponent toast;

    public void confirmDialog() {
        waitUntilComponentIsDisplayed(confirmModalDialog);
        confirmModalDialog.confirm();
        waitUntilComponentIsHidden(confirmModalDialog);
    }

    public void closeToast() {
        waitUntilComponentIsDisplayed(toast);
        toast.click();
        waitUntilComponentIsHidden(toast);
    }

    public boolean verifyToastContains(final String text){
        waitUntilComponentIsDisplayed(toast);
        final String message = toast.getText();
        LOGGER.info("Toast message is: {}", message);
        return message.contains(text);
    }
}
