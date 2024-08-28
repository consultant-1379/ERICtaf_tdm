package com.ericsson.cifwk.tdm.configuration;

import com.ericsson.cifwk.tdm.integration.CustomRestClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TCE_CLIENT;
import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TESTWARE_GROUPS_CLIENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static org.mockito.Mockito.mock;

@Configuration
@Profile(INTEGRATION_TEST)
public class MockRestClientsConfiguration {

    @Bean
    @Primary
    @Qualifier(TCE_CLIENT)
    public CustomRestClient mockContextEditorRestClient() {
        return mock(CustomRestClient.class);
    }

    @Bean
    @Primary
    @Qualifier(TESTWARE_GROUPS_CLIENT)
    public CustomRestClient mockCiPortalRestClient() {
        return mock(CustomRestClient.class);
    }
}
