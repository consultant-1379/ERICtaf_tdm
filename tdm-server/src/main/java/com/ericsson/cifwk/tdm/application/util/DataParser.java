package com.ericsson.cifwk.tdm.application.util;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URL;

public final class DataParser {

    private DataParser() {
    }

    public static String readTextFile(String resourceName) {
        try {
            URL url = Resources.getResource(resourceName);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Error loading file", e); // NOSONAR
        }
    }
}
