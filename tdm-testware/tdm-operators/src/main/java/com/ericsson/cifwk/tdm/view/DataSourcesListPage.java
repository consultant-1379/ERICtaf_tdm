package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponent;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;
import com.ericsson.cifwk.tdm.Timeouts;

import java.util.List;

/**
 * Created by ekonsla on 22/03/2016.
 */
public class DataSourcesListPage extends GenericViewModel {

    public static final String CELL_CONTENTS_CSS_SELECTOR = ".ui-grid-cell-contents";
    @UiComponentMapping("#createDatasourceButton")
    private Button createDataSourceButton;

    @UiComponentMapping("#signOutButton")
    private Link signOutButton;

    @UiComponentMapping(".navbar-brand")
    private Link home;

    @UiComponentMapping(".ui-grid-header-canvas")
    private DataSourceListHeader header;

    @UiComponentMapping(".ui-grid-row")
    private List<DataSourceListRow> rows;

    @UiComponentMapping("#deleteButton")
    private Button deleteButton;

    @UiComponentMapping("#viewButton")
    private Button viewButton;

    @UiComponentMapping("#editButton")
    private Button editButton;

    @Override
    public boolean isCurrentView() {
        return signOutButton.isDisplayed();
    }

    public void waitUntilPageLoaded() {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return isCurrentView();
            }
        }, Timeouts.MEDIUM_TIMEOUT);
    }

    public void goHome(){
        home.click();
    }

    public boolean isDataSourceInList(String context, String group, String dataSourceName) {
        for (UiComponent row : rows) {
            List<UiComponent> cells = row.getDescendantsBySelector(CELL_CONTENTS_CSS_SELECTOR);
            if (verifyCellHeaders(cells, context, group, dataSourceName)) {
                return true;
            }
        }
        return false;
    }

    public int getNumberOfDataSourcesInList(){
        return rows.size();
    }

    public boolean isDataSourceAddedToList(String context, String group, String dataSourceName) {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return !rows.isEmpty();
            }
        }, Timeouts.LONG_TIMEOUT);
        int numberOfRows = rows.size();
        UiComponent row = rows.get(numberOfRows - 1);
        List<UiComponent> cells = row.getDescendantsBySelector(CELL_CONTENTS_CSS_SELECTOR);
        return verifyCellHeaders(cells, context, group, dataSourceName);
    }

    public void clickCreateDataSourceButton() {
        createDataSourceButton.click();
    }

    private boolean verifyCellHeaders(List<UiComponent> cells, String context, String group, String dataSourceName) {
        waitUntil(new GenericPredicate() {
            @Override
            public boolean apply() {
                return !cells.isEmpty();
            }
        }, Timeouts.LONG_TIMEOUT);

        return cells.get(header.getColumnIndex("Name")).getText().equalsIgnoreCase(dataSourceName) &&
                cells.get(header.getColumnIndex("Group")).getText().equalsIgnoreCase(group) &&
                cells.get(header.getColumnIndex("Context")).getText().equalsIgnoreCase(context);
    }

    public void openDatasource(final String dataSourceName) {
        selectRowApplyAction(dataSourceName, viewButton);
    }

    public void deleteDatasource(final String dataSourceName){
        selectRowApplyAction(dataSourceName, deleteButton);
    }

    public void editDatasource(final String dataSourceName) {
        selectRowApplyAction(dataSourceName, editButton);
    }

    private void selectRowApplyAction(final String dataSourceName, final Button button) {
        for (DataSourceListRow row : rows) {
            if (row.getCellByIndex(0).equalsIgnoreCase(dataSourceName))
                row.click();
        }
        waitUntilComponentIsDisplayed(button);
        button.click();
    }

    public void waitUntilNewButtonIsLoaded() {
        waitUntilComponentIsDisplayed(createDataSourceButton);
    }
}
