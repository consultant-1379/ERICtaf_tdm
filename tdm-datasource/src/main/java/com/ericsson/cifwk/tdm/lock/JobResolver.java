package com.ericsson.cifwk.tdm.lock;

import com.ericsson.cifwk.taf.ServiceRegistry;
import com.ericsson.cifwk.taf.configuration.Configuration;

import java.util.UUID;

public final class JobResolver {

    private static final String BUILD_TAG = "BUILD_TAG";
    private static final String TE_EXECUTION_ID = "TE_EXECUTION_ID";

    private JobResolver() {
        // private constructor
    }

    public static String getJobId() {
        Configuration configuration = ServiceRegistry.getConfigurationProvider().get();

        Object teExecutionId = configuration.getProperty(TE_EXECUTION_ID);
        Object buildTag = configuration.getProperty(BUILD_TAG);
        if (teExecutionId != null) {
            return (String) teExecutionId;
        } else if (buildTag != null) {
            return (String) buildTag;
        } else {
            return UUID.randomUUID().toString();
        }
    }
}
