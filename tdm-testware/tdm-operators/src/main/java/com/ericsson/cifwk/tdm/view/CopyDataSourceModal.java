package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.Select;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import com.ericsson.cifwk.tdm.Timeouts;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
public class CopyDataSourceModal extends AbstractUiComponent {

    @UiComponentMapping("#dsCopyModal-VersionInput select")
    private Select versionSelect;

    @UiComponentMapping("#dsCopyModal-TitleInput input")
    private TextBox titleInput;

    @UiComponentMapping("#dsCopyModal-ContextInput select")
    private Select contextSelect;

    @UiComponentMapping(".angucomplete-holder")
    private AutoCompleteInputField groupInput;

    @UiComponentMapping("#dsCopyModal-Cancel")
    private Button cancelButton;

    @UiComponentMapping("#dsCopyModal-Copy")
    private Button copyButton;

    public void selectVersion(String i) {
        versionSelect.selectByTitle("ver. " + i);
    }

    public void setTitle(String title) {
        titleInput.setText(title);
    }

    public void selectContext(String context) {
        contextSelect.selectByTitle(context);
    }

    public void setGroup(String group) {
        groupInput.setValue(group);
    }

    public void pressCopy() {
        copyButton.click();
    }

    public void pressCancel() {
        cancelButton.click();
    }

    public void waitUntilInitialized() {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return copyButton.isDisplayed();
            }
        }, Timeouts.SHORT_TIMEOUT);
    }

}
