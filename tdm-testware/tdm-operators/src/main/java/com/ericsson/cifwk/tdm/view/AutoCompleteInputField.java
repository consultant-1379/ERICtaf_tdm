package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.AbstractUiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
public class AutoCompleteInputField extends AbstractUiComponent {

    @UiComponentMapping("input")
    private TextBox input;

    public String getValue() {
        return input.getText();
    }

    public void setValue(String value) {
        input.setText(value);
    }
}
