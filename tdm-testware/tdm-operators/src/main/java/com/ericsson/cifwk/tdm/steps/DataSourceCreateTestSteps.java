package com.ericsson.cifwk.tdm.steps;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.view.DataSourcesCreatePage;
import com.ericsson.cifwk.tdm.view.Dialogs;
import com.google.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by eniakel on 20/05/2016.
 */
@Operator
public class DataSourceCreateTestSteps {

    public static final String OPEN_CREATE_DATASOURCE_VIEW = "openCreateDataSourceView";
    public static final String CREATE_DATA_SOURCE_WITH_CSV_DATA = "createDataSourceWithCsvData";
    public static final String SAVE_DATA_SOURCE = "saveDataSource";
    public static final String VERIFY_TABLE_IS_POPULATED = "verifyTableIsPopulated";
    public static final String VERIFY_NO_ERRORS_DISPLAYED = "verifyNoErrorsDisplayed";

    @Inject
    private TDMOperator tdmOperator;

    @TestStep(id = OPEN_CREATE_DATASOURCE_VIEW)
    public void openDataSourceView() {
        tdmOperator.navigateToCreateDataSourceView();
        final DataSourcesCreatePage page = tdmOperator.getDataSourcesCreatePage();
        page.waitUntilPageIsLoaded();
    }

    @TestStep(id = CREATE_DATA_SOURCE_WITH_CSV_DATA)
    public void createDataSourceWithCsvData(@Input("datasource") String name, @Input("group") String group,
            @Input("csvFilename") final String csvFilename) {
        tdmOperator.enterDataSourceName(name);
        tdmOperator.enterDataSourceGroup(group);
        tdmOperator.importCsvData(csvFilename);
    }

    @TestStep(id = SAVE_DATA_SOURCE)
    public void saveDataSource() {
        tdmOperator.saveDataSource();
        final Dialogs toast = tdmOperator.getDialogs();
        toast.verifyToastContains("Data source successfully created");
        toast.closeToast();
    }

    @TestStep(id = VERIFY_TABLE_IS_POPULATED)
    public void verifyTableIsPopulated(@Input("columnCount") int columnCount,
                                       @Input("rowCount") int rowCount) {
        int actualNumberOfColumns = tdmOperator.getNumberOfColumns();
        assertThat(actualNumberOfColumns).isEqualTo(columnCount);
        int actualNumberOfRows = tdmOperator.getNumberOfRows();
        assertThat(actualNumberOfRows).isEqualTo(rowCount);
    }

    @TestStep(id = VERIFY_NO_ERRORS_DISPLAYED)
    public void verifyNoErrorsDisplayed() {
        boolean errorsExist = tdmOperator.verifyNoImportErrorsDisplayed();
        assertThat(errorsExist).isEqualTo(false);
    }
}
