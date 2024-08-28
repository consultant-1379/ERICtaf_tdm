package com.ericsson.cifwk.tdm.infrastructure;

import org.springframework.context.annotation.Configuration;
import org.springframework.session.hazelcast.config.annotation.web.http.EnableHazelcastHttpSession;

/**
 * Instantiates springs security aware hazel cast web filter
 */

@EnableHazelcastHttpSession(maxInactiveIntervalInSeconds = 3600)
@Configuration
public class HazelcastHttpSessionConfiguration {
}