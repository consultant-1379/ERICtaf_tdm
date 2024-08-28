package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;

public class EditLabel extends AbstractUiComponent{

    @UiComponentMapping(".editable-has-buttons")
    private TextBox labelTextBox;

    @UiComponentMapping(".editable-buttons > button:nth-child(1)")
        private Button save;

    public void setLabel(final String label) {
        labelTextBox.setText(label);
        save.click();
    }
}
