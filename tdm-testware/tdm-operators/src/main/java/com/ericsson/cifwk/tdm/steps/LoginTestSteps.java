package com.ericsson.cifwk.tdm.steps;

import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestStep;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.ui.core.WaitTimedOutException;
import com.ericsson.cifwk.tdm.operators.TDMOperator;
import com.ericsson.cifwk.tdm.view.DataSourcesListPage;
import com.ericsson.cifwk.tdm.view.LoginPage;
import com.ericsson.cifwk.tdm.view.TDMHeaderSection;
import com.google.inject.Inject;

import static com.google.common.truth.Truth.assertThat;

/**
 * Created by ekonsla on 13/05/2016.
 */
public class LoginTestSteps {

    @Inject
    private TDMOperator tdmOperator;

    @TestStep(id = StepIds.LOGIN)
    public void login() {
        Host host = tdmOperator.getHost();
        internalLogin(host.getUser(), host.getPass());
    }

    private void internalLogin(final String username, final String password) {
        LoginPage loginPage = tdmOperator.getLoginPage();

        loginPage.login(username, password);

        loginPage.waitForLoginToComplete();

        assertThat(loginPage.isOnLoginPage()).isFalse();
        final DataSourcesListPage dataSourcesListPage = tdmOperator.getDatasourcesListPage();
        dataSourcesListPage.waitUntilNewButtonIsLoaded();
    }

    @TestStep(id = StepIds.LOGIN_WITH_USER)
    public void loginWithUser(@Input(Parameters.USERNAME) final String username, @Input(Parameters.PASSWORD) final String password) {
        internalLogin(username, password);
    }

    @TestStep(id = StepIds.LOGOUT)
    public void logout() {
        TDMHeaderSection headerSection = tdmOperator.getHeaderSection();

        LoginPage loginPage = tdmOperator.getLoginPage();
        try {
            tdmOperator.closeToast();
        } catch(WaitTimedOutException e) { //NOSONAR
        }

        headerSection.logout();
        loginPage.waitForLogoutToComplete();

        assertThat(loginPage.isOnLoginPage()).isTrue();
    }

    public static final class StepIds {
        public static final String LOGIN = "login";
        public static final String LOGIN_WITH_USER = "loginWithUser";
        public static final String LOGOUT = "logout";

        private StepIds() {}
    }

    public static final class Parameters {
        public static final String USERNAME = "username";
        public static final String PASSWORD = "password";

        private Parameters() {}
    }
}
