package com.ericsson.cifwk.tdm.steps;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.view.ContextAside;
import com.ericsson.cifwk.tdm.view.DataSourcesListPage;
import com.ericsson.cifwk.tdm.view.TDMHeaderSection;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.ericsson.cifwk.tdm.steps.ContextTestSteps.Parameters.CONTEXT_ID;
import static com.ericsson.cifwk.tdm.steps.ContextTestSteps.StepIds.*;
import static com.google.common.truth.Truth.assertThat;

/**
 * @author Alexey Nikolaenko alexey.nikolaenko@ericsson.com
 *         Date: 12/05/2016
 */
@Operator
public class ContextTestSteps {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextTestSteps.class);

    @Inject
    private TDMOperator tdmOperator;

    @TestStep(id = OPEN_CONTEXT_MENU)
    public void openContextMenu() {
        TDMHeaderSection headerSection = tdmOperator.getHeaderSection();
        headerSection.openContextMenu();
    }

    @TestStep(id = CLOSE_CONTEXT_MENU)
    public void closeContextMenu() {
        LOGGER.info("Closing context menu");
        ContextAside contextAside = tdmOperator.getContextAside();
        contextAside.waitContextIsDisplayed();
        contextAside.close();
        contextAside.waitContextIsClosed();
    }

    @TestStep(id = NAVIGATE_TO_CONTEXT)
    public void navigateToContext(@Input(Parameters.CONTEXT) String context) {
        tdmOperator.navigateToContext(context);
    }

    @TestStep(id = VERIFY_CONTEXT)
    public void verifyContext(@Input(Parameters.CONTEXT) String context) {
        ContextAside contextAside = tdmOperator.getContextAside();
        contextAside.waitContextIsDisplayed();
        assertThat(contextAside.isNodePresent(context)).isTrue();
    }

    @TestStep(id = SELECT_CONTEXT)
    public void selectContext(@Input(Parameters.CONTEXT) final String context, @Input(CONTEXT_ID) final String contextId,
            @Output(Parameters.NUMBER_OF_DATA_SOURCES) final int numberOfDataSources) {
        final ContextAside contextAside = tdmOperator.getContextAside();
        contextAside.selectContext(contextId);
        contextAside.waitContextIsClosed();
        final TDMHeaderSection headerSection = tdmOperator.getHeaderSection();
        assertThat(headerSection.getSelectedContextFromBreadcrumb()).contains(context);
        final DataSourcesListPage page = tdmOperator.getDatasourcesListPage();
        assertThat(page.getNumberOfDataSourcesInList()).isEqualTo(numberOfDataSources);
    }

    public static final class StepIds {

        public static final String OPEN_CONTEXT_MENU = "openContextMenu";
        public static final String CLOSE_CONTEXT_MENU = "closeContextMenu";
        public static final String NAVIGATE_TO_CONTEXT = "navigateToContext";
        public static final String VERIFY_CONTEXT = "verifyContext";
        public static final String SELECT_CONTEXT = "selectContext";

        private StepIds() {}
    }

    public static final class Parameters {
        public static final String CONTEXT = "context";
        public static final String CONTEXT_ID = "contextId";
        public static final String NUMBER_OF_DATA_SOURCES = "numberOfDataSources";

        private Parameters() {}
    }
}
