package com.ericsson.cifwk.tdm.flows;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.tdm.steps.ContextTestSteps;
import com.google.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.dataSource;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.tdm.steps.ContextTestSteps.StepIds.*;

/**
 * Created by ekonsla on 15/05/2016.
 */
public class ContextFlows {

    @Inject
    private ContextTestSteps contextsTestSteps;

    public TestStepFlow verifyContextsPresent() {
        return flow("Verify Contexts are present")
                .addSubFlow(flow("Verify Context")
                        .addTestStep(annotatedMethod(contextsTestSteps, OPEN_CONTEXT_MENU))
                        .addTestStep(annotatedMethod(contextsTestSteps, VERIFY_CONTEXT))
                        .addTestStep(annotatedMethod(contextsTestSteps, SELECT_CONTEXT))
                        .withDataSources(dataSource("contexts")))
                .build();
    }
}
