package com.ericsson.cifwk.tdm.view;

import com.ericsson.cifwk.taf.ui.core.GenericPredicate;
import com.ericsson.cifwk.taf.ui.core.SelectorType;
import com.ericsson.cifwk.taf.ui.core.UiComponentMapping;
import com.ericsson.cifwk.taf.ui.sdk.Button;
import com.ericsson.cifwk.taf.ui.sdk.GenericViewModel;
import com.ericsson.cifwk.taf.ui.sdk.Link;
import com.ericsson.cifwk.tdm.Timeouts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * Created by ekonsla on 05/05/2016.
 */
public class ContextAside extends GenericViewModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContextAside.class);

    @UiComponentMapping(selector = "#tms-btn-choose-close")
    private Link asideCloseButton;

    public void close() {
        if (asideCloseButton.isDisplayed()) {
            asideCloseButton.click();
        } else {
            LOGGER.info("Contexts panel is not displayed. Nothing to close.");
        }
    }

    public boolean isNodePresent(String context) {
        String query = format("//*[contains(text(), '%s')]", context);
        return hasComponent(SelectorType.XPATH, query);
    }

    public void waitContextIsDisplayed() {
        GenericPredicate predicate = new GenericPredicate() {
            @Override
            public boolean apply() {
                return asideCloseButton.exists();
            }
        };
        waitUntil(predicate, Timeouts.LONG_TIMEOUT);
    }

    public void waitContextIsClosed() {
        GenericPredicate predicate = new GenericPredicate() {
            @Override
            public boolean apply() {
                return asideCloseButton == null || !asideCloseButton.exists();
            }
        };
        waitUntil(predicate, Timeouts.LONG_TIMEOUT);
    }

    public void selectContext(final String context) {
        final String selector = format("#%s", context);
        waitUntilComponentIsDisplayed(selector);
        final Button contextButton = getViewComponent(selector, Button.class);
        contextButton.click();
    }
}
