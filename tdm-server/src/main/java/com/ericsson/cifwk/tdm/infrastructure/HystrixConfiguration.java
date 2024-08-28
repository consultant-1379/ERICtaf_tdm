package com.ericsson.cifwk.tdm.infrastructure;

import com.netflix.hystrix.contrib.javanica.aop.aspectj.HystrixCommandAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.netflix.hystrix.contrib.javanica.conf.HystrixPropertiesManager.EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;

/**
 * Created by egergle on 25/05/2017.
 */
@Configuration
public class HystrixConfiguration {

    public static final String GROUP_KEY_CI_PORTAL = "ci-portal";
    public static final String GROUP_KEY_TEST_CASE_EDITOR = "test-context-editor";

    public static final String HYSTRIX_TIMEOUT = EXECUTION_ISOLATION_THREAD_TIMEOUT_IN_MILLISECONDS;

    public static final String TIMEOUT_SHORT = "3000";
    public static final String TIMEOUT_MEDIUM = "5000";
    public static final String TIMEOUT_LONG = "10000";

    @Bean
    public HystrixCommandAspect hystrixAspect() {
        return new HystrixCommandAspect();
    }
}