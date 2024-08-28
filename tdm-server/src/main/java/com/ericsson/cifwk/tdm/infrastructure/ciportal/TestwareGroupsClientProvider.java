package com.ericsson.cifwk.tdm.infrastructure.ciportal;

import com.ericsson.cifwk.tdm.application.ciportal.testware.CIPortalTestwareRepository;
import com.ericsson.cifwk.tdm.model.testware.ArtifactItems;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static com.ericsson.cifwk.tdm.application.util.XmlParser.parseObjectFile;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.CUSTOMER;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.DEVELOPMENT;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.TEST;

@Configuration
@Profile({DEVELOPMENT, TEST, CUSTOMER})
public class TestwareGroupsClientProvider {

    @Value("${mock.testware}")
    private String testwareFile;

    @Bean
    @Primary
    public CIPortalTestwareRepository mockCIPortalTestwareRepository() {
        class MockCIPortalTestwareRepository extends CIPortalTestwareRepository {

            @Override
            public ArtifactItems getTestwareArtifacts() {
                return parseObjectFile(testwareFile, ArtifactItems.class);
            }
        }
        return new MockCIPortalTestwareRepository();
    }
}
