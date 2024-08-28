package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.TextBox;
import com.ericsson.cifwk.tdm.Timeouts;

/**
 * Created by ekonsla on 21/03/2016.
 */
public class LoginPage extends GenericViewModel {

    @UiComponentMapping(selector = "#username")
    private TextBox userNameInput;

    @UiComponentMapping(selector = "#password")
    private TextBox passwordInput;

    public void login(String userName, String password) {
        userNameInput.setText(userName);
        passwordInput.setText(password);
        passwordInput.sendKeys("\n");
    }

    public boolean isOnLoginPage() {
        return passwordInput.exists();
    }

    public TextBox getPasswordInput() {
        return passwordInput;
    }

    public void waitForLoginToComplete() {
        GenericPredicate predicate = new GenericPredicate() {
            @Override
            public boolean apply() {
                return !isOnLoginPage();
            }
        };
        this.waitUntil(predicate, Timeouts.LONG_TIMEOUT);
    }

    public void waitForLogoutToComplete() {
        GenericPredicate predicate = new GenericPredicate() {
            @Override
            public boolean apply() {
                return isOnLoginPage();
            }
        };
        this.waitUntil(predicate, Timeouts.LONG_TIMEOUT);
    }
}
