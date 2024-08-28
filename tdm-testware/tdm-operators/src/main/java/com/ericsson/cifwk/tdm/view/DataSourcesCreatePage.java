package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import com.ericsson.cifwk.tdm.Timeouts;

import java.util.List;

/**
 * Created by eniakel on 20/05/2016.
 */
public class DataSourcesCreatePage extends GenericViewModel {

    @UiComponentMapping(".modal-dialog")
    private UiComponent importCsvDialog;

    @UiComponentMapping(".dsForm-Name")
    private TextBox dataSourceNameField;

    @UiComponentMapping(".dsForm-GroupInput")
    private TextBox dataSourceGroupField;

    @UiComponentMapping("#csvImportButton")
    private UiComponent csvImportButton;

    @UiComponentMapping("#saveButton")
    private Button saveButton;

    @UiComponentMapping(".ui-grid-header-cell-wrapper")
    private List<UiComponent> tableHeaders;

    @UiComponentMapping(".ui-grid-canvas")
    private UiComponent tableData;

    @UiComponentMapping(".form-group")
    private UiComponent groupForm;

    @UiComponentMapping("#dsGrid-errorAlert")
    private UiComponent errorBlock;

    public void setDataSourceName(String name) {
        dataSourceNameField.setText(name);
    }

    public void setDataSourceGroup(String group) {
        dataSourceGroupField.setText(group);
        groupForm.click();
    }

    public void clickImportCsvButton() {
        csvImportButton.click();
    }

    public void waitUntilImportCsvButtonIsHidden(){
        waitUntilComponentIsHidden(csvImportButton);
    }

    public void clickSaveButton() {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return !isCsvDialogDisplayed();
            }
        }, Timeouts.MEDIUM_TIMEOUT);
        saveButton.click();
    }

    public void waitUntilPageIsLoaded(){
        waitUntilComponentIsDisplayed(saveButton);
    }

    public int getNumberOfTableColumns() {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return tableHeaders.get(1).isDisplayed();
            }
        }, Timeouts.LONG_TIMEOUT);
        return tableHeaders.get(1).getDescendantsBySelector(".ui-grid-header-cell").size();
    }

    public int getNumberTableRows() {
        return tableData.getDescendantsBySelector(".ui-grid-row").size();
    }

    public boolean isCsvDialogDisplayed() {
        return importCsvDialog.isDisplayed();
    }

    public boolean isErrorBlockDisplayed() {
        return errorBlock.isDisplayed();
    }

}
