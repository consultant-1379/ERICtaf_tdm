package com.ericsson.cifwk.tdm.api.model;

import java.util.Locale;

/**
 * Created by egergle on 13/10/2017.
 */
public enum UserAgent {
    BROWSER("Browser"),
    REST("Rest");

    private String name;

    UserAgent(String name) {
        this.name = name;
    }

    public static String getRequestType(String userAgent) {
        String toLowerCase = userAgent.toLowerCase(Locale.UK);
        if (toLowerCase.contains("mozilla") || toLowerCase.contains("chrome")
                || toLowerCase.contains("firefox") || toLowerCase.contains("safari")) {
            return BROWSER.getName();
        } else {
            return REST.getName();
        }
    }

    public String getName() {
        return name;
    }
}
