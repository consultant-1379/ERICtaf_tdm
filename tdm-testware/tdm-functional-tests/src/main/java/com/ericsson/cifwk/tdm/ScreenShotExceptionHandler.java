package com.ericsson.cifwk.tdm;

import com.ericsson.cifwk.taf.scenario.api.ScenarioExceptionHandler;
import com.ericsson.cifwk.taf.ui.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ekonsla on 06/06/2016.
 */
public class ScreenShotExceptionHandler implements ScenarioExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenShotExceptionHandler.class);

    private Browser browser;

    public ScreenShotExceptionHandler(Browser browser) {
        this.browser = browser;
    }

    @Override
    public Outcome onException(Throwable e) {
        browser.getCurrentWindow().takeScreenshot("exception.png");
        LOGGER.info("Screenshot taken in ScreenShotExceptionHandler", e);
        return Outcome.PROPAGATE_EXCEPTION;
    }
}
