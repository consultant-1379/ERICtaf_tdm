package com.ericsson.cifwk.tdm.steps;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.view.DataSourcesListPage;
import com.google.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by ekonsla on 13/05/2016.
 */
public class DataSourceListTestSteps {

    @Inject
    private TDMOperator tdmOperator;

    @TestStep(id = StepIds.VERIFY_DATASOURCE_IS_IN_LIST)
    public void verifyDataSourceIsInList(@Input("context") String context,
                                         @Input("group") String group,
                                         @Input("datasource") String datasource) {
        DataSourcesListPage dataSourcesListPage = tdmOperator.getDatasourcesListPage();
        dataSourcesListPage.waitUntilPageLoaded();
        assertThat(dataSourcesListPage.isDataSourceInList(context, group, datasource)).isTrue();
    }

    @TestStep(id = StepIds.VERIFY_DATASOURCE_IS_ADDED_TO_LIST)
    public void verifyDataSourceIsAddedToList(@Input("context") String context,
                                              @Input("group") String group,
                                              @Input("datasource") String datasource) {
        DataSourcesListPage dataSourcesListPage = tdmOperator.getDatasourcesListPage();
        dataSourcesListPage.waitUntilPageLoaded();
        assertThat(dataSourcesListPage.isDataSourceAddedToList(context, group, datasource)).isTrue();
    }

    @TestStep(id = StepIds.DELETE_DATA_SOURCE)
    public void deleteDataSource(@Input(Parameters.NEW_DATA_SOURCE_NAME) final String newDataSourceName) {
        DataSourcesListPage dataSourcesListPage = tdmOperator.getDatasourcesListPage();
        final int numberOfDataSources = dataSourcesListPage.getNumberOfDataSourcesInList();
        dataSourcesListPage.deleteDatasource(newDataSourceName);
        tdmOperator.confirmDialog();
        final int expectedNumberOfDataSources = dataSourcesListPage.getNumberOfDataSourcesInList();
        assertThat(numberOfDataSources).isGreaterThan(expectedNumberOfDataSources);
    }

    public static final class StepIds {
        public static final String VERIFY_DATASOURCE_IS_IN_LIST = "verifyDataSourceIsInList";
        public static final String VERIFY_DATASOURCE_IS_ADDED_TO_LIST = "verifyDataSourceIsAddedToList";
        public static final String DELETE_DATA_SOURCE = "deleteDataSource";

        private StepIds() {}
    }

    public static final class Parameters {
        private static final String NEW_DATA_SOURCE_NAME = "newDataSourceName";

        private Parameters() {}
    }
}
