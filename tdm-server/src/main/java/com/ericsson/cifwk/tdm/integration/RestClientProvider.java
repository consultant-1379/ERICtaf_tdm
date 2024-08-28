package com.ericsson.cifwk.tdm.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TCE_CLIENT;
import static com.ericsson.cifwk.tdm.application.constants.Qualifiers.TESTWARE_GROUPS_CLIENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;
import static com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider.DEFAULT_ANNOTATIONS;

@Configuration
@Profile("!" + INTEGRATION_TEST)
public class RestClientProvider {

    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    @Qualifier(TCE_CLIENT)
    public CustomRestClient provideTceRestClientFacade(@Value("${remote.tce.url.api}") String tceUrl) {
        return buildCustomRestClient(tceUrl);
    }

    @Bean
    @Qualifier(TESTWARE_GROUPS_CLIENT)
    public CustomRestClient provideTestwareRestClientFacade(
            @Value("${remote.ci.testware.url}") String ciPortalUrl) {
        return buildCustomRestClient(ciPortalUrl);
    }

    private CustomRestClient buildCustomRestClient(String url) {
        return new CustomRestClient.Builder(url)
                .withComponent(new JacksonJaxbJsonProvider(objectMapper, DEFAULT_ANNOTATIONS))
                .build();
    }
}
