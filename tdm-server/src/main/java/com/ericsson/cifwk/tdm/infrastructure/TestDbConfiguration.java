package com.ericsson.cifwk.tdm.infrastructure;

import com.mongodb.MongoCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Collections;
import java.util.List;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.TEST;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.CUSTOMER;

@Configuration
@Profile({TEST, CUSTOMER})
public class TestDbConfiguration  extends BaseDbConfiguration {

    @Value("${spring.data.mongodb.port}")
    private int mongoPort;

    @Override
    protected int determineMongoPort() {
        return mongoPort;
    }

    @Override
    protected int determineBackupMongoPort() {
        return mongoPort;
    }

    @Override
    protected List<MongoCredential> determineMongoCredential() {
        return Collections.<MongoCredential>emptyList();
    }
}
