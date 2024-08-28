package com.ericsson.cifwk.tdm.scenario;

import com.ericsson.cifwk.taf.TafTestBase;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.scenario.TestScenario;
import com.ericsson.cifwk.tdm.HostResolver;
import com.ericsson.cifwk.tdm.ScreenShotExceptionHandler;
import com.ericsson.cifwk.tdm.flows.LoginFlows;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.inject.Inject;

import static com.ericsson.cifwk.taf.scenario.TestScenarios.runner;
import static com.ericsson.cifwk.taf.scenario.TestScenarios.scenario;

public class LoginScenario extends TafTestBase {

    @Inject
    private TDMOperator operator;

    @Inject
    private LoginFlows loginFlows;

    @BeforeMethod
    public void setUp() {
        operator.init(HostResolver.resolve());
    }

    @Test
    @TestId(id = "TAF_TDM_001")
    public void login() {
        TestScenario scenario = scenario("Login")
                .addFlow(loginFlows.loginFlow())
                .addFlow(loginFlows.logoutFlow())
                .build();

        ScreenShotExceptionHandler screenShotExceptionHandler = new ScreenShotExceptionHandler(operator.getBrowser());
        runner().withDefaultExceptionHandler(screenShotExceptionHandler).build().start(scenario);
    }
}
