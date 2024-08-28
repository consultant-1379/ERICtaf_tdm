package com.ericsson.cifwk.tdm.application.user;

import com.ericsson.cifwk.tdm.integration.CustomRestClient;
import com.ericsson.gic.tms.presentation.dto.users.UserBean;
import com.ericsson.gic.tms.presentation.resources.ContextResource;
import com.ericsson.gic.tms.presentation.resources.UserResource;
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
import static com.ericsson.gic.tms.presentation.dto.common.SortingMode.ASC;
import static java.util.Collections.emptyList;

@Repository
@CacheConfig(cacheNames = "contextUsers")
public class TceUserRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(TceUserRepository.class);

    @Autowired
    @Qualifier(TCE_CLIENT)
    private CustomRestClient customRestClient;

    @Cacheable(unless = "#result.isEmpty()")
    @HystrixCommand(groupKey = GROUP_KEY_TEST_CASE_EDITOR, fallbackMethod = "contextFallback",
            commandProperties = @HystrixProperty(name = HYSTRIX_TIMEOUT, value = TIMEOUT_MEDIUM))
    public List<UserBean> findByContext(String contextId) {
        LOGGER.info("load users details by [{}] context", contextId);
        return customRestClient.get(ContextResource.class).getContextUserRoles(contextId, ASC, true).unwrap();
    }

    @Cacheable
    @HystrixCommand(groupKey = GROUP_KEY_TEST_CASE_EDITOR, fallbackMethod = "usernameFallback",
            commandProperties = @HystrixProperty(name = HYSTRIX_TIMEOUT, value = TIMEOUT_MEDIUM))
    public UserBean findByUsername(String username) {
        LOGGER.info("load user details by [{}] username", username);
        return customRestClient.get(UserResource.class).findUser(username).unwrap();
    }

    @SuppressWarnings("unused")
    public UserBean usernameFallback(String username) {
        LOGGER.warn("User was not found by username [{}] from the test context editor application", username);
        UserBean userBean = new UserBean();
        userBean.setUsername(username);
        userBean.setRoles(emptyList());
        return userBean;
    }

    @SuppressWarnings("unused")
    public List<UserBean> contextFallback(String contextId) {
        LOGGER.warn("Context was not found for context id [{}] from the test context editor application", contextId);
        return emptyList();
    }
}
