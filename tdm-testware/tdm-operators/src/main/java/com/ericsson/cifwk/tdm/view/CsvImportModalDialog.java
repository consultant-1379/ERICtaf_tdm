package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;

import java.io.File;

/**
 * Created by eniakel on 20/05/2016.
 */
public class CsvImportModalDialog extends GenericViewModel {

    @UiComponentMapping("#file")
    private UiComponent fileSelectionInput;

    @UiComponentMapping("#addButton")
    private Button addFileButton;

    public void setImportedFile(File file) {
        fileSelectionInput.sendKeys(file.getAbsolutePath());
    }

    public void clickAddFileButton() {
        addFileButton.click();
    }
}
