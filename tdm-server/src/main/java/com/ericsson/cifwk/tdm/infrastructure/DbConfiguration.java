package com.ericsson.cifwk.tdm.infrastructure;

import com.mongodb.MongoCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.PRODUCTION;
import static com.ericsson.cifwk.tdm.infrastructure.Profiles.STAGE;
import static java.util.Arrays.asList;

@Configuration
@Profile({PRODUCTION, STAGE})
public class DbConfiguration extends BaseDbConfiguration {

    @Value("${spring.data.mongodb.port}")
    private int mongoPort;

    @Value("${spring.data.mongodb.user}")
    private String mongoUser;

    @Value("${spring.data.mongodb.password}")
    private String mongoPassword;

    @Value("${spring.data.mongodb.backup_port}")
    private Integer backUpPort;

    @Override
    protected int determineMongoPort() {
        return mongoPort;
    }

    @Override
    protected int determineBackupMongoPort() {
        return backUpPort;
    }

    @Override
    protected List<MongoCredential> determineMongoCredential() {
        return asList(MongoCredential.createScramSha1Credential(mongoUser, mongoDatabase, mongoPassword.toCharArray()));
    }
}
