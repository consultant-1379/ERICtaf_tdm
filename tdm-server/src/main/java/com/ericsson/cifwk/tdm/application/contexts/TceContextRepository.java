package com.ericsson.cifwk.tdm.application.contexts;

import com.ericsson.cifwk.tdm.integration.CustomRestClient;
import com.ericsson.gic.tms.presentation.dto.ContextBean;
import com.ericsson.gic.tms.presentation.resources.ContextResource;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TCE_CLIENT;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.GROUP_KEY_TEST_CASE_EDITOR;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.HYSTRIX_TIMEOUT;
import static com.ericsson.cifwk.tdm.infrastructure.HystrixConfiguration.TIMEOUT_MEDIUM;
import static java.util.Collections.emptyList;

@Repository
@CacheConfig(cacheNames = TceContextRepository.CACHE_NAME)
public class TceContextRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TceContextRepository.class);

    static final String CACHE_NAME = "contexts";

    @Autowired
    @Qualifier(TCE_CLIENT)
    private CustomRestClient customRestClient;

    @Cacheable(unless = "#result.isEmpty()")
    @HystrixCommand(groupKey = GROUP_KEY_TEST_CASE_EDITOR, fallbackMethod = "contextsFallback",
            commandProperties = @HystrixProperty(name = HYSTRIX_TIMEOUT, value = TIMEOUT_MEDIUM))
    public List<ContextBean> getContexts() {
        return customRestClient.get(ContextResource.class).getContexts().unwrap();
    }

    @SuppressWarnings("unused")
    public List<ContextBean> contextsFallback() {
        LOGGER.warn("Cannot retrieve contexts from the test context editor application");
        return emptyList();
    }
}
