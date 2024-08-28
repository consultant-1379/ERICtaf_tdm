package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

public class DataSourceEditPage extends GenericViewModel {

    @UiComponentMapping(".dsForm-ControlButton.btn.btn-success")
    private Button save;

    public void saveChanges(){
        save.click();
    }
}
