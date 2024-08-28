package com.ericsson.cifwk.tdm.flows;

import com.ericsson.cifwk.taf.scenario.TestStepFlow;
import com.ericsson.cifwk.tdm.steps.LoginTestSteps;
import com.google.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.annotatedMethod;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.flow;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.StepIds.LOGIN;
import static com.ericsson.cifwk.tdm.steps.LoginTestSteps.StepIds.LOGOUT;

/**
 * Created by ekonsla on 06/05/2016.
 */
public class LoginFlows {

    @Inject
    private LoginTestSteps loginTestSteps;

    public TestStepFlow loginFlow() {
        return flow("Login Flow")
                .addTestStep(annotatedMethod(loginTestSteps, LOGIN))
                .build();
    }

    public TestStepFlow logoutFlow() {
        return flow("Logout Flow")
                .addTestStep(annotatedMethod(loginTestSteps, LOGOUT))
                .build();
    }
}
