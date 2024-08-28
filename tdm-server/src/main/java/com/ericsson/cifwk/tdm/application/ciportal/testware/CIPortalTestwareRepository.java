package com.ericsson.cifwk.tdm.application.ciportal.testware;

import com.ericsson.cifwk.tdm.integration.CustomRestClient;
import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import com.ericsson.cifwk.tdm.resources.TestwareResource;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TESTWARE_GROUPS_CLIENT;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.GROUP_KEY_CI_PORTAL;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.HYSTRIX_TIMEOUT;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.TIMEOUT_LONG;

@Repository
@CacheConfig(cacheNames = "testware")
public class CIPortalTestwareRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(CIPortalTestwareRepository.class);

    @Autowired
    @Qualifier(TESTWARE_GROUPS_CLIENT)
    private CustomRestClient customRestClient;

    @Cacheable(unless = "#result.getArtifacts().isEmpty()")
    @HystrixCommand(groupKey = GROUP_KEY_CI_PORTAL, fallbackMethod = "artifactsFallback",
            commandProperties = @HystrixProperty(name = HYSTRIX_TIMEOUT, value = TIMEOUT_LONG))
    public ArtifactItems getTestwareArtifacts() {
        return customRestClient.get(TestwareResource.class).getTestware();
    }

    @SuppressWarnings("unused")
    public ArtifactItems artifactsFallback() {
        LOGGER.warn("Cannot retrieve test ware from CI Portal");
        return new ArtifactItems();
    }
}
