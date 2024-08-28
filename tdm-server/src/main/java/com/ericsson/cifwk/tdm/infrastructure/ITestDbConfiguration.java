package com.ericsson.cifwk.tdm.infrastructure;

import com.mongodb.MongoCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.util.SocketUtils;

import java.util.Collections;
import java.util.List;

import static com.ericsson.cifwk.tdm.infrastructure.Profiles.INTEGRATION_TEST;

/**
 * Allocates random port for in-memory mongo db instance
 */
@Configuration
@Profile(INTEGRATION_TEST)
public class ITestDbConfiguration extends BaseInMemoryDbConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(ITestDbConfiguration.class);

    private int mongoPort;

    public ITestDbConfiguration() {
        mongoPort = SocketUtils.findAvailableTcpPort();
        LOGGER.info("mongoDB in-memory port: {}", mongoPort);
    }

    protected int determineMongoPort() {
        return mongoPort;
    }

    @Override
    protected int determineBackupMongoPort() {
        return mongoPort;
    }

    @Override
    protected List<MongoCredential> determineMongoCredential() {
        return Collections.emptyList();
    }
}
